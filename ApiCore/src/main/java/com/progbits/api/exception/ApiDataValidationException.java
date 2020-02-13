package com.progbits.api.exception;

public class ApiDataValidationException extends RuntimeException{

    public ApiDataValidationException() {
    }

    public ApiDataValidationException(String message) {
        super(message);
    }

    public ApiDataValidationException(String message, Throwable ex) {
        super(message, ex);
    }

}
