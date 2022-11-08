package com.order.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.DependencyFactory;
import com.order.exceptions.InputException;
import com.order.model.OrderRequest;
import com.order.service.DbService;
import com.order.service.S3Service;
import com.order.service.UtilHelper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.utils.StringUtils;

import java.util.*;

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class Order implements RequestHandler<OrderRequest, ArrayList<com.order.model.Order>> {
    private final S3Client s3Client;
    private final DynamoDbClient dynamoDbClient;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public Order() {
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        s3Client = DependencyFactory.s3Client();
        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        dynamoDbClient = DependencyFactory.dynamoDbClient();

        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
    }

    public ArrayList<com.order.model.Order> handleRequest(OrderRequest requestEvent, Context context) {
        if(StringUtils.isEmpty(requestEvent.getFileContent()) || StringUtils.isEmpty(requestEvent.getDealerId())) {
            throw new InputException("User name or password is empty");
        }
        ArrayList<com.order.model.Order> orderList = new UtilHelper().convertToOrder(requestEvent);
        S3Service s3Service = new S3Service(s3Client, context);
        DbService dbService = new DbService(dynamoDbClient, context);
        orderList.forEach(order ->  {
            order.setCity(order.getCity().replaceAll("[^A-Za-z0-9]",""));
            order.setMake(order.getMake().replaceAll("[^A-Za-z0-9]",""));
            com.order.entity.Order orderEntity = s3Service.putObjectToS3(order);
            dbService.saveObjectToDb(orderEntity);
        });


        return orderList;
    }

}
