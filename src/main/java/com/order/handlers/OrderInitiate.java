package com.order.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.DependencyFactory;
import com.order.entity.Order;
import com.order.service.DbService;
import com.order.service.SQSService;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.ArrayList;

public class OrderInitiate {

    private final DynamoDbClient dynamoDbClient;
    private final SqsClient sqsClient;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public OrderInitiate() {

        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        dynamoDbClient = DependencyFactory.dynamoDbClient();

        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables

        sqsClient = DependencyFactory.sqsClient();
    }

    public void handleRequest(Context context) {
        DbService dbService = new DbService(dynamoDbClient, context);
        SQSService sqsService = new SQSService(sqsClient, context);
        ArrayList<Order> ordersFromDb = dbService.scanOrderReceived();
        context.getLogger().log("Total orders with status received"+ordersFromDb.size());
        ordersFromDb.forEach(order -> {
            String customerId = order.getCustomerIdAndOrderId().split("#")[0];
            String orderId = order.getCustomerIdAndOrderId().split("#")[1];
            sqsService.postToSQS(order.getDealerId(),customerId, orderId, order.getBucketName(), order.getFileName());
            dbService.updateOrder(order.getDealerId(),order.getCustomerIdAndOrderId());
        });
    }

}
