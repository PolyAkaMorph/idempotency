package com.example.idempotency.exception;

public class RequestRunningException extends Exception {
    public RequestRunningException() {
        super("Request is still processing");
    }
}
