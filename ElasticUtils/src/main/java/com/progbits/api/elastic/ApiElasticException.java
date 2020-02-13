package com.progbits.api.elastic;

/**
 *
 * @author scarr
 */
public class ApiElasticException extends Exception {
    public ApiElasticException(String message, Throwable ex) {
        super(message, ex);
    }
}
