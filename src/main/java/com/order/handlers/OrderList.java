package com.order.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.DependencyFactory;
import com.order.model.Order;
import com.order.model.OrderRequest;
import com.order.service.DbService;
import com.order.service.S3Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.ArrayList;

public class OrderList implements RequestHandler<OrderRequest, ArrayList<Order>>{
    private final S3Client s3Client;
    private final DynamoDbClient dynamoDbClient;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public OrderList() {
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        s3Client = DependencyFactory.s3Client();
        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        dynamoDbClient = DependencyFactory.dynamoDbClient();

        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
    }

//
    @Override
    public ArrayList<Order> handleRequest(OrderRequest requestEvent, Context context) {
        S3Service s3Service = new S3Service(s3Client, context);
        DbService dbService = new DbService(dynamoDbClient, context);
        ArrayList<com.order.entity.Order> orderListFromDb;
        ArrayList<Order> orderList = new ArrayList<>();

        if(requestEvent.isShowAll()) {
            orderListFromDb = dbService.orderList(requestEvent.getDealerId());
        } else {
            orderListFromDb = dbService.orderList(requestEvent.getDealerId(), requestEvent.getOrderId(), requestEvent.getCustomerId());
        }

        orderListFromDb.forEach(order -> {
            Order orderObject = s3Service.getObjectFromS3(order.getBucketName(),order.getFileName());
            orderList.add(orderObject);
        });
        return orderList;
    }
}
