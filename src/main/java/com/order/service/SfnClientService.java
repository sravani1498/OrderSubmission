package com.order.service;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.exceptions.StepFunctionException;
import com.order.model.Order;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.SfnException;
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest;
import software.amazon.awssdk.services.sfn.model.StartExecutionResponse;

import java.net.ContentHandler;
import java.util.UUID;

public class SfnClientService {

    private SfnClient sfnClient;
    private Context context;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public SfnClientService(SfnClient sfnClient, Context context) {
        this.sfnClient = sfnClient;
        this.context = context;
    }

    public void startExecution(Order order){
        UUID uuid = UUID.randomUUID();
        String uuidValue = uuid.toString();
        try {

            StartExecutionRequest executionRequest = StartExecutionRequest.builder()
                    .input(gson.toJson(order))
                    .stateMachineArn("arn:aws:states:us-east-1:215068915431:stateMachine:MyStateMachine")
                    .name(uuidValue)
                    .build();

            sfnClient.startExecution(executionRequest);

        } catch (SfnException e) {
            context.getLogger().log("Error while start state machine"+e.getMessage());
            throw new StepFunctionException("Error while start state machine"+e.getMessage());
        }
    }
}
