package com.order.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.exceptions.IoException;
import com.order.model.Order;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.utils.IoUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class S3Service {

    private Context context;
    private S3Client s3Client;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public S3Service(S3Client s3Client, Context context) {
        this.context = context;
        this.s3Client = s3Client;
    }

    public com.order.entity.Order putObjectToS3(Order order) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String fileName = order.getDealerId() + "_" + order.getCustomerId()+ "_" + order.getOrderId();
        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket("dealer-orders")
                    .key(fileName)
                    .build();

            s3Client.putObject(objectRequest, RequestBody.fromString(gson.toJson(order)));
            com.order.entity.Order orderEntity = new com.order.entity.Order(order.getDealerId(), order.getCustomerId() +"#" + order.getOrderId(), "dealer-orders", fileName, order.getOrderStatus());
            return orderEntity;
        } catch(S3Exception e) {
            context.getLogger().log("Exception while writing object to S3 : " + e.getMessage());
            throw new RuntimeException("File Upload failed");
        }
    }

    public Order getObjectFromS3(String bucketName, String fileName) throws IoException {
        GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .key(fileName)
                .bucket(bucketName)
                .build();
        try{
            ResponseInputStream<GetObjectResponse> responseInputStream = s3Client.getObject(objectRequest);

            InputStream stream = new ByteArrayInputStream(responseInputStream.readAllBytes());
            String s3Object = IoUtils.toUtf8String(stream);
            return gson.fromJson(s3Object, Order.class);
        } catch( java.io.IOException e) {
            context.getLogger().log("Exception while reading order from s3: "+e.getMessage());
            throw new IoException("Error while reading s3 object");
        }
    }
}
