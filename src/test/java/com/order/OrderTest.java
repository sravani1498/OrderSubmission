package com.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.order.handlers.Order;
import com.order.model.OrderRequest;
import org.junit.jupiter.api.Test;

public class OrderTest {

    @Test
    public void handleRequest_shouldReturnConstantValue() {
        Order function = new Order();
        Object result = function.handleRequest(new OrderRequest(), null);
        assertNotNull(result);
    }
}
