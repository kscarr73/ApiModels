package com.progbits.api.exception;

/**
 *
 * @author scarr
 */
public class ApiClassNotFoundException extends Exception {
    private String className = null;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
    
    public ApiClassNotFoundException() {
    }
    
    public ApiClassNotFoundException(String message) {
        super(message);
    }
    
    public ApiClassNotFoundException(String message, Throwable ex) {
        super(message, ex);
    }
    
    public ApiClassNotFoundException(String message, String className, Throwable ex) {
        super(message, ex);
        
        this.className = className;
    }
}
