package com.progbits.api.utils.db;

import com.progbits.api.ApiMapping;
import com.progbits.api.ParserService;
import com.progbits.api.WriterService;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClass;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import com.progbits.api.parser.JsonObjectParser;
import com.progbits.api.utils.oth.ApiUtilsInterface;
import com.progbits.api.writer.JsonObjectWriter;
import com.progbits.db.SsDbObjects;
import com.progbits.db.SsDbUtils;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
@Component(name = "ApiUtilsDb",
		immediate = true,
		property = {"name=ApiUtilsDb"},
		configurationPolicy = ConfigurationPolicy.REQUIRE)
public class ApiUtilsDb implements ApiUtilsInterface {

	private static final Logger LOG = LoggerFactory.getLogger(ApiUtilsDb.class);

	private ApiObject config = null;

	private static final String API_MODELS = "sm_apiModels";
	private static final String API_MAPPINGS = "sm_apiMappings";
	private static final String API_SERVICES = "sm_apiServices";

	private ParserService _parser;
	private WriterService _writer;

	private final JsonObjectParser _jsonParser = new JsonObjectParser(true);
	private final JsonObjectWriter _jsonWriter = new JsonObjectWriter(true);

	private ApiMapping _mappingFactory = null;

	private Map<String, DataSource> _dataSources = new HashMap<>();

	private DataSource _ds = null;

	@Activate
	public void setup(Map<String, String> params) {
		modified(params);
	}

	@Modified
	public void modified(Map<String, String> params) {
		config = new ApiObject();
		config.putAll(params);

		if (config.isSet("dataSource")) {
			_ds = _dataSources.get(config.getString("dataSource"));
		}
	}

	@Override
	public void close() {
		// Nothing to report here
	}

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
	@Override
	public void setMappingFactory(ApiMapping mapping) {
		_mappingFactory = mapping;
	}

	public void unsetMappingFactory(ApiMapping mapping) {
		_mappingFactory = null;
	}

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
	public void setDataSource(DataSource ds, Map<String, String> props) {
		_dataSources.put(props.get("name"), ds);
	}

	public void unsetDataSource(DataSource ds, Map<String, String> props) {
		_dataSources.remove(props.get("name"));
	}

	@Reference
	public void setParser(ParserService parser) {
		_parser = parser;
	}

	@Reference
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
	public void retrievePackage(String company, String thisClass, ApiClasses classes) throws ApiException, ApiClassNotFoundException {
		retrievePackage(company, thisClass, classes, true);
	}

	@Override
	public void retrievePackage(String company, String thisClass, ApiClasses classes, boolean verify) throws ApiException, ApiClassNotFoundException {
		retrievePackage(company, thisClass, classes, verify, true);
	}

	@Override
	public void retrieveClasses(String company, String thisClass, ApiClasses classes) throws ApiException, ApiClassNotFoundException {
		retrieveClasses(company, thisClass, classes, true);
	}

	@Override
	public void retrieveClasses(String company, String thisClass, ApiClasses classes, boolean verify) throws ApiException, ApiClassNotFoundException {
		retrieveClasses(company, thisClass, classes, verify, true);
	}

