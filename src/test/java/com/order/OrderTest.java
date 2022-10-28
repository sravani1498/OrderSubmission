package com.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.order.model.Request;
import org.junit.jupiter.api.Test;

public class OrderTest {

    @Test
    public void handleRequest_shouldReturnConstantValue() {
        Order function = new Order();
        Object result = function.handleRequest(new Request(), null);
        assertNotNull(result);
    }
}
