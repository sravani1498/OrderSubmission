package com.order.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.entity.Order;
import com.order.exceptions.IoException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.utils.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;

public class DbService {

    private Context context;
    private DynamoDbEnhancedClient enhancedClient;
    Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public DbService(DynamoDbClient client, Context context) {
        this.context = context;
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();
    }

    public void saveObjectToDb(com.order.entity.Order order) {
        try {
            DynamoDbTable<Order> orderTable = enhancedClient.table("Order", TableSchema.fromBean(com.order.entity.Order.class));
            // Put the customer data into an Amazon DynamoDB table.
            orderTable.putItem(order);

        } catch (DynamoDbException e) {
            context.getLogger().log("Exception while writing data to db " +e.getMessage());
        }
        context.getLogger().log("Order data added to the table");
    }

    public  Boolean checkOrderStatus(com.order.model.Order order) {
        context.getLogger().log(order.getCustomerId()+ ""+order.getDealerId() + " "+order.getOrderId());
        DynamoDbTable<com.order.entity.Order> orderTable = enhancedClient.table("Order", TableSchema.fromBean(com.order.entity.Order.class));
        Key key = Key.builder().partitionValue(order.getDealerId()).sortValue(order.getCustomerId() + "#" + order.getOrderId()).build();
        // Get the item by using the key.
        com.order.entity.Order result = orderTable.getItem(
                (GetItemEnhancedRequest.Builder requestBuilder) -> requestBuilder.key(key));
        context.getLogger().log(result.getOrderStatus());
        if(StringUtils.equals(result.getOrderStatus(),"Received")) {
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<Order> orderList(String dealerCode, String orderId, String customerId) throws IoException {
        DynamoDbTable<com.order.entity.Order> orderTable = enhancedClient.table("Order", TableSchema.fromBean(com.order.entity.Order.class));
        Key key = Key.builder().partitionValue(dealerCode).sortValue(customerId + "#" + orderId).build();
        // Get the item by using the key.
        com.order.entity.Order result = orderTable.getItem(
                (GetItemEnhancedRequest.Builder requestBuilder) -> requestBuilder.key(key));
        ArrayList<Order> OrderListFromDb = new ArrayList<>();

//        com.order.model.Order order = getObjectFromS3(result.getBucketName(), result.getFileName());
        OrderListFromDb.add(result);
        return OrderListFromDb;
    }

    public ArrayList<Order> orderList(String dealerCode) throws IoException {
        DynamoDbTable<com.order.entity.Order> orderTable = enhancedClient.table("Order", TableSchema.fromBean(com.order.entity.Order.class));
        QueryConditional queryConditional = QueryConditional.keyEqualTo(Key.builder()
                .partitionValue(dealerCode)
                .build());

        // Get items in the table and write out the ID value.
        Iterator<Order> results = orderTable.query(queryConditional).items().iterator();
        ArrayList<Order> OrderListFromDb = new ArrayList<>();
        ArrayList<com.order.model.Order> ordersList = new ArrayList<>();
        do {
            OrderListFromDb.add(results.next());
//            ordersList.add(getObjectFromS3(order.getBucketName(), order.getFileName()));
        } while(results.hasNext());
        return OrderListFromDb;
    }

}
