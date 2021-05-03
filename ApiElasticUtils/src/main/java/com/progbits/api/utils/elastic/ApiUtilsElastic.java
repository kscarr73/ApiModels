package com.progbits.api.utils.elastic;

import com.progbits.api.ApiMapping;
import com.progbits.api.ParserService;
import com.progbits.api.WriterService;
import com.progbits.api.elastic.ApiElasticException;
import com.progbits.api.elastic.ElasticUtils;
import com.progbits.api.elastic.query.BoolQuery;
import com.progbits.api.elastic.query.EsSearch;
import com.progbits.api.elastic.query.MainQuery;
import com.progbits.api.elastic.query.RegExpQuery;
import com.progbits.api.elastic.query.TermQuery;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClass;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import com.progbits.api.utils.oth.ApiUtilsInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
@Component(name = "ApiUtils",
		immediate = true,
		property = {"name=ApiUtils"})
public class ApiUtilsElastic implements ApiUtilsInterface {

	private static final Logger LOG = LoggerFactory.getLogger(ApiUtilsElastic.class);

	private static final String API_MODELS = "apimodels";
	private static final String API_MAPPINGS = "apimappings";
	private static final String API_SERVICES = "apiservices";

	private ParserService _parser;
	private WriterService _writer;

	private ElasticUtils elasticUtils;
	private ApiMapping mappingFactory = null;

	@Reference
	public void setApiElasticUtils(ElasticUtils elasticUtils) {
		this.elasticUtils = elasticUtils;
	}

	@Override
	public void close() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
	@Override
	public void setMappingFactory(ApiMapping mapping) {
		this.mappingFactory = mapping;
	}

