package com.order.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.exceptions.OrderUpdateException;
import com.order.exceptions.S3FileUploadException;
import com.order.model.Order;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class OrderUpdateService {

    private Context context;
    private DynamoDbEnhancedClient enhancedClient;
    private BlobServiceClient blobServiceClient;
    Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public OrderUpdateService(DynamoDbClient client, BlobServiceClient blobServiceClient, Context context) {
        this.context = context;
        this.blobServiceClient = new BlobServiceClientBuilder()
                .endpoint("https://<your-storage-account-name>.blob.core.windows.net")
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
        this.enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(client)
                .build();
    }

    public Order updateOrder(Order order) {
        Boolean checkStatus = checkOrderStatus(order);
        if(checkStatus) {
            return updateBlobObject(order);
        } else {
            context.getLogger().log("Order is already processed.Can not update now...");
            throw new OrderUpdateException("Order is already processed.Can not update now...");
        }
    }

    private Boolean checkOrderStatus(Order order) {
        DynamoDbTable<com.order.entity.Order> orderTable = enhancedClient.table("Order", TableSchema.fromBean(com.order.entity.Order.class));
        Key key = Key.builder().partitionValue(order.getDealerId()).sortValue(order.getCustomerId() + "#" + order.getOrderId()).build();
        // Get the item by using the key.
        com.order.entity.Order result = orderTable.getItem(
                (GetItemEnhancedRequest.Builder requestBuilder) -> requestBuilder.key(key));
        if(result.getOrderStatus() == "Received") {
            return true;
        } else {
            return false;
        }
    }

    private Order updateBlobObject(Order order){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String fileName = order.getDealerId() + "_" + order.getCustomerId()+ "_" + order.getOrderId();
        try {
            BlockBlobClient blobClient = blobServiceClient.getBlobContainerClient("dealer-orders").getBlobClient(fileName).getBlockBlobClient();
            blobClient.upload(BinaryData.fromString(gson.toJson(order)), true);
            return order;
        } catch(Exception e) {
            context.getLogger().log("Exception while updating object to Blob Storage : " + e.getMessage());
            throw new S3FileUploadException("Object update failed");
        }
    }
}