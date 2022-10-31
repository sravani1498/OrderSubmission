package com.order.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.entity.Order;
import com.order.exceptions.IoException;
import com.order.model.OrderSave;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.utils.IoUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

public class OrderListService {

    private Context context;
    private DynamoDbEnhancedClient enhancedClient;
    private S3Client s3Client;
    Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public OrderListService(DynamoDbClient client, S3Client s3Client, Context context) {
        this.context = context;
        this.s3Client = s3Client;
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();
    }

    public ArrayList<OrderSave> orderList(String dealerCode, String orderId, String customerId) throws IoException {
        DynamoDbTable<Order> orderTable = enhancedClient.table("Order", TableSchema.fromBean(Order.class));
        Key key = Key.builder().partitionValue(dealerCode).sortValue(customerId + "#" + orderId).build();
        // Get the item by using the key.
        Order result = orderTable.getItem(
                (GetItemEnhancedRequest.Builder requestBuilder) -> requestBuilder.key(key));
        ArrayList<OrderSave> ordersList = new ArrayList<>();

        OrderSave order = getObjectFromS3(result.getBucketName(), result.getFileName());
        ordersList.add(order);
        return ordersList;
    }

    public ArrayList<OrderSave> orderList(String dealerCode) throws IoException {
        DynamoDbTable<Order> orderTable = enhancedClient.table("Order", TableSchema.fromBean(Order.class));
        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                .partitionValue(dealerCode)
                .build());

        // Get items in the table and write out the ID value.
        Iterator<Order> results = orderTable.query(queryConditional).items().iterator();
        ArrayList<OrderSave> ordersList = new ArrayList<>();
        do {
            Order order = results.next();
            ordersList.add(getObjectFromS3(order.getBucketName(), order.getFileName()));
        } while(results.hasNext());
        return ordersList;
    }

    public OrderSave getObjectFromS3(String bucketName, String fileName) throws IoException {
        GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .key(fileName)
                .bucket(bucketName)
                .build();
        try{
            ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(objectRequest);

            InputStream stream = new ByteArrayInputStream(responseInputStream.readAllBytes());
            String s3Object = IoUtils.toUtf8String(stream);
            return gson.fromJson(s3Object, OrderSave.class);
        } catch( java.io.IOException e) {
            context.getLogger().log("Exception while reading order from s3: "+e.getMessage());
            throw new IoException("Error while reading s3 object");
        }
    }

}
