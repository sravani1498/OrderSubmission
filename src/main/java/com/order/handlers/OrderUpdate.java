package com.order.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.DependencyFactory;
import com.order.exceptions.OrderUpdateException;
import com.order.model.Order;
import com.order.service.DbService;
import com.order.service.S3Service;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class OrderUpdate implements RequestHandler<Order, Order> {

    private final BlobServiceClient blobServiceClient;
    private final DynamoDbClient dynamoDbClient;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public OrderUpdate() {
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        blobServiceClient = new BlobServiceClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        dynamoDbClient = DependencyFactory.dynamoDbClient();

        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
    }


    @Override
    public Order handleRequest(Order orderInput, Context context) {
        S3Service s3Service = new S3Service(blobServiceClient, context); //TODO: Need manual change
        DbService dbService = new DbService(dynamoDbClient, context);
        Boolean checkOrderStatus = dbService.checkOrderStatus(orderInput);
        context.getLogger().log("status"+checkOrderStatus);
        if(checkOrderStatus) {
            com.order.entity.Order order = s3Service.putObjectToS3(orderInput); //TODO: Need manual change
        } else {
            throw new OrderUpdateException("Order is already processed.Can not update now...");
        }
        return orderInput;
    }
}