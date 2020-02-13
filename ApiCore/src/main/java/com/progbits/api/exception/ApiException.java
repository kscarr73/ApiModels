package com.progbits.api.exception;

/**
 *
 * @author scarr
 */
public class ApiException extends Exception {
 
    public ApiException() {
    }
    
    public ApiException(String message) {
        super(message);
    }
    
    public ApiException(String message, Throwable ex) {
        super(message, ex);
    }
}
