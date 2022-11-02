package com.order.exceptions;

public class SqsPostException extends RuntimeException {
    public SqsPostException(String message) {
        super(message);
    }
}
