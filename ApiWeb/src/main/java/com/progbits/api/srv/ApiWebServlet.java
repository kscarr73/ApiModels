package com.progbits.api.srv;

import com.progbits.api.ObjectParser;
import com.progbits.api.ObjectWriter;
import com.progbits.api.auth.Authenticate;
import com.progbits.api.auth.SendEmails;
import com.progbits.api.elastic.ElasticUtils;
import com.progbits.api.elastic.query.BoolQuery;
import com.progbits.api.elastic.query.EsSearch;
import com.progbits.api.elastic.query.RegExpQuery;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import com.progbits.api.parseimport.ImportParser;
import com.progbits.api.srv.formatter.DocTransform;
import com.progbits.api.srv.formatter.ElasticSearchV5Formatter;
import com.progbits.api.srv.formatter.FddFormatter;
import com.progbits.api.srv.formatter.PlantUmlFormatter;
import com.progbits.api.srv.formatter.RamlFormatter;
import com.progbits.api.transforms.Transform;
import com.progbits.api.transforms.XsdTransform;
import com.progbits.api.utils.ApiUtils;
import com.progbits.api.utils.oth.ApiUtilsInterface;
import com.progbits.util.http.HttpUtils;
import com.progbits.web.UrlEntry;
import com.progbits.web.WebUtils;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jdk.jfr.StackTrace;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
@Component(name = "ApiWebServlet", immediate = true,
		configurationPolicy = ConfigurationPolicy.REQUIRE,
		property = {"name=ApiWebServlet", "alias=/apiweb"},
		service = {HttpServlet.class})
public class ApiWebServlet extends HttpServlet {

	private static Logger log = LoggerFactory.getLogger(ApiWebServlet.class);

	private static String servletRoot = "/apiweb";

	private Map<String, String> _params;

	private static SendEmails _sendEmails = new SendEmails();

	private static Long lastRun = 0L;
	private static Configuration _fm = null;
	private static ApiUtilsInterface _apiUtils = null;

	private static Map<String, ApiUtils> mapUtils = new LinkedHashMap<>();

	private ElasticUtils _elasticUtils;

	private ObjectParser jsonParser = null;
	private ObjectWriter jsonWriter = null;

	public LinkedHashMap<String, ImportParser> importParsers = new LinkedHashMap<>();

	private Authenticate authenticate;

	DefaultObjectWrapperBuilder objWrap = new DefaultObjectWrapperBuilder(
			Configuration.VERSION_2_3_30);

	public enum TABLE_SQL {
		ApiModels, ApiMappings, ApiWebServices,
		className, modelJson,
		mapName, mapJson,
		serviceName, serviceJson
	};

	public ApiUtilsInterface getApiUtils() {
		return _apiUtils;
	}

	@Reference
	public void setAuthenticate(Authenticate authenticate) {
		this.authenticate = authenticate;
	}

	@Reference
	public void setApiUtils(ApiUtilsInterface api) {
		_apiUtils = api;
	}

	@Reference
	public void setElasticUtils(ElasticUtils elasticUtils) {
		_elasticUtils = elasticUtils;
	}

	@Activate
	public void startup(Map<String, String> params) {
		_params = params;
		
		_sendEmails.configure(params);
		
		_fm = new Configuration(Configuration.VERSION_2_3_25);

		_fm.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);

