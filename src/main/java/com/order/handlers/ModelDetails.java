package com.order.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.DependencyFactory;
import com.order.exceptions.IoException;
import com.order.model.Order;
import okhttp3.*;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ModelDetails {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Order handleRequest(Order orderInput, Context context) throws IoException, IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://34.229.47.108:8083/dealer/model")).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(orderInput)))
                .build();

        context.getLogger().log("Request" + request);

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        context.getLogger().log("response" + response.body());

        Order order = gson.fromJson(response.body(), Order.class);
        return order;
    }
}