	@Override
	public void retrieveClasses(String company, String thisClass, ApiClasses classes, boolean verify, boolean localCopy) throws ApiException, ApiClassNotFoundException {
		ApiObject obj = classes.getClass(thisClass);

		// If the class already exists in the classes list exit
		if (obj != null) {
			return;
		}

		try {
			obj = retrieveClassDb(company, thisClass, localCopy);
		} catch (Exception ex) {
			LOG.error("retrieveClasses", ex);
		}

		if (obj != null) {
			ApiClass cls = new ApiClass(obj);

			cls.setName("apiClass");
			cls.setApiClass(ApiObject.returnClassDef().getClass(
					"com.icg.isg.api.ApiClass"));

			classes.addClass(cls);

			if (cls.isSet("fields")) {
				for (ApiObject fld : cls.getList("fields")) {
					fld.setName("ApiField");
					fld.setApiClass(ApiObject.returnClassDef().getClass(
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
						"Class: " + thisClass + " Fields Not Set",
						null);
			}
		}

		if (verify) {
			verifyClasses(thisClass, classes);
		}
	}

	@Override
	public void retrievePackage(String company, String thisClass, ApiClasses classes, boolean verify, boolean localCopy) throws ApiException, ApiClassNotFoundException {
		List<String> lstPackage = null;

		try {
			lstPackage = retrievePackageDb(company, thisClass);
		} catch (Exception ex) {
			LOG.error("retreivePackage", ex);
		}

		if (lstPackage != null) {
			for (String strClass : lstPackage) {
				retrieveClasses(company, strClass, classes, verify, localCopy);
			}
		}
	}

	public List<String> retrievePackageDb(String company, String thisClass) throws ApiException {
		List<String> lstPackage = new ArrayList<>();

		ApiObject objSearch = new ApiObject();

		ApiObject likeObj = new ApiObject();

		likeObj.setString("$like", thisClass + "%");
		objSearch.setObject("className", likeObj);

		objSearch.setString("company", company);

		ApiObject rtnObject = searchApiModel(objSearch);

		if (rtnObject.containsKey("root")) {
			for (ApiObject obj : rtnObject.getList("root")) {
				lstPackage.add(obj.getString("className"));
			}
		}

		return lstPackage;
	}

	@Override
	public ApiObject retrieveServices(String company, String serviceName, String access) throws ApiException, ApiClassNotFoundException {
		ApiObject retObj = new ApiObject();
		Map<String, ApiObject> retMap = null;

		try {
			retMap = getApiServicesDb(company, serviceName, access);
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
		}

		if (retMap != null) {
			retObj.getFields().putAll(retMap);
		}

		return retObj;
	}

	@Override
	public Map<String, ApiObject> getApiServices(String company) throws ApiException, ApiClassNotFoundException {
		return getApiServicesDb(company, null, null);
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

				//exceptionClassDef = XsdTransform.DEFAULT_EXCEPTION;
				try {
					if (classes.getClass(inboundClassDef) == null) {
						retrieveClasses(company, inboundClassDef, classes);
					}

					if (classes.getClass(outboundClassDef) == null) {
						retrieveClasses(company, outboundClassDef, classes);
					}

//                    if (classes.getClass(exceptionClassDef) == null) {
//                        retrieveClasses(exceptionClassDef, classes);
//                    }
				} catch (ApiClassNotFoundException | ApiException ex) {
					LOG.error(
							"getApiClasses: " + apiService.getString("name")
							+ " Operation: " + funcName,
							ex);
				}
			});
		}
	}

	@Override
	public ApiMapping getApiMapping(String company, String mapName) throws ApiException, ApiClassNotFoundException {
		ApiMapping resp = null;

		ApiObject obj = getApiMappingObject(company, mapName);

		if (obj != null) {
			String strSource = replaceImports(company, obj.getString("mapScript"));

			resp = getApiMapping(company, obj.getString("sourceClass"),
					obj.getString("targetClass"),
					strSource);

		}

		return resp;
	}

	@Override
	public ApiObject getApiMappingObject(String company, String mapName) throws ApiException, ApiClassNotFoundException {
		ApiObject searchObj = new ApiObject();

		searchObj.setString("mapName", mapName);

		ApiObject retObj = searchApiMapping(searchObj);

		if (retObj.isSet("root")) {
			return retObj.getList("root").get(0);
		} else {
			return null;
		}
	}

	@Override
	public ApiMapping getApiMapping(String company, String sourceClass, String targetClass, String mapScript) throws ApiException, ApiClassNotFoundException {
		ApiMapping resp = _mappingFactory.getClone();

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
	}

	public Map<String, ApiObject> getApiServicesDb(String company, String service,
			String access) throws ApiException, ApiClassNotFoundException {
		Map<String, ApiObject> retMap = new HashMap<>();

		ApiObject objSearch = new ApiObject();

		if (service != null) {
			objSearch.setString("serviceName", service);
		}

		ApiObject rtnSql = searchApiService(objSearch);

		if (rtnSql.containsKey("root")) {
			for (ApiObject row : rtnSql.getList("root")) {
				retMap.put(row.getString("serviceName"), row);
			}
		}

		return retMap;
	}

	private ApiObject retrieveClassDb(String company, String subject, boolean localCopy) throws ApiException, ApiClassNotFoundException {
		ApiObject obj = null;

		ApiObject objSearch = new ApiObject();
		objSearch.setString("className", subject);
		objSearch.setString("company", company);

		ApiObject rtnSql = searchApiModel(objSearch);

		if (rtnSql.containsKey("root")) {
			obj = rtnSql.getList("root").get(0);
		}

		return obj;
	}

	public String replaceImports(String company, String txtSource) throws ApiException, ApiClassNotFoundException {
		String strRet = null;

		boolean bContinue = true;

		while (bContinue) {
			int iStart = txtSource.indexOf("import '");

			if (iStart > -1) {
				int iEnd = txtSource.indexOf(";", iStart);

				if (iEnd > -1) {
					String tImport = txtSource.substring(iStart, iEnd + 1);
					String tName = tImport.substring(8, tImport.length() - 2);

					ApiObject objMap = getApiMappingObject(company, tName.toString());

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
	 * Verify that all Classes are in the array for use.
	 *
	 * @param thisClass
	 * @param classes
	 * @throws ICGSystemException
	 */
	private void verifyClasses(String thisClass, ApiClasses classes) throws ApiException {
		ApiClass cls = classes.getClass(thisClass);

		if (cls == null) {
			throw new ApiException("Class: " + thisClass + " NOT FOUND", null);
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
	public Boolean saveApiMapping(ApiObject obj) throws ApiException, ApiClassNotFoundException {
		String id = obj.getString("_id");

		if (id != null) {
			obj.remove("_id");
		}

		try (Connection conn = _ds.getConnection()) {
			ApiObject saveObj = new ApiObject();

			if (id != null) {
				saveObj.setInteger("id", Integer.parseInt(id));
			}

			if (obj.containsKey("company")) {
				saveObj.setString("company", obj.getString("company"));
			}

			saveObj.setDateTime("lastUpdated", OffsetDateTime.now());
			saveObj.setString("jsonObject", _jsonWriter.writeSingle(obj));

			SsDbObjects.upsertWithInteger(conn, API_MAPPINGS, "id", saveObj);

			return true;
		} catch (SQLException ex) {
			LOG.error("Save Record Error", ex);

			return false;
		}
	}

	@Override
	public Boolean saveApiModel(ApiObject obj) throws ApiException, ApiClassNotFoundException {
		String id = obj.getString("_id");

		if (id != null) {
			obj.remove("_id");
		}

		try (Connection conn = _ds.getConnection()) {
			ApiObject saveObj = new ApiObject();

			if (id != null) {
				saveObj.setInteger("id", Integer.parseInt(id));
			}

			if (obj.containsKey("company")) {
				saveObj.setString("company", obj.getString("company"));
			}

			saveObj.setDateTime("lastUpdated", OffsetDateTime.now());

			saveObj.setString("jsonObject", _jsonWriter.writeSingle(obj));

			SsDbObjects.upsertWithInteger(conn, API_MODELS, "id", saveObj);

			return true;
		} catch (SQLException ex) {
			LOG.error("Save Record Error", ex);

			return false;
		}
	}

	@Override
	public Boolean saveApiService(ApiObject obj) throws ApiException, ApiClassNotFoundException {
		String id = obj.getString("_id");

		if (id != null) {
			obj.remove("_id");
		}

		if (obj.containsKey("_type")) {
			obj.remove("_type");
		}

		try (Connection conn = _ds.getConnection()) {
			ApiObject saveObj = new ApiObject();

			if (id != null) {
				saveObj.setInteger("id", Integer.parseInt(id));
			}

			if (obj.containsKey("company")) {
				saveObj.setString("company", obj.getString("company"));
			}

			saveObj.setDateTime("lastUpdated", OffsetDateTime.now());
			saveObj.setString("jsonObject", _jsonWriter.writeSingle(obj));

			SsDbObjects.upsertWithInteger(conn, API_SERVICES, "id", saveObj);

			return true;
		} catch (SQLException ex) {
			LOG.error("Save Record Error", ex);

			return false;
		}
	}

	@Override
	public ApiObject searchApiMapping(ApiObject obj) throws ApiException {
		obj.setString("tableName", API_MAPPINGS);

		ApiObject objRet = new ApiObject();
		objRet.setArrayList("root", new ArrayList<>());

		try (Connection conn = _ds.getConnection()) {
			if (obj.containsKey("_id")) {
				obj.setInteger("id", Integer.parseInt(obj.getString("_id")));
				obj.remove("_id");
			}
			
			ApiObject sqlRet = SsDbObjects.find(conn, obj);

			if (sqlRet.isSet("root")) {
				for (ApiObject row : sqlRet.getList("root")) {
					try {
						ApiObject rowObj = _jsonParser.parseSingle(new StringReader(row.getString("jsonObject")));

						rowObj.setString("_id", String.valueOf(row.getInteger("id")));

						objRet.getList("root").add(rowObj);
					} catch (ApiClassNotFoundException cex) {
						
					}
				}
			}
			return objRet;
		} catch (SQLException ex) {
			throw new ApiException("SQL Exception", ex);
		}
	}

	@Override
	public ApiObject searchApiModel(ApiObject obj) throws ApiException {
		obj.setString("tableName", API_MODELS);

		ApiObject objRet = new ApiObject();
		objRet.setArrayList("root", new ArrayList<>());

		try (Connection conn = _ds.getConnection()) {
			if (obj.containsKey("_id")) {
				obj.setInteger("id", Integer.parseInt(obj.getString("_id")));
				obj.remove("_id");
			}
			
			ApiObject sqlRet = SsDbObjects.find(conn, obj);

			if (sqlRet.isSet("root")) {
				for (ApiObject row : sqlRet.getList("root")) {
					try {
						ApiObject rowObj = _jsonParser.parseSingle(new StringReader(row.getString("jsonObject")));

						rowObj.setString("_id", String.valueOf(row.getInteger("id")));

						objRet.getList("root").add(rowObj);
					} catch (ApiClassNotFoundException cex) {

					}
				}
			}
			return objRet;
		} catch (SQLException ex) {
			throw new ApiException("SQL Exception", ex);
		}
	}

	@Override
	public ApiObject searchApiService(ApiObject obj) throws ApiException {
		obj.setString("tableName", API_SERVICES);
		ApiObject objRet = new ApiObject();
		objRet.setArrayList("root", new ArrayList<>());

		try (Connection conn = _ds.getConnection()) {
			if (obj.containsKey("_id")) {
				obj.setInteger("id", Integer.parseInt(obj.getString("_id")));
				obj.remove("_id");
			}
			
			ApiObject sqlRet = SsDbObjects.find(conn, obj);

			if (sqlRet.isSet("root")) {
				for (ApiObject row : sqlRet.getList("root")) {
					try {
						ApiObject rowObj = _jsonParser.parseSingle(new StringReader(row.getString("jsonObject")));

						rowObj.setString("_id", String.valueOf(row.getInteger("id")));

						objRet.getList("root").add(rowObj);
					} catch (ApiClassNotFoundException cex) {

					}
				}
			}
			return objRet;
		} catch (SQLException ex) {
			throw new ApiException("SQL Exception", ex);
		}
	}

	@Override
	public boolean deleteApiMapping(ApiObject obj) throws ApiException {
		if (!obj.containsKey("_id")) {
			throw new ApiException("_id is REQURIED");
		}

		try (Connection conn = _ds.getConnection()) {
			String url = "DELETE FROM " + API_MAPPINGS + " WHERE id=:_id";

			SsDbUtils.updateObject(conn, url, obj);
		} catch (SQLException ex) {
			throw new ApiException("SQL Exception", ex);
		}

		return true;
	}

	@Override
	public boolean deleteApiModel(ApiObject obj) throws ApiException {
		if (!obj.containsKey("_id")) {
			throw new ApiException("_id is REQURIED");
		}

		try (Connection conn = _ds.getConnection()) {
			String url = "DELETE FROM " + API_MODELS + " WHERE id=:_id";

			SsDbUtils.updateObject(conn, url, obj);
		} catch (SQLException ex) {
			throw new ApiException("SQL Exception", ex);
		}

		return true;
	}

	@Override
	public boolean deleteApiService(ApiObject obj) throws ApiException {
		if (!obj.containsKey("_id")) {
			throw new ApiException("_id is REQURIED");
		}

		try (Connection conn = _ds.getConnection()) {
			String url = "DELETE FROM " + API_SERVICES + " WHERE id=:_id";

			SsDbUtils.updateObject(conn, url, obj);
		} catch (SQLException ex) {
			throw new ApiException("SQL Exception", ex);
		}

		return true;
	}

}
