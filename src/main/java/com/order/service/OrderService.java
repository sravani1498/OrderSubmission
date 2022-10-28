package com.order.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.entity.Order;
import com.order.model.OrderSave;
import com.order.model.Request;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.*;

public class OrderService {

    private Context context;
    private DynamoDbEnhancedClient enhancedClient;
    private S3Client s3Client;

    public OrderService(DynamoDbClient client, S3Client s3Client, Context context) {
        this.context = context;
        this.s3Client = s3Client;
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();
    }

    public ArrayList<OrderSave> orderSave(Request input) {
        String decodedBase64String = base64ToString(input.getBase64());
        return parseStringToJson(decodedBase64String, input.getDealerId());
    }

    public String base64ToString(String base64){
        Base64.Decoder decoder = Base64.getMimeDecoder();

        byte[] decodedBytes = decoder.decode(base64);
        return new String(decodedBytes);
    }

    public ArrayList<OrderSave> parseStringToJson(String inputString, String dealerid) {
        ArrayList<OrderSave> orderList = new ArrayList<>();
        String[] lines = inputString.split(System.lineSeparator());

        List<String> headersList = Arrays.asList(lines[0].split(","));
        for(int i=1; i < lines.length; i++) {
            List<String> data = Arrays.asList(lines[i].split(","));
            if (data.size() != headersList.size()) {
                return null;
            } else {
                //Random Order Id
                Random rnd = new Random();
                int number = rnd.nextInt(999999);

                // this will convert any number sequence into 6 character.
                String orderId = String.format("%06d", number);

                OrderSave order = new OrderSave(data.get(0), data.get(1), data.get(2), data.get(3), data.get(4), data.get(5), data.get(6), orderId, "Received");

                OrderSave savedOrder = saveObjectToS3(order, dealerid);
                orderList.add(savedOrder);

            }
        }
        return orderList;
    }

    public OrderSave saveObjectToS3(OrderSave order, String dealerId) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String fileName = dealerId + "_" + order.getCustomerId()+ "_" + order.getOrderId();
        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket("dealer-orders")
                    .key(fileName)
                    .build();

            s3Client.putObject(objectRequest, RequestBody.fromString(gson.toJson(order)));
            Order orderEntity = new Order(dealerId, order.getCustomerId() , order.getOrderId(), "dealer-orders", fileName, order.getOrderStatus());
            saveObjectToDb(orderEntity);
            return order;
        } catch(S3Exception e) {
            context.getLogger().log("Exception while writing object to S3 : " + e.getMessage());
            throw new RuntimeException("File Upload failed");
        }
    }

    public void saveObjectToDb(Order order) {
        try {
            DynamoDbTable<Order> orderTable = enhancedClient.table("Order", TableSchema.fromBean(Order.class));
            // Put the customer data into an Amazon DynamoDB table.
            orderTable.putItem(order);

        } catch (DynamoDbException e) {
            context.getLogger().log("Exception while writing data to db " +e.getMessage());
        }
        context.getLogger().log("Order data added to the table");
    }

}
