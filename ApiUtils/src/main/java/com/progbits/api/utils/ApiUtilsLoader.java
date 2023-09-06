package com.progbits.api.utils;

import com.progbits.api.ApiMapping;
import com.progbits.api.ParserService;
import com.progbits.api.WriterService;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClass;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import com.progbits.api.model.ApiObjectDef;
import com.progbits.api.parser.JsonObjectParser;
import com.progbits.api.utils.oth.ApiUtilsInterface;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Uses Class Loader to pull classes and Mappings
 *
 * @author scarr
 */
public class ApiUtilsLoader implements ApiUtilsInterface {

    private ClassLoader _loader = null;
    private ParserService _parser;
    private WriterService _writer;
    private ApiMapping mappingFactory = null;

    private ApiClasses _defaultClasses = ApiObjectDef.returnClassDef();
    private JsonObjectParser _objectParser = new JsonObjectParser(true);

    private ScanResult _scanResult = null;
    
    public ApiUtilsLoader(ClassLoader loader, ParserService parser,
            WriterService writer) {
        _loader = loader;
        _parser = parser;
        _writer = writer;

        Map<String, String> props = new HashMap<>();
    }

    @Override
    public void close() {

    }

    public void setLoader(ClassLoader loader) {
        _loader = loader;
    }

    @Override
    public ParserService getParser() {
        return _parser;
    }

    @Override
    public WriterService getWriter() {
        return _writer;
    }

    @Override
    public void setMappingFactory(ApiMapping mapping) {
        mappingFactory = mapping;
    }

    @Override
    public void retrieveClasses(String company, String thisClass, ApiClasses classes) throws ApiException, ApiClassNotFoundException {
        retrieveClasses(company, thisClass, classes, true);
    }

    @Override
    public void retrieveClasses(String company, String thisClass, ApiClasses classes,
            boolean verify) throws ApiException, ApiClassNotFoundException {
        if (_scanResult == null) {
            _scanResult = new ClassGraph().disableNestedJarScanning().acceptPaths("models").scan();
        }
        
        String strFileName = thisClass;

        if (!strFileName.startsWith("models/")) {
            strFileName = "models/" + strFileName;
        }

        if (!thisClass.endsWith(".json")) {
            strFileName += ".json";
        }

        try {
            ResourceList resources = _scanResult.getResourcesWithPath(strFileName);
            
            if (!resources.isEmpty()) {
                ApiClass newClass = getClassFromFile(resources.get(0));

                classes.addClass(newClass);

                for (ApiObject fld : newClass.getList("fields")) {
                    if (fld.getString("subType") != null && !fld.
                            getString("subType").
                            isEmpty()) {
                        if (classes.getClass(fld.getString("subType")) == null) {
                            retrieveClasses(company, fld.getString("subType"), classes);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new ApiException(ex.getMessage(), ex);
        }
    }

    @Override
    public void retrievePackage(String company, String thisClass, ApiClasses classes) throws ApiException, ApiClassNotFoundException {
        retrievePackage(company, thisClass, classes, true);
    }

    @Override
    public void retrievePackage(String company, String thisClass, ApiClasses classes,
            boolean verify) throws ApiException, ApiClassNotFoundException {
        if (_scanResult == null) {
            _scanResult = new ClassGraph().disableNestedJarScanning().acceptPaths("models").scan();
        }
        
        ResourceList resources = _scanResult.getResourcesMatchingWildcard("models/" + thisClass + ".json");
        
        for (var parsedFileName : resources) {
            retrieveClasses(company, parsedFileName.getPath(), classes);
        }
    }

    @Override
    public Map<String, ApiObject> getApiServices(String company) throws ApiException, ApiClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiObject retrieveServices(String company, String serviceName, String access) throws ApiException, ApiClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getApiClasses(String company, ApiObject apiService, ApiClasses classes) throws ApiException, ApiClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiObject getApiMappingObject(String company, String mapName) throws ApiException, ApiClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiMapping getApiMapping(String company, String mapName) throws ApiException, ApiClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiMapping getApiMapping(String company, String sourceClass, String targetClass,
            String mapScript) throws ApiException, ApiClassNotFoundException {
        if (mappingFactory != null) {
            ApiMapping resp = mappingFactory.getClone();

            resp.setSourceClass(sourceClass);
            resp.setTargetClass(targetClass);
            resp.setScript(mapScript);

            ApiClasses inClasses = new ApiClasses();

            this.retrieveClasses(company, resp.getSourceClass(), inClasses);

            resp.setInModels(inClasses);

            ApiClasses outClasses = new ApiClasses();

            this.retrieveClasses(company, resp.getTargetClass(), outClasses);

            resp.setOutModels(outClasses);

            return resp;
        } else {
            return null;
        }
    }

    private ApiClass getClassFromFile(Resource file) throws ApiException {
        try {
            String salesJson = file.getContentAsString();

            ApiClasses apiClasses = new ApiClasses();

            ApiObject obj = _objectParser.parseSingle(new StringReader(salesJson));
            obj.setApiClasses(_defaultClasses);
            obj.setApiClass(_defaultClasses.getClass("com.icg.isg.api.ApiClass"));

            ApiClass cldObj = new ApiClass(obj);

            return cldObj;
        } catch (ApiClassNotFoundException | IOException ex) {
            throw new ApiException(550, ex.getMessage());
        }
    }

    @Override
    public void retrieveClasses(String company, String thisClass, ApiClasses classes,
            boolean verify, boolean localCopy) throws ApiException, ApiClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void retrievePackage(String company, String thisClass, ApiClasses classes,
            boolean verify, boolean localCopy) throws ApiException, ApiClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiObject saveApiMapping(ApiObject obj) throws ApiException, ApiClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiObject saveApiModel(ApiObject obj) throws ApiException, ApiClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiObject saveApiService(ApiObject obj) throws ApiException, ApiClassNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiObject searchApiMapping(ApiObject obj) throws ApiException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiObject searchApiModel(ApiObject obj) throws ApiException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiObject searchApiService(ApiObject obj) throws ApiException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean deleteApiMapping(ApiObject obj) throws ApiException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean deleteApiModel(ApiObject obj) throws ApiException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean deleteApiService(ApiObject obj) throws ApiException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
