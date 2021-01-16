package com.progbits.api.export;

import com.progbits.api.model.ApiObject;

/**
 * Defines an interface for running an Export
 * 
 * @author scarr
 */
public interface ApiExport {
	public String performExport(String mainClass, ApiObject obj);
}
