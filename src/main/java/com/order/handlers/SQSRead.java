package com.order.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.DependencyFactory;
import com.order.service.SQSService;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

public class SQSRead implements RequestHandler<SQSEvent, Object> {
    private final SqsClient sqsClient;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public SQSRead() {
        sqsClient = DependencyFactory.sqsClient();
    }



    public Object handleRequest(SQSEvent sqsEvent,Context context) {
        context.getLogger().log(sqsEvent.getRecords().get(0).getBody());
        return sqsEvent.getRecords().get(0).getBody();
    }
}
