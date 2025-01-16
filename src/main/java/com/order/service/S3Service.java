package com.order.service;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobClient;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.exceptions.IoException;
import com.order.model.Order;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class S3Service {

    private Context context;
    private BlobServiceClient blobServiceClient;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public S3Service(BlobServiceClient blobServiceClient, Context context) {
        this.context = context;
        this.blobServiceClient = blobServiceClient;
    }

    public com.order.entity.Order putObjectToS3(Order order) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String fileName = order.getDealerId() + "_" + order.getCustomerId()+ "_" + order.getOrderId();
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("dealer-orders");
            BlobClient blobClient = containerClient.getBlobClient(fileName);

            blobClient.upload(new ByteArrayInputStream(gson.toJson(order).getBytes()), gson.toJson(order).getBytes().length, true);
            com.order.entity.Order orderEntity = new com.order.entity.Order(order.getDealerId(), order.getCustomerId() +"#" + order.getOrderId(), "dealer-orders", fileName, order.getOrderStatus());
            return orderEntity;
        } catch(Exception e) {
            context.getLogger().log("Exception while writing object to Azure Blob Storage : " + e.getMessage());
            throw new RuntimeException("File Upload failed");
        }
    }

    public Order getObjectFromS3(String bucketName, String fileName) throws IoException {
        try{
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(bucketName);
            BlobClient blobClient = containerClient.getBlobClient(fileName);

            InputStream stream = new ByteArrayInputStream(blobClient.downloadContent().toBytes());
            String blobObject = new String(stream.readAllBytes());
            return gson.fromJson(blobObject, Order.class);
        } catch( java.io.IOException e) {
            context.getLogger().log("Exception while reading order from Azure Blob Storage: "+e.getMessage());
            throw new IoException("Error while reading blob object");
        }
    }
}