package com.progbits.api.utils.oth;

import com.progbits.api.ApiMapping;
import com.progbits.api.ParserService;
import com.progbits.api.WriterService;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.util.Map;

/**
 *
 * @author scarr
 */
public interface ApiUtilsInterface {

    public void close();

    public void setMappingFactory(ApiMapping mapping);

    public ParserService getParser();

    public WriterService getWriter();

    public void retrieveClasses(String company, String thisClass, ApiClasses classes) throws ApiException, ApiClassNotFoundException;

    public void retrieveClasses(String company, String thisClass, ApiClasses classes,
            boolean verify) throws ApiException, ApiClassNotFoundException;

    public void retrievePackage(String Company, String thisClass, ApiClasses classes) throws ApiException, ApiClassNotFoundException;

    public void retrievePackage(String company, String thisClass, ApiClasses classes,
            boolean verify) throws ApiException, ApiClassNotFoundException;

    public void retrieveClasses(String company, String thisClass, ApiClasses classes,
            boolean verify, boolean localCopy) throws ApiException, ApiClassNotFoundException;

    public void retrievePackage(String company, String thisClass, ApiClasses classes,
            boolean verify, boolean localCopy) throws ApiException, ApiClassNotFoundException;

    /**
     * Returns a Object list of each service found
     *
     * @param company The Company that is making the request
     * @param serviceName The Name of the Service to locate, null if matches
     * access level
     * @param access Access level of the service, null, to access specific
     * service
     *
     * @return The Services found
     * @throws ApiClassNotFoundException Class Not Found
     * @throws ApiException General Exception
     */
    public ApiObject retrieveServices(String company, String serviceName, String access) throws ApiException, ApiClassNotFoundException;

    public Map<String, ApiObject> getApiServices(String company) throws ApiException, ApiClassNotFoundException;

    public void getApiClasses(String company, ApiObject apiService, ApiClasses classes) throws ApiException, ApiClassNotFoundException;

    /**
     * Returns a Mapping using the Api Mapping Service
     *
     * @param mapName The Name of the Map to Return
     * @return ThreadSafe Mapping Object for Mapping ApiObjects
     */
    public ApiMapping getApiMapping(String company, String mapName) throws ApiException, ApiClassNotFoundException;

    /**
     * Retrieves the Api Mapping Object
     *
     * @param mapName Name of the object to retrieve
     * @return
     * @throws ApiException General Exception
     * @throws ApiClassNotFoundException Class Not Found
     */
    public ApiObject getApiMappingObject(String company, String mapName) throws ApiException, ApiClassNotFoundException;

    public ApiMapping getApiMapping(String company, String sourceClass, String targetClass,
            String mapScript) throws ApiException, ApiClassNotFoundException;

    public ApiObject saveApiMapping(ApiObject obj) throws ApiException, ApiClassNotFoundException;

    public ApiObject saveApiModel(ApiObject obj) throws ApiException, ApiClassNotFoundException;

    public ApiObject saveApiService(ApiObject obj) throws ApiException, ApiClassNotFoundException;

    public ApiObject searchApiModel(ApiObject obj) throws ApiException;

    public ApiObject searchApiService(ApiObject obj) throws ApiException;

    public ApiObject searchApiMapping(ApiObject obj) throws ApiException;

    public boolean deleteApiModel(ApiObject obj) throws ApiException;

    public boolean deleteApiService(ApiObject obj) throws ApiException;

    public boolean deleteApiMapping(ApiObject obj) throws ApiException;

}
