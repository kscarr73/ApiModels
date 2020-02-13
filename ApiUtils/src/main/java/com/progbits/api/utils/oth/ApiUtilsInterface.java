package com.progbits.api.utils.oth;

import com.progbits.api.ParserService;
import com.progbits.api.WriterService;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.ApiMapping;
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

	public void retrieveClasses(String thisClass, ApiClasses classes) throws ApiException,ApiClassNotFoundException;

	public void retrieveClasses(String thisClass, ApiClasses classes,
			  boolean verify) throws ApiException, ApiClassNotFoundException;

	public void retrievePackage(String thisClass, ApiClasses classes) throws ApiException, ApiClassNotFoundException;

	public void retrievePackage(String thisClass, ApiClasses classes,
			  boolean verify) throws ApiException, ApiClassNotFoundException;

	public void retrieveClasses(String thisClass, ApiClasses classes,
			  boolean verify, boolean localCopy) throws ApiException, ApiClassNotFoundException;

	public void retrievePackage(String thisClass, ApiClasses classes,
			  boolean verify, boolean localCopy) throws ApiException, ApiClassNotFoundException;

	/**
	 * Returns a Object list of each service found
	 *
	 * @param serviceName The Name of the Service to locate, null if matches access level
	 * @param access Access level of the service, null, to access specific service
	 *
	 * @return
	 */
	public ApiObject retrieveServices(String serviceName, String access) throws ApiException, ApiClassNotFoundException;

	public Map<String, ApiObject> getApiServices() throws ApiException, ApiClassNotFoundException;

	public void getApiClasses(ApiObject apiService, ApiClasses classes) throws ApiException, ApiClassNotFoundException;

	/**
	 * Returns a Mapping using the Api Mapping Service
	 *
	 * @param mapName The Name of the Map to Return
	 * @return ThreadSafe Mapping Object for Mapping ApiObjects
	 */
	public ApiMapping getApiMapping(String mapName) throws ApiException, ApiClassNotFoundException;

	/**
	 * Retrieves the Api Mapping Object
	 *
	 * @param mapName Name of the object to retrieve
	 * @return
         * @throws ApiException
         * @throws ApiClassNotFoundException
	 */
	public ApiObject getApiMappingObject(String mapName) throws ApiException, ApiClassNotFoundException;

	public ApiMapping getApiMapping(String sourceClass, String targetClass,
			  String mapScript) throws ApiException, ApiClassNotFoundException;

	public Boolean saveApiMapping(ApiObject obj) throws ApiException, ApiClassNotFoundException;

	public Boolean saveApiModel(ApiObject obj) throws ApiException, ApiClassNotFoundException;

	public Boolean saveApiService(ApiObject obj) throws ApiException, ApiClassNotFoundException;
}
