/**
 * <b>(c) 2016 ProgBits. All rights reserved </b>
 */
package com.progbits.api.utils;

import com.progbits.api.ApiMapping;
import com.progbits.api.ObjectParser;
import com.progbits.api.ParserService;
import com.progbits.api.WriterService;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClass;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import com.progbits.api.model.ApiObjectDef;
import com.progbits.api.transforms.Transform;
import com.progbits.api.transforms.XsdTransform;
import com.progbits.api.utils.oth.ApiUtilsInterface;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses File Access to pull ApiUtils from a System
 *
 * @author scarr
 */
public class ApiUtils implements ApiUtilsInterface {

    private final Logger LOG = LoggerFactory.getLogger(ApiUtils.class);

    private ObjectParser _jsonParser = null;

    private String _esLocation;
    private String _esVersion;
    private ParserService _parser;
    private WriterService _writer;
    private Path _workLocation;
    private ApiMapping mappingFactory = null;

    private Map<String, String> configs = new HashMap<>();

    public String getLocation() {
        return _esLocation;
    }

    public String getVersion() {
        return _esVersion;
    }

    @Override
    public void setMappingFactory(ApiMapping mapping) {
        this.mappingFactory = mapping;
    }

    public void unsetMappingFactory(ApiMapping mapping) {
        this.mappingFactory = null;
    }

    public void setup() {
        if (configs != null) {
            try {
                _workLocation = Paths.get(configs.get("WorkLocation"));

                if (!Files.exists(_workLocation)) {
                    Files.createDirectories(_workLocation);
                }
            } catch (Exception ex) {
                _workLocation = Paths.get(
                        System.getProperty("user.home") + "/work");

                if (!Files.exists(_workLocation)) {
                    try {
                        Files.createDirectories(_workLocation);
                    } catch (Exception ex2) {

                    }
                }
            }
        }

        try {
            _jsonParser = _parser.getParser("JSON");
            _jsonParser.init(null, null, null, null);
        } catch (Exception app) {

        }
    }

    @Override
    public void close() {

    }

    public void setLocation(String loc) {
        _esLocation = loc;
    }

    public void setVersion(String ver) {
        _esVersion = ver;
    }

    public void setParser(ParserService parser) {
        _parser = parser;
    }

