package com.progbits.api;

import com.progbits.api.exception.ApiException;

/**
 *
 * @author scarr
 */
public interface ParserService {

	ObjectParser getParser(String type) throws ApiException;
}
