package com.progbits.api;

import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 *
 * @author scarr
 */
public interface ObjectWriter {

   void init(ApiClasses classes, Map<String, String> properties,
           Writer out) throws ApiException;

   void init(ApiClasses classes, String mainClass, Map<String, String> properties,
           Writer out) throws ApiException;

   void initStream(ApiClasses classes, String mainClass, Map<String, String> properties,
           OutputStream out) throws ApiException;

   void write(ApiObject obj) throws ApiException;

   void writeHeader() throws ApiException;

   /**
    * Write a Single Object to a String
    *
    * @param obj
    * @return
    * @throws ApiException An API Exception
    */
   String writeSingle(ApiObject obj) throws ApiException;

   ObjectWriter getWriter();

   List<String> getWriteErrors();

   Throwable getThrowException();
}