    public void setWriter(WriterService writer) {
        _writer = writer;
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
    public ApiObject retrieveServices(String company, String serviceName, String access) throws ApiException, ApiClassNotFoundException {
        ApiObject retObj = new ApiObject();

        Map<String, ApiObject> retMap = getApiServicesFiles(serviceName, access);

        if (retMap != null) {
            retObj.getFields().putAll(retMap);
        }

        return retObj;
    }

    @Override
    public Map<String, ApiObject> getApiServices(String company) throws ApiException, ApiClassNotFoundException {
        Map<String, ApiObject> retMap = getApiServicesFiles(null, null);

        return retMap;
    }

    public Map<String, ApiObject> getApiServicesFiles(String service,
            String access) throws ApiException, ApiClassNotFoundException {
        Map<String, ApiObject> retMap = new HashMap<>();

        if (_workLocation != null) {
            Path path = _workLocation.resolve("api/apiservices/");

            try ( DirectoryStream<Path> directoryStream = Files.
                    newDirectoryStream(path)) {
                for (Path tstPath : directoryStream) {
                    String strContents = new String(Files.readAllBytes(tstPath));

                    ApiObject obj = _jsonParser.parseSingle(new StringReader(
                            strContents));

                    String serviceName = obj.getString(("name"));

                    boolean addRec = true;

                    if (service != null || access != null) {
                        addRec = true;

                        if (service != null) {
                            if (!service.equals(obj.getString("name"))) {
                                addRec = false;
                            }
                        }

                        if (access != null && addRec) {
                            if (!access.equals(obj.getString("accessLevel"))) {
                                addRec = false;
                            }
                        }
                    }

                    if (addRec) {
                        retMap.put(serviceName, obj);
                    }
                }
            } catch (IOException ex) {
                throw new ApiException("getApiServicesFiles", ex);
            }
        }

        return retMap;
    }

    @Override
    public void getApiClasses(String company, ApiObject apiService, ApiClasses classes) throws ApiException, ApiClassNotFoundException {
        if (apiService.isSet("functions")) {
            apiService.getList("functions").forEach((func) -> {
                String funcName = func.getString("name");
                String packageName = apiService.getString("packageName");
                String inboundClassDef;
                String outboundClassDef;
                String exceptionClassDef;

                if (func.isSet("request")) {
                    inboundClassDef = func.getString("request");
                } else {
                    inboundClassDef = packageName + "." + funcName.
                            substring(0, 1).
                            toUpperCase() + funcName.substring(1);

                    func.setString("request", inboundClassDef);
                }

                if (func.isSet("response")) {
                    outboundClassDef = func.getString("response");
                } else {
                    outboundClassDef = inboundClassDef + "Response";

                    func.setString("response", outboundClassDef);
                }

                exceptionClassDef = XsdTransform.DEFAULT_EXCEPTION;

                try {
                    if (classes.getClass(inboundClassDef) == null) {
                        retrieveClasses(company, inboundClassDef, classes);
                    }

                    if (classes.getClass(outboundClassDef) == null) {
                        retrieveClasses(company, outboundClassDef, classes);
                    }

                    if (classes.getClass(exceptionClassDef) == null) {
                        retrieveClasses(company, exceptionClassDef, classes);
                    }
                } catch (ApiException | ApiClassNotFoundException ex) {
                    LOG.error(
                            "getApiClasses: " + apiService.getString("name")
                            + " Operation: " + funcName,
                            ex);
                }
            });
        }
    }

    @Override
    public void retrievePackage(String company, String thisClass, ApiClasses classes) throws ApiException, ApiClassNotFoundException {
        retrievePackage(company, thisClass, classes, true);
    }

    @Override
    public void retrievePackage(String company, String thisClass, ApiClasses classes,
            boolean verify) throws ApiException, ApiClassNotFoundException {
        retrievePackage(company, thisClass, classes, verify, true);
    }

    @Override
    public void retrievePackage(String company, String thisClass, ApiClasses classes,
            boolean verify, boolean localCopy) throws ApiException, ApiClassNotFoundException {
        List<String> lstPackage = retrievePackageFiles(thisClass);

        if (lstPackage != null) {
            for (String strClass : lstPackage) {
                retrieveClasses(company, strClass, classes, verify, localCopy);
            }
        }
    }

    public List<String> retrievePackageFiles(String thisClass) throws ApiException, ApiClassNotFoundException {
        List<String> lstPackage = new ArrayList<>();

        if (_workLocation != null) {
            Path path = _workLocation.resolve("api/apimodels/");
            Pattern pattern = Pattern.compile(thisClass + ".[^.]*.json");

            try ( DirectoryStream<Path> directoryStream = Files.
                    newDirectoryStream(path)) {
                for (Path tstPath : directoryStream) {
                    if (pattern.matcher(tstPath.getFileName().toString()).
                            matches()) {
                        lstPackage.add(tstPath.getFileName().toString().replace(".json", ""));
                    }
                }
            } catch (IOException ex) {

            }
        }

        return lstPackage;
    }

    @Override
    public void retrieveClasses(String company, String thisClass, ApiClasses classes) throws ApiException, ApiClassNotFoundException {
        retrieveClasses(company, thisClass, classes, true);
    }

    @Override
    public void retrieveClasses(String company, String thisClass, ApiClasses classes,
            boolean verify) throws ApiException, ApiClassNotFoundException {
        retrieveClasses(company, thisClass, classes, verify, true);
    }

    @Override
    public void retrieveClasses(String company, String thisClass, ApiClasses classes,
            boolean verify, boolean localCopy) throws ApiException, ApiClassNotFoundException {
        ApiObject obj = classes.getClass(thisClass);

        // If the class already exists in the classes list exit
        if (obj != null) {
            return;
        }

        obj = getApiFile(thisClass, "apimodels");

        if (obj != null) {
            ApiClass cls = new ApiClass(obj);

            cls.setName("apiClass");
            cls.setApiClass(ApiObjectDef.returnClassDef().getClass(
                    "com.icg.isg.api.ApiClass"));

            classes.addClass(cls);

            if (cls.isSet("fields")) {
                for (ApiObject fld : cls.getList("fields")) {
                    fld.setName("ApiField");
                    fld.setApiClass(ApiObjectDef.returnClassDef().getClass(
                            "com.icg.isg.api.ApiField"));

                    if (fld.getString("subType") != null && !fld.getString(
                            "subType").
                            isEmpty()) {
                        if (classes.getClass(fld.getString("subType")) == null) {
                            try {
                                retrieveClasses(company, fld.getString("subType"),
                                        classes, verify,
                                        localCopy);
                            } catch (Exception ex) {
                                throw ex;
                            }
                        }
                    }
                }
            } else {
                throw new ApiException(
                        "Class: " + thisClass + " Fields Not Set", null);
            }
        }

        if (verify) {
            verifyClasses(thisClass, classes);
        }
    }

    /**
     * Verify that all Classes are in the array for use.
     *
     * @param thisClass
     * @param classes
     * @throws ApiException
     * @throws ApiClassNotFoundException
     */
    private void verifyClasses(String thisClass, ApiClasses classes) throws ApiException, ApiClassNotFoundException {
        ApiClass cls = classes.getClass(thisClass);

        if (cls == null) {
            throw new ApiException("Class: " + thisClass + " NOT FOUND",
                    null);
        } else {
            if (cls.isSet("fields")) {
                for (ApiObject fld : cls.getList("fields")) {
                    if ("Object".equalsIgnoreCase(fld.getString("type")) || "ArrayList".
                            equalsIgnoreCase(fld.getString("type"))) {
                        ApiClass clsSub = classes.getClass(fld.getString(
                                "subType"));

                        if (clsSub == null) {
                            throw new ApiException(
                                    "Test Class: " + thisClass + " Sub Class: " + fld.
                                            getString("subType") + " Does Not Exist",
                                    null);
                        }
                    }
                }
            }
        }

    }

    @Override
    public ApiObject getApiMappingObject(String company, String mapName) throws ApiException, ApiClassNotFoundException {
        return getApiFile(mapName, "apimappings");
    }

    @Override
    public ApiMapping getApiMapping(String company, String mapName) throws ApiException, ApiClassNotFoundException {
        ApiMapping resp = null;
        ApiObject obj = getApiFile(mapName, "apimappings");

        if (obj != null) {
            String strSource = replaceImports(obj.
                    getString("mapScript"));

            resp = getApiMapping(company, obj.getString("sourceClass"),
                    obj.getString("targetClass"),
                    strSource);
        }

        return resp;
    }

    public String replaceImports(String txtSource) throws ApiException, ApiClassNotFoundException {
        String strRet = null;

        boolean bContinue = true;

        while (bContinue) {
            int iStart = txtSource.indexOf("import '");

            if (iStart > -1) {
                int iEnd = txtSource.indexOf(";", iStart);

                if (iEnd > -1) {
                    String tImport = txtSource.substring(iStart, iEnd + 1);
                    String tName = tImport.substring(8, tImport.length() - 2);

                    ApiObject objMap = getApiFile(tName.toString(),
                            "apimappings");

                    if (objMap != null) {
                        txtSource = txtSource.
                                replace(tImport, objMap.getString("mapScript"));
                    } else {
                        txtSource = txtSource.replace(tImport,
                                "// Map Not Found");
                    }

                }
            } else {
                bContinue = false;
            }
        }

        strRet = txtSource.toString();

        return strRet;
    }

    /**
     * Retrieve Api File from the File Cache.
     *
     * @param fileName Name of the file to retrieve
     * @param type apimappings, apiclasses, webservices
     * @return ApiObject
     * @throws ApiException Api Exception
     * @throws ApiClassNotFoundException Api Class Not Found
     */
    public ApiObject getApiFile(String fileName, String type) throws ApiException, ApiClassNotFoundException {
        ApiObject resp = null;

        if (_workLocation != null) {
            String locFileName = fileName;

            if (!locFileName.endsWith(".json")) {
                locFileName += ".json";
            }

            Path path = _workLocation.resolve(
                    "api/" + type + "/" + locFileName);

            try {
                if (Files.exists(path)) {
                    String strContents = new String(Files.readAllBytes(path));

                    resp = Transform.
                            toApiObject(_parser.getParser("JSON"), null, null,
                                    strContents);
                }
            } catch (ApiException | ApiClassNotFoundException | IOException iex) {

            }
        }

        return resp;
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
