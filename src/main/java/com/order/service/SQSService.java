package com.order.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.order.exceptions.SqsPostException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.util.ArrayList;
import java.util.List;

public class SQSService {
    private final SqsClient sqsClient;
    private final Context context;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String queueUrl = "https://sqs.us-east-1.amazonaws.com/215068915431/order-processing-queue.fifo";

    public SQSService(SqsClient sqsClient, Context context) {
        this.sqsClient = sqsClient;
        this.context = context;
    }

    public void postToSQS(String dealerId, String customerId, String orderId, String bucketName, String objectKey) {
        try {
            // snippet-start:[sqs.java2.sqs_example.send_message]
            JsonObject obj = new JsonObject();
            obj.addProperty("dealerId", dealerId);
            obj.addProperty("customerId", customerId);
            obj.addProperty("orderId", orderId);
            obj.addProperty("bucketname", bucketName);
            obj.addProperty("fileName", objectKey);

            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(gson.toJson(obj)).messageGroupId("OrderProcess")
                    .build());
            // snippet-end:[sqs.java2.sqs_example.send_message]

        } catch (SqsException e) {
            context.getLogger().log("SQS Exception : " + e.getMessage());
            throw new SqsPostException("Sqs message publish failed");

        }
    }

    public List<Message> readMessages() {
        try {
            // snippet-start:[sqs.java2.sqs_example.retrieve_messages]
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .build();
            return sqsClient.receiveMessage(receiveMessageRequest).messages();

        } catch (SqsException e) {
            context.getLogger().log("Exception while reading sqs messages" + e.getMessage());
            throw new SqsPostException("Failed to read messages from SQS");
        }
    }
}
