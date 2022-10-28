package com.order.entity;

import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.ListAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.StringAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.ArrayList;

import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.*;

@DynamoDbBean
public class Order {

    public Order(String dealerId, String customerId, String orderId, String bucketName, String fileName, String orderStatus) {
        this.dealerId = dealerId;
        this.customerId = customerId;
        OrderId = orderId;
        this.bucketName = bucketName;
        this.fileName = fileName;
        this.orderStatus = orderStatus;
    }

    public static final StaticTableSchema.Builder<Order> TABLE_SCHEMA =
            StaticTableSchema.builder(Order.class)
                    .newItemSupplier(Order::new)
                    .addAttribute(String.class, a -> a.name("dealerId")
                            .getter(Order::getDealerId)
                            .setter(Order::setDealerId)
                            .tags(primaryPartitionKey()))
                    .addAttribute(String.class,
                            a -> a.name("customerIdAndOrderId")
                                    .getter(Order::getCustomerId)
                                    .setter(Order::setCustomerId).tags(primarySortKey())
                                    );

    public Order() {

    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("dealerId")
    public String getDealerId() {
        return dealerId;
    }

    public void setDealerId(String dealerId) {
        this.dealerId = dealerId;
    }

    @DynamoDbAttribute("bucketName")
    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

     @DynamoDbAttribute("fileName")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    @DynamoDbAttribute("orderStatus")
    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    private String dealerId;
    @DynamoDbAttribute("customerId")
    @DynamoDbSortKey
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    @DynamoDbSecondarySortKey(indexNames = "orderId")
    @DynamoDbAttribute("orderId")
    public String getOrderId() {
        return OrderId;
    }

    public void setOrderId(String orderId) {
        OrderId = orderId;
    }

    private String customerId;
    private String OrderId;
    private String bucketName;
    private String fileName;
    private String orderStatus;
}