	public void unsetMappingFactory(ApiMapping mapping) {
		this.mappingFactory = null;
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
	public void retrievePackage(String thisClass, ApiClasses classes) throws ApiException, ApiClassNotFoundException {
		retrievePackage(thisClass, classes, true);
	}

	@Override
	public void retrievePackage(String thisClass, ApiClasses classes, boolean verify) throws ApiException, ApiClassNotFoundException {
		retrievePackage(thisClass, classes, verify, true);
	}

	@Override
	public void retrieveClasses(String thisClass, ApiClasses classes) throws ApiException, ApiClassNotFoundException {
		retrieveClasses(thisClass, classes, true);
	}

	@Override
	public void retrieveClasses(String thisClass, ApiClasses classes, boolean verify) throws ApiException, ApiClassNotFoundException {
		retrieveClasses(thisClass, classes, verify, true);
	}

	@Override
	public void retrieveClasses(String thisClass, ApiClasses classes, boolean verify, boolean localCopy) throws ApiException, ApiClassNotFoundException {
		ApiObject obj = classes.getClass(thisClass);

		// If the class already exists in the classes list exit
		if (obj != null) {
			return;
		}

		try {
			obj = retrieveClassElastic(thisClass, localCopy);
		} catch (Exception ex) {

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
								retrieveClasses(fld.getString("subType"),
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
	public void retrievePackage(String thisClass, ApiClasses classes, boolean verify, boolean localCopy) throws ApiException, ApiClassNotFoundException {
		List<String> lstPackage = null;

		try {
			lstPackage = retrievePackageElastic(thisClass);
		} catch (Exception ex) {
			LOG.error("retreivePackage", ex);
		}

		if (lstPackage != null) {
			for (String strClass : lstPackage) {
				retrieveClasses(strClass, classes, verify, localCopy);
			}
		}
	}

	public List<String> retrievePackageElastic(String thisClass) throws ApiException, ApiElasticException {
		List<String> lstPackage = new ArrayList<>();

		EsSearch search = new EsSearch();

		MainQuery mq = new MainQuery();

		RegExpQuery tq = new RegExpQuery("className",
				thisClass.toLowerCase() + ".[^.]*");

		mq.setQuery(tq);

		search.setQuery(mq);

		search.setStart(0);
		search.setCount(200);

		ApiObject rtnObject = elasticUtils.getSearchRecords(API_MODELS, null, search);

		if (rtnObject.isSet("hits")) {
			rtnObject.getList("hits").forEach((hit) -> {
				lstPackage.add(hit.getString("_source.className"));
			});
		}

		return lstPackage;
	}

	@Override
	public ApiObject retrieveServices(String serviceName, String access) throws ApiException, ApiClassNotFoundException {
		ApiObject retObj = new ApiObject();
		Map<String, ApiObject> retMap = null;

		try {
			retMap = getApiServicesElastic(serviceName, access);
		} catch (Exception ex) {

		}

		if (retMap != null) {
			retObj.getFields().putAll(retMap);
		}

		return retObj;
	}

	@Override
	public Map<String, ApiObject> getApiServices() throws ApiException, ApiClassNotFoundException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void getApiClasses(ApiObject apiService, ApiClasses classes) throws ApiException, ApiClassNotFoundException {
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
						retrieveClasses(inboundClassDef, classes);
					}

					if (classes.getClass(outboundClassDef) == null) {
						retrieveClasses(outboundClassDef, classes);
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
	public ApiMapping getApiMapping(String mapName) throws ApiException, ApiClassNotFoundException {
		ApiMapping resp = null;
		ApiObject obj = null;

		try {
			obj = getApiMappingElastic(mapName);
		} catch (Exception ex) {

		}

		if (obj != null) {
			try {
				String strSource = replaceImports(obj.
						getString("mapScript"));

				resp = getApiMapping(obj.getString("sourceClass"),
						obj.getString("targetClass"),
						strSource);
			} catch (ApiElasticException aee) {

			}
		}

		return resp;
	}

	@Override
	public ApiObject getApiMappingObject(String mapName) throws ApiException, ApiClassNotFoundException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ApiMapping getApiMapping(String sourceClass, String targetClass, String mapScript) throws ApiException, ApiClassNotFoundException {
		ApiMapping resp = mappingFactory.getClone();

		resp.setSourceClass(sourceClass);
		resp.setTargetClass(targetClass);
		resp.setScript(mapScript);

		ApiClasses inClasses = new ApiClasses();

		this.retrieveClasses(resp.getSourceClass(), inClasses);

		resp.setInModels(inClasses);

		ApiClasses outClasses = new ApiClasses();

		this.retrieveClasses(resp.getTargetClass(), outClasses);

		resp.setOutModels(outClasses);

		return resp;
	}

	public Map<String, ApiObject> getApiServicesElastic(String service,
			String access) throws ApiException, ApiElasticException {
		Map<String, ApiObject> retMap = new HashMap<>();

		EsSearch search = new EsSearch();
		MainQuery mq = new MainQuery();

		search.setQuery(mq);

		if (service != null || access != null) {
			BoolQuery bool = new BoolQuery();

			if (service != null) {
				bool.getMust().add(new RegExpQuery("name", service));
			}

			if (access != null) {
				bool.getMust().add(new TermQuery("accessLevel", access));
			}

			search.getQuery().setQuery(bool);
		}

		search.setCount(1000);

		ApiObject obj = elasticUtils.getSearchRecords(API_SERVICES, null, search);

		for (ApiObject ao : obj.getList("hits")) {
			ApiObject aoObj = ao.getObject("_source");

			aoObj.setString("_id", ao.getString("_id"));

			String url = aoObj.getString("url");

			if (!url.startsWith("/")) {
				url = "/" + url;
			}

			retMap.put(aoObj.getString("name"), aoObj);
		}

		return retMap;
	}

	private ApiObject retrieveClassElastic(String subject, boolean localCopy) throws ApiException, ApiElasticException {
		ApiObject obj = null;

		EsSearch search = new EsSearch();

		MainQuery mq = new MainQuery();

		TermQuery tq = new TermQuery("className", subject, false);

		mq.setQuery(tq);

		search.setQuery(mq);

		ApiObject rtnObject = elasticUtils.getSearchRecords(API_MODELS,
				null, search);

		ArrayList<ApiObject> hits = rtnObject.getList("hits");

		if (hits != null && hits.size() > 0) {
			obj = hits.get(0).getObject("_source");

			obj.setString("_id", hits.get(0).getString("_id"));
		}

		return obj;
	}

	public String replaceImports(String txtSource) throws ApiException, ApiElasticException {
		String strRet = null;

		boolean bContinue = true;

		while (bContinue) {
			int iStart = txtSource.indexOf("import '");

			if (iStart > -1) {
				int iEnd = txtSource.indexOf(";", iStart);

				if (iEnd > -1) {
					String tImport = txtSource.substring(iStart, iEnd + 1);
					String tName = tImport.substring(8, tImport.length() - 2);

					ApiObject objMap = getApiMappingElastic(tName.toString());

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

	public ApiObject getApiMappingElastic(String mapName) throws ApiException, ApiElasticException {
		ApiObject resp = null;

		EsSearch search = new EsSearch();
		search.getQuery().setQuery(new TermQuery("name", mapName));

		ApiObject retMap = elasticUtils.getSearchRecords(API_MAPPINGS, null, search);

		if (retMap != null && retMap.getLong("total.value") > 0) {
			resp = retMap.getObject("hits[0]._source");
		}

		return resp;
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

		try {
			elasticUtils.saveRecord(API_MAPPINGS, id, obj);
			return true;
		} catch (ApiElasticException ex) {
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

		try {
			elasticUtils.saveRecord(API_MODELS, id, obj);
			return true;
		} catch (ApiElasticException ex) {
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

		try {
			elasticUtils.saveRecord(API_SERVICES, id, obj);
			return true;
		} catch (ApiElasticException ex) {
			LOG.error("Save Record Error", ex);

			return false;
		}
	}

	public ElasticUtils getElasticUtils() {
		return elasticUtils;
	}

}
