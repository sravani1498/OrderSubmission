package com.order.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.DependencyFactory;
import com.order.service.SQSService;
import com.order.service.SfnClientService;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

public class SQSRead implements RequestHandler<SQSEvent, com.order.model.Order> {
    private final SqsClient sqsClient;
    private final SfnClient sfnClient;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public SQSRead() {
        sqsClient = DependencyFactory.sqsClient();
        sfnClient = DependencyFactory.sfnClient();
    }



    public com.order.model.Order handleRequest(SQSEvent sqsEvent, Context context) {
        context.getLogger().log(sqsEvent.getRecords().get(0).getBody());
        com.order.model.Order order = gson.fromJson(sqsEvent.getRecords().get(0).getBody(), com.order.model.Order.class);
        SfnClientService service = new SfnClientService(sfnClient, context);
        service.startExecution(order);
        return order;
    }
}
