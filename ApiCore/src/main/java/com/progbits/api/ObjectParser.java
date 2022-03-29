package com.progbits.api;

import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;

/**
 * Defines a Parser Interface
 *
 * @author scarr
 */
public interface ObjectParser {

    /**
     * Initialize the Parser using an Input Stream
     *
     * @param classes ApiClasses for this Parser
     * @param mainClass The mainClass/rootClass to be used from ApiClasses
     * @param properties Properties to change default behavior of the Parser
     * @param in InputStream to pass to the Parser and read from
     */
    void initStream(ApiClasses classes, String mainClass,
            Map<String, String> properties, InputStream in) throws ApiException;

    /**
     * Initialize the Parser using a Reader Object
     *
     * @param classes ApiClasses for this Parser
     * @param mainClass The mainClass/rootClass to be used from ApiClasses
     * @param properties Properties to change default behavior of the Parser
     * @param in Reader to pass to the Parser and read from
     * @throws ApiException
     */
    void init(ApiClasses classes, String mainClass,
            Map<String, String> properties, Reader in) throws ApiException;

    boolean next() throws ApiException, ApiClassNotFoundException;

    /**
     * Parse a Single Object from the input stream
     *
     * @param in Input Stream Reader
     * @return
     */
    ApiObject parseSingle(Reader in) throws ApiException, ApiClassNotFoundException;

    ApiObject parseSingle(Reader in, String className) throws ApiException, ApiClassNotFoundException;

    ApiObject getObject();

    ObjectParser getParser();

    List<String> getParseErrors();

    Throwable getThrowException();
}
