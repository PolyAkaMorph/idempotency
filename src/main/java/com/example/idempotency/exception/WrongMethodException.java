package com.example.idempotency.exception;

public class WrongMethodException extends Exception {
    public WrongMethodException() {
        super("Wrong method or path");
    }

}