		try {
			if (_params.containsKey("Local_Templates")) {
				_fm.setTemplateLoader(
						new FileTemplateLoader(new File("/home/scarr/Projects/ApiModels/ApiWeb/src/main/resources/fm")
						));
			} else {
				_fm.setTemplateLoader(
						new ClassTemplateLoader(this.getClass(), "/fm"));
			}

			jsonParser = _apiUtils.getParser().getParser("JSON");
			jsonParser.init(null, null, null, null);

			jsonWriter = _apiUtils.getWriter().getWriter("JSON");
			jsonWriter.init(null, null, null);
		} catch (Exception iex) {
			log.error("init", iex);
		}
	}
	
	@Modified
	public void update(Map<String, String> params) {
		_params = params;

		_sendEmails.configure(params);
	}

	public static SendEmails getSendEmails() {
		return _sendEmails;
	}
	
	@Override
	public void init() throws ServletException {
		
	}

	@Reference(policy = ReferencePolicy.DYNAMIC,
			policyOption = ReferencePolicyOption.RELUCTANT,
			cardinality = ReferenceCardinality.MULTIPLE)
	public void addParser(ImportParser parser, Map<String, String> svcProps) {
		importParsers.put(svcProps.get("type"), parser);
	}

	public void removeParser(ImportParser parser, Map<String, String> svcProps) {
		importParsers.remove(svcProps.get("type"));
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req.getMethod(), req, resp);
	}

	protected void handleRequest(String method, HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		try {
			if (req.getRequestURI().contains("/apiweb/css/main.css")) {
				transferFile(req.getRequestURI().replace("/apiweb", ""), resp);

				return;
			}

			resp.setHeader("Access-Control-Allow-Origin", "*");

			if (req.getRequestURI().contains("/auth/")) {
				if ("OPTIONS".equals(req.getMethod())) {
					processOptions("GET, PUT, POST, OPTIONS", req, resp);
				} else {
					handleAuth(method, req, resp);
					return;
				}
			}

			// TODO:  Authenticate the token
			ApiObject authUser = authenticate.validateToken(req.getHeader("Authorization"), true);

			UrlEntry url = new UrlEntry();

			url.setCurrUrl(req.getRequestURI());
			url.chompUrl();

			SimpleHash hash = getHash(req);
			if (req.getRequestURI().endsWith("/apiclasses")) {
				Template tmp = _fm.getTemplate("classes/list.html");

				tmp.process(hash, resp.getWriter());
			} else if (req.getRequestURI().endsWith("/apimapping")) {
				Template tmp = _fm.getTemplate("classes/mapping.html");

				tmp.process(hash, resp.getWriter());
			} else if (req.getRequestURI().endsWith("/monitors")) {
				Template tmp = _fm.getTemplate("monitors/monitors.html");

				hash.put("title", "My Title");
				tmp.process(hash, resp.getWriter());
			} else if (req.getRequestURI().contains("/apipromote")) {
				handleApiPromote(method, req, resp);
			} else if (req.getRequestURI().contains("/webpromote")) {
				handleWebPromote(method, req, resp);
			} else if (req.getRequestURI().contains("/api/import")) {
				if ("OPTIONS".equals(req.getMethod())) {
					processOptions("POST, OPTIONS", req, resp);
				} else {
					if (importParsers.containsKey(req.getParameter("format"))) {
						importParsers.get(req.getParameter("format")).parseImport(req.getParameter("prefix"), this, req.getInputStream());
					}
				}
			} else if (req.getRequestURI().endsWith("/webservices")) {
				Template tmp = _fm.getTemplate("classes/webservices.html");

				tmp.process(hash, resp.getWriter());
			} else if (req.getRequestURI().contains("/view")) {
				if ("OPTIONS".equals(req.getMethod())) {
					processOptions("GET, OPTIONS", req, resp);
				} else {
					processClassView(req, resp);
				}
			} else if ("esget".equals(url.getCurrEntry())) {
				url.chompUrl();
				String strIndex = url.getCurrEntry();
				url.chompUrl();
				String strType = url.getCurrEntry();
				url.chompUrl();
				String strId = url.getCurrEntry();

				ApiObject objRet = _elasticUtils.getRecord(
						strIndex,
						strId);

				if (objRet != null) {
					String objJson = Transform.toString(_apiUtils.getWriter().
							getWriter(
									"JSON"), null, objRet);

					resp.setStatus(200);
					resp.setContentType("application/json");
					resp.setContentLength(objJson.length());
					resp.getWriter().append(objJson);
				} else {
					resp.setStatus(404);
				}

			} else if (req.getRequestURI().contains("/es/")) {
				if ("OPTIONS".equals(req.getMethod())) {
					processOptions("GET, PUT, POST, OPTIONS", req, resp);
				} else {
					if ("POST".equals(method) || "PUT".equals(method)) {
						if (req.getRequestURI().contains("webservicesdata")) {
							// Do Nothing
						} else if (req.getRequestURI().contains("apiclasses")) {
							// Do Nothing
						} else {
							parseDataTableRestElastic(method, req, resp);
							Thread.sleep(2000);
						}
					} else {
						parseDataTableRestElastic(method, req, resp);
					}
				}
			} else if (req.getRequestURI().contains("/db/")) {
				log.error("Still calling DB");
				resp.setStatus(400);
				resp.getWriter().append("Should not call");
			} else {
				Template tmp = _fm.getTemplate("home.html");

				tmp.process(hash, resp.getWriter());
			}
		} catch (Exception ex) {
			if (ex instanceof ApplicationException) {
				resp.setStatus(((ApplicationException) ex).getStatus());

				if (req.getContentType() != null && req.getContentType().contains("application/json")) {
					resp.getWriter().append("{ \"message\": \"" + ex.getMessage() + "\" }");
				} else {
					log.error("handleRequest", ex);
					throw new ServletException("Error in Page", ex);
				}
			} else {
				log.error("handleRequest", ex);
				throw new ServletException("Error in Page", ex);
			}
		}
	}

	private void processOptions(String allowedMethods, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		if (req.getHeader("Access-Control-Request-Method") != null) {
			resp.setHeader("Access-Control-Allow-Origin", req.getHeader("origin"));
			resp.setHeader("Access-Control-Allow-Methods", allowedMethods);
			resp.setHeader("Access-Control-Allow-Headers", req.getHeader("Access-Control-Request-Headers"));
			resp.setHeader("Access-Control-Max-Age", "600");
			resp.setStatus(200);
		}
	}

	private void handleAuth(String method, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		ApiObject objReq = jsonParser.parseSingle(new InputStreamReader(req.getInputStream()));
		ApiObject objRet = null;

		if (req.getRequestURI().endsWith("/login")) {
			objRet = authenticate.login(objReq);

			if (objRet != null && objRet.containsKey("message")) {
				resp.setStatus(401);
			}
		} else if (req.getRequestURI().endsWith("/logout")) {
			ApiObject authUser = authenticate.validateToken(req.getHeader("Authorization"), true);

			objReq.setInteger("userId", authUser.getInteger("id"));

			objRet = authenticate.logout(objReq);

			if (objRet != null && objRet.containsKey("message")) {
				resp.setStatus(401);
			}
		} else if (req.getRequestURI().endsWith("/verifyEmail")) {
			objRet = authenticate.verifyEmail(objReq);

			resp.setStatus(200);
		} else if (req.getRequestURI().endsWith("/validateEmail")) {
			objRet = authenticate.validateEmail(objReq);

			if (objRet != null) {
				if (objRet.getInteger("status") == 1) {
					resp.setStatus(409);
				} else if (objRet.containsKey("message")) {
					resp.setStatus(500);
				}
			}
		} else if (req.getRequestURI().endsWith("/user")) {
			objRet = authenticate.storeUser(objReq);

			if (objRet != null) {
				if (objRet.containsKey("message")) {
					if (objRet.getString("message").equals("Email Address Already Exists")) {
						resp.setStatus(409);
					} else {
						resp.setStatus(500);
					}
				} else {
					switch (objRet.getInteger("status")) {
						case 0:
							resp.setStatus(201);
							break;

						case 1:
							resp.setStatus(409);
							break;
					}
				}
			}
		}

		if (objRet != null) {
			resp.setContentType("application/json");
			resp.getWriter().append(jsonWriter.writeSingle(objRet));
		}
	}

	private void transferFile(String sResource, HttpServletResponse resp) throws Exception {
		URL url = this.getClass().getResource(sResource);

		URLConnection conn = url.openConnection();

		String connType = conn.getContentType();

		if (connType != null) {
			resp.setContentType(connType);
		} else {
			if (sResource.endsWith(".css")) {
				resp.setContentType("text/css");
			}
		}

		resp.setContentLength(conn.getContentLength());

		writeFile(conn.getInputStream(), resp.getOutputStream());
	}

	private void writeFile(InputStream is, OutputStream out) throws Exception {
		byte[] buffer = new byte[4096]; // tweaking this number may increase performance
		int len;

		while ((len = is.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}

		out.flush();
	}

	private void processClassView(HttpServletRequest req,
			HttpServletResponse resp) throws Exception {
		String strClass = req.getRequestURI().replace("/apiweb/view/", "");
		List<String> lstEntry = Arrays.asList(strClass.split("/"));

		//ApiUtilsSql apiUtil = getApiSql(req.getParameter("environment"));
		String strClassName = lstEntry.get(lstEntry.size() - 1);

		ApiClasses intClasses = new ApiClasses();
		_apiUtils.retrieveClasses(strClassName, intClasses);

		if (intClasses.getClasses().size() > -1) {
			if ("xsd".equalsIgnoreCase(req.getParameter("format"))) {
				String strPackage = strClassName.substring(0, strClassName.
						lastIndexOf(
								"."));
				String namespaceUrl = "http://" + XsdTransform.
						reversePackageName(
								strPackage) + "/";

				String strXsd = XsdTransform.convertToXsd(intClasses,
						namespaceUrl);

				resp.setContentType("text/xml");
				resp.setContentLength(strXsd.length());
				resp.getWriter().append(strXsd);
			} else if ("plantumlpackage".equalsIgnoreCase(req.getParameter(
					"format"))) {
				Template tmp = _fm.getTemplate("classes/view.html");

				SimpleHash hash = getHash(req);

				String strPackage = strClassName.substring(0, strClassName.
						lastIndexOf(
								"."));

				hash.put("className", strPackage);
				StringBuilder tbImg = new StringBuilder();
				tbImg.append("<img src=\"");
				tbImg.append(req.getRequestURI());
				tbImg.append("?format=plantumlembedpackage");
				tbImg.append("\" />");

				hash.put("classHtml", tbImg.toString());
				tmp.process(hash, resp.getWriter());
			} else if ("plantumlembedpackage".equalsIgnoreCase(req.getParameter(
					"format"))) {
				String strPackage = strClassName.substring(0, strClassName.
						lastIndexOf(
								"."));

				intClasses.getClassList().clear();

				_apiUtils.retrievePackage(strPackage, intClasses);

				PlantUmlFormatter uml = new PlantUmlFormatter();
				String strUml = uml.format(intClasses);

				SourceStringReader plntUml = new SourceStringReader(strUml);

				ByteArrayOutputStream out = new ByteArrayOutputStream();

				plntUml.generateImage(out, new FileFormatOption(FileFormat.SVG));

				resp.setContentType("image/svg+xml");
				resp.setContentLength(out.size());
				resp.getOutputStream().write(out.toByteArray());
			} else if ("plantuml".equalsIgnoreCase(req.getParameter("format"))) {
				Template tmp = _fm.getTemplate("classes/view.html");

				SimpleHash hash = getHash(req);

				hash.put("className", strClassName);
				StringBuilder tbImg = new StringBuilder();
				tbImg.append("<img src=\"");
				tbImg.append(req.getRequestURI());
				tbImg.append("?format=plantumlembed");
				tbImg.append("\" />");

				hash.put("classHtml", tbImg.toString());
				tmp.process(hash, resp.getWriter());
			} else if ("plantumlembed".equalsIgnoreCase(req.getParameter(
					"format"))) {
				String strPackage = strClassName.substring(0, strClassName.
						lastIndexOf(
								"."));

				PlantUmlFormatter uml = new PlantUmlFormatter();
				String strUml = uml.format(intClasses);

				SourceStringReader plntUml = new SourceStringReader(strUml);

				ByteArrayOutputStream out = new ByteArrayOutputStream();

				plntUml.generateImage(out, new FileFormatOption(FileFormat.SVG));

				resp.setContentType("image/svg+xml");
				resp.setContentLength(out.size());
				resp.getOutputStream().write(out.toByteArray());
			} else if ("ffdMulesoft".equalsIgnoreCase(req.getParameter(
					"format"))) {
				String strPackage = strClassName.substring(0, strClassName.
						lastIndexOf(
								"."));

				String strFddResponse = FddFormatter.convertToFdd(strClass, intClasses);

				resp.setHeader("Content-Disposition",
						String.format("attachment; filename=\"%s\"",
								intClasses.getClass(strClassName).getString("name") + ".ffd")
				);
				resp.setContentType("application/text");
				resp.setContentLength(strFddResponse.length());
				resp.getWriter().write(strFddResponse);
			} else if ("raml".equalsIgnoreCase(req.getParameter(
					"format"))) {
				String strPackage = strClassName.substring(0, strClassName.
						lastIndexOf(
								"."));

				String strFddResponse = RamlFormatter.convertToRaml(strClass, intClasses);

				resp.setHeader("Content-Disposition",
						String.format("attachment; filename=\"%s\"",
								intClasses.getClass(strClassName).getString("name") + ".raml")
				);
				resp.setContentType("application/text");
				resp.setContentLength(strFddResponse.length());
				resp.getWriter().write(strFddResponse);
			} else if ("esmapv5".equalsIgnoreCase(req.getParameter(
					"format"))) {
				ElasticSearchV5Formatter v5format = new ElasticSearchV5Formatter();
				v5format.setApiUtils(_apiUtils);

				resp.setContentType("application/json");
				String strResp = v5format.format(intClasses, strClassName);

				resp.setContentLength(strResp.length());
				resp.getWriter().append(strResp);
			} else if ("docpos".equalsIgnoreCase(req.getParameter(
					"format"))) {
				Template tmp = _fm.getTemplate("classes/view.html");

				SimpleHash hash = getHash(req);

				hash.put("className", strClassName);
				hash.put("classHtml", DocTransform.convertClassToHtml(
						strClassName,
						intClasses, true));
				tmp.process(hash, resp.getWriter());
			} else {
				Template tmp = _fm.getTemplate("classes/view.html");

				SimpleHash hash = getHash(req);

				hash.put("className", strClassName);
				hash.put("classHtml", DocTransform.convertClassToHtml(
						strClassName,
						intClasses, false));
				tmp.process(hash, resp.getWriter());
			}
		} else {
			Template tmp = _fm.getTemplate("classes/view.html");

			SimpleHash hash = getHash(req);

			hash.put("className", strClassName);
			hash.put("classHtml", "No Class Information Found");
			tmp.process(hash, resp.getWriter());
		}
	}

	private ApiObject parseDataTableRest(Map<String, String[]> params) throws Exception {
		ApiObject retObj = new ApiObject();

		if (params.containsKey("start")) {
			retObj.setInteger("start", Integer.parseInt(params.get("start")[0]));
		}

		if (params.containsKey("length")) {
			retObj.setInteger("count", Integer.parseInt(params.get("length")[0]));
		}

		if (params.containsKey("search[value]") && !params.get("search[value]")[0].
				isEmpty()) {
			retObj.setString("query", params.get("search[value]")[0]);
		}

		if (params.containsKey("order[0][column]") && !params.get(
				"order[0][column]")[0].
				isEmpty()) {
			String columnId = params.get("order[0][column]")[0];

			if (params.containsKey("columns[" + columnId + "][data]")) {
				String fieldName = params.get("columns[" + columnId + "][data]")[0];

				if (fieldName != null && !fieldName.isEmpty()
						&& params.containsKey(
								"order[0][dir]")
						&& "desc".equals(params.get("order[0][dir]")[0])) {
					retObj.setString("SortDir", "DESC");
				} else {
					retObj.setString("SortDir", "ASC");
				}

				if (!fieldName.contains("_id") && !fieldName.isEmpty()) {
					retObj.setString("SortField", fieldName);
				}
			}

		}

		return retObj;
	}

	private void parseDataTableRestElastic(String method, HttpServletRequest req,
			HttpServletResponse resp) throws Exception {
		ApiObject tableUrl = pullTableUrl(req.getRequestURI().replace(
				"/apiweb/es/",
				""));

		String reqString = null;
		String reqQuery = req.getQueryString();

		try {
			reqString = HttpUtils.inputStreamToString(req.getInputStream(),
					"UTF-8");
		} catch (Exception ex) {

		}

		if (req.getHeader("Origin") != null) {
			resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
			resp.setHeader("Vary", "Origin");
		}

		if ("GET".equals(method) && tableUrl.getString("id") == null) {
			Map<String, String[]> params = new HashMap<>();

			WebUtils.pullReqParamValues(params, req);

			EsSearch search = processDataTable(params, req);

			ApiObject obj = null;

			try {
				obj = _elasticUtils.getSearchRecords(
						tableUrl.
								getString("table"), null,
						search);
			} catch (Exception ex) {
				log.error("Elastic Failure: ", ex);
			}

			ApiObject retObj = new ApiObject();

			try {
				if (obj != null && obj.getLong("total.value") != null && obj.getLong(
						"total.value") > 0L) {
					retObj.setString("draw", req.getParameter("draw"));
					retObj.setInteger("iTotalRecords", obj.getList("hits").
							size());
					retObj.setInteger("iTotalDisplayRecords", obj.getLong(
							"total.value").
							intValue());
					retObj.createList("data");

					List<ApiObject> retHits = obj.getList("hits");

					int iCnt = 0;

					for (ApiObject o : retHits) {
						ApiObject source = o.getObject("_source");

						if (tableUrl.getString("type") == null) {
							source.setString("_id",
									o.getString("_type") + "/" + o.getString(
									"_id"));
						} else {
							source.setString("_id", o.getString("_id"));
						}

						source.setString("_type", o.getString("_type"));

						retObj.getList("data").add(source);
					}

					resp.setStatus(200);
					resp.setContentType("application/json");

					String strJson = Transform.toString(_apiUtils.getWriter().
							getWriter(
									"JSON"), null, retObj);
					//resp.setContentLength(strJson.length());
					resp.getWriter().append(strJson);

				} else {
					resp.setStatus(200);
					resp.setContentType("application/json");

					retObj.setString("draw", req.getParameter("draw"));

					if (obj != null) {
						if (obj.isSet("hits")) {
							retObj.setInteger("iTotalRecords", obj.getList(
									"hits").
									size());
						} else {
							retObj.setInteger("iTotalRecords", 0);
						}
						retObj.setInteger("iTotalDisplayRecords", obj.getLong(
								"total.value").
								intValue());
					}
					retObj.createList("data");

					String strJson = Transform.toString(_apiUtils.getWriter().
							getWriter(
									"JSON"), null, retObj);
					resp.setContentLength(strJson.length());
					resp.getWriter().append(strJson);
				}

			} catch (Exception ex) {
				log.error("Write", ex);
			}
		} else if ("GET".equals(method) && tableUrl.getString("id") != null) {
			ApiObject idObject = _elasticUtils.getRecord(tableUrl.
					getString("table"),
					tableUrl.getString("id"));
			resp.setStatus(200);
			resp.setContentType("application/json");
			try {
				resp.getWriter().append("[");
				resp.getWriter().append(Transform.toString(
						_apiUtils.getWriter().
								getWriter("JSON"), ApiObject.returnClassDef(),
						idObject));
				resp.getWriter().append("]");
			} catch (Exception ex) {
				log.error("Sending GET Results", ex);
			}
		} else if ("POST".equals(method)) {
			ApiObject saveObject = Transform.toApiObject(_apiUtils.getParser().
					getParser("JSON"), null, null, reqString);

			if (saveObject.getString("_id") == null) {
				saveObject.setString("_id", tableUrl.getString("id"));
			}

			switch (tableUrl.getString("table")) {
				case "apimodels":
					_apiUtils.saveApiModel(saveObject);

					ApiClasses apiRetClasses = new ApiClasses();

					Thread.sleep(2000);

					_apiUtils.retrieveClasses(saveObject.getString("className"), apiRetClasses);

					resp.getWriter().append(Transform.toString(
							_apiUtils.getWriter().
									getWriter("JSON"), ApiObject.returnClassDef(),
							apiRetClasses.getClassList().get(0)));

					break;

				case "apimappings":
					_apiUtils.saveApiMapping(saveObject);
					break;

				case "apiservices":
					_apiUtils.saveApiService(saveObject);

					Thread.sleep(2000);

					ApiObject objRet = _apiUtils.retrieveServices(saveObject.getString("info.title"), null);

					resp.getWriter().append(jsonWriter.writeSingle(objRet.getObject(saveObject.getString("info.title"))));

					break;
			}

			resp.setStatus(200);
		} else if ("DELETE".equals(method)) {
			_elasticUtils.deleteRecord(tableUrl.getString("table"),
					tableUrl.getString("id"));
			resp.setStatus(200);
		} else if ("PUT".equals(method)) {
			ApiObject saveObject = Transform.toApiObject(_apiUtils.getParser().
					getParser("JSON"), ApiObject.returnClassDef(), tableUrl.
					getString(
							"type"), reqString);
			String saveid = saveObject.getString("_id");

			if (saveid != null && !saveid.isEmpty()) {
				saveObject.getFields().remove("_id");
			} else {
				saveid = null;
			}

			_elasticUtils.saveRecord(tableUrl.getString("table"),
					saveid, saveObject);
			resp.setStatus(200);
		}
	}

	public ApiObject pullTableUrl(String url) {
		String[] urlParse = url.split("/");
		ApiObject retObj = new ApiObject();

		retObj.setString("table", urlParse[0]);

		if (urlParse.length > 1) {
			retObj.setString("type", urlParse[1]);
		}

		if (urlParse.length > 2) {
			retObj.setString("id", urlParse[2]);
		}

		return retObj;
	}

	private void handleApiPromote(String method, HttpServletRequest req,
			HttpServletResponse resp) throws Exception {
		Template tmp = _fm.getTemplate("classes/apipromote.html");
		ApiObject promoteRet = null;
		if (req.getParameter("promote") != null) {
//			ApiPromote promote = new ApiPromote();
//			promote.setParser(_apiUtils.getParser());
//			promote.setWriter(_apiUtils.getWriter());
//
//			promoteRet = promote.promotePackage(_config.getProperty(
//					  "ElasticSearch_Location"), _config.getProperty(
//								 "ElasticSearch_UrlPromote"), req.getParameter(
//								 "packageName"));
		}

		SimpleHash hash = getHash(req);

		if (promoteRet != null) {
			hash.put("objRet", promoteRet);
		}

		tmp.process(hash, resp.getWriter());
	}

	private void handleWebPromote(String method, HttpServletRequest req,
			HttpServletResponse resp) throws Exception {
		Template tmp = _fm.getTemplate("classes/webpromote.html");
		ApiObject promoteRet = null;
		if (req.getParameter("promote") != null) {
//			ApiPromote promote = new ApiPromote();
//			promote.setParser(_apiUtils.getParser());
//			promote.setWriter(_apiUtils.getWriter());
//
//			promoteRet = promote.promoteServices(_config.getProperty(
//					  "ElasticSearch_Location"), _config.getProperty(
//								 "ElasticSearch_UrlPromote"), req.getParameter(
//								 "serviceName"));
		}

		SimpleHash hash = getHash(req);

		if (promoteRet != null) {
			hash.put("objRet", promoteRet);
		}

		tmp.process(hash, resp.getWriter());
	}

	private SimpleHash getHash(HttpServletRequest req) {
		SimpleHash hash = new SimpleHash(objWrap.build());

		hash.put("Environments", mapUtils.keySet());

		if (req.getParameter("noframe") != null) {
			hash.put("noframe", true);
		} else {
			hash.put("noframe", false);
		}

		return hash;
	}

	private EsSearch processDataTable(Map<String, String[]> params,
			HttpServletRequest req) {
		EsSearch search = new EsSearch();

		Integer iStart = 0;
		Integer iCount = 0;

		if (req.getParameter("start") != null) {
			iStart = Integer.parseInt(req.getParameter("start"));
		}

		if (req.getParameter("length") != null) {
			iCount = Integer.parseInt(req.getParameter("length"));
		}

		search.setStart(iStart);
		search.setCount(iCount);

		if (params.containsKey("search[value]") && !params.get("search[value]")[0].
				isEmpty()) {
			BoolQuery bq = new BoolQuery();

			if (req.getRequestURI().contains("es/monitors")) {
				bq.getShould().add(new RegExpQuery("monitorName", ".*" + params.
						get(
								"search[value]")[0] + ".*"));
				bq.getShould().add(
						new RegExpQuery(
								"server",
								".*"
								+ params.get("search[value]")[0]
								+ ".*"
						)
				);
				bq.getShould().add(
						new RegExpQuery(
								"actions.action.name",
								".*"
								+ params.get("search[value]")[0]
								+ ".*"
						)
				);
			} else if (req.getRequestURI().contains("es/apimodels")) {
				bq.getShould().add(new RegExpQuery("name", ".*" + params.get(
						"search[value]")[0] + ".*"));
				bq.getShould().add(new RegExpQuery("className", ".*" + params.
						get(
								"search[value]")[0] + ".*"));
			} else if (req.getRequestURI().contains("es/webservices")) {
				bq.getShould().add(new RegExpQuery("name", ".*" + params.get(
						"search[value]")[0] + ".*"));
				bq.getShould().add(new RegExpQuery("packageName", ".*" + params.
						get(
								"search[value]")[0] + ".*"));
			} else {
				bq.getShould().add(new RegExpQuery("name", ".*" + params.get(
						"search[value]")[0] + ".*"));
			}

			search.getQuery().setQuery(bq);
		}

		if (params.containsKey("order[0][column]") && !params.get(
				"order[0][column]")[0].
				isEmpty()) {
			String columnId = params.get("order[0][column]")[0];

			if (params.containsKey("columns[" + columnId + "][data]")) {
				String fieldName = params.get("columns[" + columnId + "][data]")[0];

				if (fieldName != null && !fieldName.isEmpty()
						&& params.containsKey(
								"order[0][dir]")
						&& "desc".equals(params.get("order[0][dir]")[0])) {
					fieldName = "-" + fieldName;
				} else {
					fieldName = "+" + fieldName;
				}

				if (!fieldName.contains("_id") && !fieldName.isEmpty()) {
					search.addSortField(fieldName);
				}
			}

		}

		return search;
	}
}
