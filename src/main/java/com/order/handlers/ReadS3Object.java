package com.order.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.DependencyFactory;
import com.order.model.Order;
import com.order.service.S3Service;
import software.amazon.awssdk.services.s3.S3Client;

public class ReadS3Object implements RequestHandler<Order, Order> {

    private final S3Client s3Client;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public ReadS3Object() {

        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        s3Client = DependencyFactory.s3Client();
    }

    public Order handleRequest(Order input , Context context) {
        context.getLogger().log("Input Req: "+gson.toJson(input));
        S3Service s3Service = new S3Service(s3Client, context);
        Order order = s3Service.getObjectFromS3(input.getBucketname(),input.getFileName());
        order.setDealerId(input.getDealerId());
        return order;
    }
}
