package com.order.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.DependencyFactory;
import com.order.model.Error;
import com.order.model.OrderSave;
import com.order.model.RequestOrderList;
import com.order.model.Response;
import com.order.service.OrderListService;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.ArrayList;

public class OrderList implements RequestHandler<RequestOrderList, Response>{
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
    public Response handleRequest(RequestOrderList requestEvent, Context context) {
        OrderListService service = new OrderListService(dynamoDbClient, s3Client, context);
        ArrayList<OrderSave> ordersList;

        if(requestEvent.isShowAll()) {
            ordersList = service.orderList(requestEvent.getDealerId());

        } else {
            ordersList = service.orderList(requestEvent.getDealerId(), requestEvent.getOrderId(), requestEvent.getCustomerId());
        }
        return new Response(HttpStatusCode.OK,ordersList,new Error());

    }
}
