package com.order.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.entity.Order;
import com.order.exceptions.DbException;
import com.order.exceptions.IoException;
import com.order.model.OrderRequest;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
        } while(results.hasNext());
        return OrderListFromDb;
    }

    public ArrayList<Order> scanOrderReceived() {
        DynamoDbTable<com.order.entity.Order> orderTable = enhancedClient.table("Order", TableSchema.fromBean(com.order.entity.Order.class));
        AttributeValue attr = AttributeValue.builder()
                .s("Received")
                .build();
        // Get only Open items in the Work table
        Map<String, AttributeValue> myMap = new HashMap<>();
        myMap.put(":val1", attr);

        Map<String, String> myExMap = new HashMap<>();
        myExMap.put("#orderStatus", "orderStatus");

        // Set the Expression so only Closed items are queried from the Work table
        Expression expression = Expression.builder()
                .expressionValues(myMap)
                .expressionNames(myExMap)
                .expression("#orderStatus = :val1")
                .build();

        ScanEnhancedRequest enhancedRequest = ScanEnhancedRequest.builder()
                .filterExpression(expression)
                .build();

        // Get items in the Record table and write out the ID value
        Iterator<Order> results = orderTable.scan(enhancedRequest).items().iterator();
        ArrayList<Order> ordersFromDb = new ArrayList<>();

        while (results.hasNext()) {
            Order order = results.next();
            ordersFromDb.add(order);
        }
        return ordersFromDb;
    }

    public void updateOrder(String pKey, String sKey) {
        try {
            DynamoDbTable<com.order.entity.Order> orderTable = enhancedClient.table("Order", TableSchema.fromBean(com.order.entity.Order.class));
            Key key = Key.builder().partitionValue(pKey).sortValue(sKey).build();
            Order order = orderTable.getItem(
                    (GetItemEnhancedRequest.Builder requestBuilder) -> requestBuilder.key(key));
            order.setOrderStatus("Initiated");
            orderTable.updateItem(order);
        }catch(DynamoDbException e) {
            context.getLogger().log("Exception while updating order status: " + e.getMessage());
            throw new DbException("Can't update order status to process initiated");
        }
    }

    public void processOrder(com.order.model.Order order){
        try {
            DynamoDbTable<com.order.entity.Order> orderTable = enhancedClient.table("Order", TableSchema.fromBean(com.order.entity.Order.class));
            Key key = Key.builder().partitionValue(order.getDealerId()).sortValue(order.getCustomerId()+ "#" + order.getOrderId()).build();
            Order result = orderTable.getItem(
                    (GetItemEnhancedRequest.Builder requestBuilder) -> requestBuilder.key(key));
            result.setOrderStatus("Processed");
            result.setExpectedDeliveryDate(order.getExpectedDeliveryDate().toString());
            result.setPrice(order.getPrice());
            orderTable.updateItem(result);
        }catch(DynamoDbException e) {
            context.getLogger().log("Exception while updating order status: " + e.getMessage());
            throw new DbException("Can't update order status to process initiated");
        }
    }

}
