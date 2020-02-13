package com.progbits.api;

import com.progbits.api.exception.ApiException;

/**
 *
 * @author scarr
 */
public interface WriterService {

	ObjectWriter getWriter(String type) throws ApiException;
}
