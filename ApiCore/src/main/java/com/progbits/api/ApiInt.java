package com.progbits.api;

import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;

/**
 * Defines an Interface that can be used for API Services. SOAP / REST / Others
 *
 * @author scarr
 */
public interface ApiInt {

	ApiObject processOperation(ApiClasses classes, ApiObject function) throws ApiException;
}
