package com.progbits.api.exception;

public class ApiDataValidationException extends Exception {

    public ApiDataValidationException() {
    }

    public ApiDataValidationException(String message) {
        super(message);
    }

    public ApiDataValidationException(String message, Throwable ex) {
        super(message, ex);
    }

}
