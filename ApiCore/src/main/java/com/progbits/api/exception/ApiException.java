package com.progbits.api.exception;

/**
 *
 * @author scarr
 */
public class ApiException extends Exception {
 
	private Integer code = 400;
	
    public ApiException() {
    }
    
    public ApiException(String message) {
        super(message);
    }
    
	public ApiException(Integer code, String message) {
		super(message);
		this.code = code;
	}
	
    public ApiException(String message, Throwable ex) {
        super(message, ex);
    }
	
    public ApiException(Integer code, String message, Throwable ex) {
        super(message, ex);
		this.code = code;
    }

	public Integer getCode() {
		return code;
	}
	
}
