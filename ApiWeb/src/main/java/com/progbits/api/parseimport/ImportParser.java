package com.progbits.api.parseimport;

import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiObject;
import com.progbits.api.srv.ApiWebServlet;
import java.io.InputStream;

/**
 *
 * @author scarr
 */
public interface ImportParser {
    ApiObject parseImport(String packagePrefix, ApiWebServlet webSrv, InputStream is) throws ApiException ;
}
