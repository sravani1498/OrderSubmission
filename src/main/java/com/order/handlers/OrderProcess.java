package com.order.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.DependencyFactory;
import com.order.model.Order;
import com.order.service.DbService;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

public class OrderProcess  {
    private final DynamoDbClient dynamoDbClient;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public OrderProcess() {
        dynamoDbClient = DependencyFactory.dynamoDbClient();
    }


    public void handleRequest(Order orderInput, Context context) {
        DbService dbService = new DbService(dynamoDbClient, context);
        dbService.processOrder(orderInput);
    }
}
