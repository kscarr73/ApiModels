package com.progbits.api.srv;

/**
 *
 * @author scarr
 */
public class ApplicationException extends Exception {
	private Integer status;

	public Integer getStatus() {
		return status;
	}

	public ApplicationException(Integer status, String message) {
		super(message);
		this.status = status;
	}
	
}
