package com.order;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.order.model.Error;
import com.order.model.OrderSave;
import com.order.model.Request;
import com.order.model.Response;
import com.order.service.OrderService;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.StringUtils;

import java.util.*;

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class Order implements RequestHandler<Request, Response> {
    private final S3Client s3Client;
    private final DynamoDbClient dynamoDbClient;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    public Order() {
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        s3Client = DependencyFactory.s3Client();
        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        dynamoDbClient = DependencyFactory.dynamoDbClient();

        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables
    }

    public Response handleRequest(Request requestEvent, Context context) {
//        //fetch the request body
//        String body = requestEvent.getBody();
//
//        final Request input = gson.fromJson(body, Request.class);
        OrderService service = new OrderService(dynamoDbClient,s3Client,context);
        context.getLogger().log(requestEvent.getBase64()+ " " +requestEvent.getDealerId());
        if(StringUtils.isEmpty(requestEvent.getBase64()) || StringUtils.isEmpty(requestEvent.getDealerId())) {
            return returnResponse(HttpStatusCode.BAD_REQUEST, null, "Username or password is empty", "400", context);
        }

        ArrayList<OrderSave> ordersList = new ArrayList<>();

        try{
            ordersList = service.orderSave(requestEvent);
            if(ordersList.size() > 0) {
                return returnResponse(HttpStatusCode.OK, ordersList, null, null,context);
            } else {
                return returnResponse(HttpStatusCode.OK,ordersList, "File upload failed , please try again", "500",context);
            }
        } catch(Exception e) {
            return returnResponse(HttpStatusCode.OK, ordersList, "File upload failed , please try again", "500",context);
        }

    }

    public Response returnResponse(int statusCode, ArrayList<OrderSave> responseBody,
                                                       String errorMessage, String errorCode, Context context){
        Error error = new Error();
        if(errorCode != null){
            error.setErrorCode(errorCode);
            error.setErrorMessage(errorMessage);
        }

        Response responseEvent = new Response(statusCode, responseBody, error);
        context.getLogger().log("\n" + responseEvent.toString());
        return responseEvent;

    }

}
