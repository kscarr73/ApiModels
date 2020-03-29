package com.progbits.api.srv;

import com.progbits.api.elastic.query.EsSearch;
import com.progbits.api.elastic.query.ParseRestToEsSearch;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import com.progbits.api.transforms.JsonTransform;
import com.progbits.api.transforms.XsdTransform;
import com.progbits.api.utils.oth.ApiUtilsInterface;
import com.progbits.web.WebUtils;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
@Component(name = "ApiWebServlet", immediate = true, property = {"name=ApiWebServlet", "alias=/apiweb"}, service = { HttpServlet.class })
public class ApiWebServlet extends HttpServlet {

    private Logger log = LoggerFactory.getLogger(ApiWebServlet.class);

    private String servletRoot = "/apiweb";

    private Long lastRun = 0L;
    private Configuration _fm = null;
    private ApiUtilsInterface _apiUtils = null;

    @Reference
    public void setApiUtils(ApiUtilsInterface api) {
        _apiUtils = api;
    }

    @Override
    public void init() throws ServletException {
        _fm = new Configuration();

        _fm.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);

        try {
            _fm.setClassForTemplateLoading(this.getClass(), "/fm");
            //_fm.setDirectoryForTemplateLoading(new File("/home/scarr/Projects_Git/isgapiservices/ApiWeb/src/main/resources/fm"));
        } catch (Exception iex) {
            log.error("init", iex);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req.getMethod(), req, resp);
    }

    protected void handleRequest(String method, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            SimpleHash hash = new SimpleHash();
            
            if (req.getRequestURI().endsWith("/apiclasses")) {
                Template tmp = _fm.getTemplate("classes/list.html");
    
                tmp.process(hash, resp.getWriter());
            } else if (req.getRequestURI().endsWith("/monitors")) {
                Template tmp = _fm.getTemplate("monitors/monitors.html");

                hash.put("title", "My Title");
                tmp.process(hash, resp.getWriter());
            } else if (req.getRequestURI().contains("/apipromote")) {
                handleApiPromote(method, req, resp);
            } else if (req.getRequestURI().contains("/webpromote")) {
                handleWebPromote(method, req, resp);
            } else if (req.getRequestURI().endsWith("db/monitoractions")) {
                retrieveMonitorActions(resp);
            } else if (req.getRequestURI().endsWith("/webservices")) {
                Template tmp = _fm.getTemplate("classes/webservices.html");

                tmp.process(hash, resp.getWriter());
            } else if (req.getRequestURI().contains("/view")) {
                processClassView(req, resp);
            } else if (req.getRequestURI().endsWith("/icglog")) {
                Template tmp = _fm.getTemplate("icglog/logger.html");

                tmp.process(hash, resp.getWriter());
            } else if (req.getRequestURI().endsWith("/icgaudit")) {
                Template tmp = _fm.getTemplate("icglog/audit.html");

                tmp.process(hash, resp.getWriter());
            } else if (req.getRequestURI().endsWith("/icgmetric")) {
                Template tmp = _fm.getTemplate("icglog/metric.html");

                tmp.process(hash, resp.getWriter());
            } else if (req.getRequestURI().contains("/es/")) {
                if ("POST".equals(method) || "PUT".equals(method)) {
                    if (req.getRequestURI().contains("webservices")) {
                        saveApiServices(method, req, resp);
                    } else if (req.getRequestURI().contains("apiclasses")) {
                        saveApiClasses(method, req, resp);
                    } else {
                        parseDojoRestElastic(method, req, resp);
                    }
                } else {
                    parseDojoRestElastic(method, req, resp);
                }
            } else {
                Template tmp = _fm.getTemplate("home.html");

                tmp.process(hash, resp.getWriter());
            }
        } catch (Exception ex) {
            log.error("handleRequest", ex);
            throw new ServletException("Error in Page", ex);
        }
    }

    private void saveApiServices(String method, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        ApiObject tableUrl = pullTableUrl(req.getRequestURI().replace("/apiweb/es/", ""));

        // TODO:  Check functions to see if the following classes exist:
        //   {packageName}.{functionName}
        //   {packageName}.{functionName}Response
        //   If they do not exist then create stub classes
        String reqString;
        try {
            reqString = inputStreamToString(req.getInputStream(), "UTF-8");
        } catch (IOException ex) {
            throw new Exception("Input String Error", ex);
        }

        ApiObject saveObject = JsonTransform.jsonToApiObject(_apiUtils.getParser(), ApiObject.returnServiceDef(), "com.icg.isg.api.ApiService", reqString);

        String saveid = saveObject.getString("_id");

        if (saveid != null && !saveid.isEmpty()) {
            saveObject.getFields().remove("_id");
        } else {
            saveid = null;
        }

        _apiUtils.getElasticUtils().saveRecord(tableUrl.getString("table"), tableUrl.getString("type"), saveid, saveObject);
    }

    private void saveApiClasses(String method, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        ApiObject tableUrl = pullTableUrl(req.getRequestURI().replace("/apiweb/es/", ""));

        String reqString;
        try {
            reqString = inputStreamToString(req.getInputStream(), "UTF-8");
        } catch (IOException ex) {
            throw new Exception("Input String Error", ex);
        }

        ApiObject saveObject = JsonTransform.jsonToApiObject(_apiUtils.getParser(), ApiObject.returnClassDef(), "com.icg.isg.api.ApiClass", reqString);

        String saveid = saveObject.getString("_id");

        if (saveid != null && !saveid.isEmpty()) {
            saveObject.getFields().remove("_id");
        } else {
            saveid = null;
        }

        _apiUtils.getElasticUtils().saveRecord(tableUrl.getString("table"), tableUrl.getString("type"), saveid, saveObject);
    }

    private void processClassView(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String strClass = req.getRequestURI().replace("/apiweb/view/", "");
        List<String> lstEntry = Arrays.asList(strClass.split("/"));

        String strClassName = lstEntry.get(lstEntry.size() - 1);

        ApiClasses intClasses = new ApiClasses();
        _apiUtils.retrieveClasses(strClassName, intClasses);

        if (intClasses.getClasses().size() > -1) {
            if ("xsd".equalsIgnoreCase(req.getParameter("format"))) {
                String strPackage = strClassName.substring(0, strClassName.lastIndexOf("."));
                String namespaceUrl = "http://" + XsdTransform.reversePackageName(strPackage) + "/";

                String strXsd = XsdTransform.convertToXsd(intClasses, namespaceUrl);

                resp.setContentType("text/xml");
                resp.setContentLength(strXsd.length());
                resp.getWriter().append(strXsd);
            } else {
                Template tmp = _fm.getTemplate("classes/view.html");

                SimpleHash hash = new SimpleHash();

                hash.put("className", strClassName);
                hash.put("classHtml", XsdTransform.convertClassToHtml(strClassName, intClasses));
                tmp.process(hash, resp.getWriter());
            }
        } else {
            Template tmp = _fm.getTemplate("classes/view.html");

            SimpleHash hash = new SimpleHash();

            hash.put("className", strClassName);
            hash.put("classHtml", "No Class Information Found");
            tmp.process(hash, resp.getWriter());
        }
    }

    private void parseDojoRestElastic(String method, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        ApiObject tableUrl = pullTableUrl(req.getRequestURI().replace("/apiweb/es/", ""));

        String reqString = null;
        String reqQuery = req.getQueryString();

        try {
            reqString = inputStreamToString(req.getInputStream(), "UTF-8");
        } catch (Exception ex) {

        }

        if ("GET".equals(method) && tableUrl.getString("id") == null) {
            Map<String, String[]> params = new HashMap<>();

            WebUtils.pullReqParamValues(params, req);

            EsSearch search = ParseRestToEsSearch.parseRest(params, WebUtils.pullHeaders(req));

            ApiObject obj = _apiUtils.getElasticUtils().getSearchRecords(tableUrl.getString("table"), null, tableUrl.getString("type"), search);

            try {
                if (obj != null && obj.getLong("total") != null && obj.getLong("total") > 0L) {
                    resp.getWriter().append("[");

                    List<ApiObject> retHits = obj.getList("hits");

                    resp.setContentType("application/json");

                    if (search.getCount() > 0) {
                        resp.setHeader("Content-Range", "items " + search.getStart() + "-" + (retHits.size() - 1) + "/" + obj.getLong("total"));
                    }

                    int iCnt = 0;

                    for (ApiObject o : retHits) {
                        if (iCnt > 0) {
                            resp.getWriter().append(",");
                        }

                        ApiObject source = o.getObject("_source");

                        if (tableUrl.getString("type") == null) {
                            source.setString("_id", o.getString("_type") + "/" + o.getString("_id"));
                        } else {
                            source.setString("_id", o.getString("_id"));
                        }

                        source.setString("_type", o.getString("_type"));

                        resp.getWriter().append(JsonTransform.convertToJson(_apiUtils.getWriter(), ApiObject.returnClassDef(), source));

                        iCnt++;
                    }

                    resp.getWriter().append("]");
                } else {
                    resp.setStatus(204);
                    resp.getWriter().append("{ }");
                }

            } catch (Exception ex) {
                log.error("Write", ex);
            }
        } else if ("GET".equals(method) && tableUrl.getString("id") != null) {
            ApiObject idObject = _apiUtils.getElasticUtils().getRecord(tableUrl.getString("table"),
                    tableUrl.getString("type"), tableUrl.getString("id"));
            resp.setStatus(200);
            resp.setContentType("application/json");
            try {
                resp.getWriter().append("[");
                resp.getWriter().append(JsonTransform.convertToJson(_apiUtils.getWriter(), ApiObject.returnClassDef(), idObject));
                resp.getWriter().append("]");
            } catch (Exception ex) {
                log.error("Sending GET Results", ex);
            }
        } else if ("POST".equals(method)) {
            ApiObject saveObject = JsonTransform.jsonToApiObject(_apiUtils.getParser(), ApiObject.returnServiceDef(), tableUrl.getString("type"), reqString);

            String saveid = saveObject.getString("_id");

            if (saveid != null && !saveid.isEmpty()) {
                saveObject.getFields().remove("_id");
            } else {
                saveid = null;
            }

            _apiUtils.getElasticUtils().saveRecord(tableUrl.getString("table"), tableUrl.getString("type"), saveid, saveObject);
        } else if ("DELETE".equals(method)) {
            _apiUtils.getElasticUtils().deleteRecord(tableUrl.getString("table"), tableUrl.getString("type"), tableUrl.getString("id"));
        } else if ("PUT".equals(method)) {
            ApiObject saveObject = JsonTransform.jsonToApiObject(_apiUtils.getParser(), ApiObject.returnClassDef(), tableUrl.getString("type"), reqString);
            String saveid = saveObject.getString("_id");

            if (saveid != null && !saveid.isEmpty()) {
                saveObject.getFields().remove("_id");
            } else {
                saveid = null;
            }

            _apiUtils.getElasticUtils().saveRecord(tableUrl.getString("table"), tableUrl.getString("type"), saveid, saveObject);
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

    private void retrieveMonitorActions(HttpServletResponse resp) {
        
    }

    private void handleApiPromote(String method, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Template tmp = _fm.getTemplate("classes/apipromote.html");
        ApiObject promoteRet = null;
        if (req.getParameter("promote") != null) {
//            ApiPromote promote = new ApiPromote();
//            promote.setParser(_apiUtils.getParser());
//            promote.setWriter(_apiUtils.getWriter());
//
//            promoteRet = promote.promotePackage(_config.getProperty("ElasticSearch_Location"), _config.getProperty("ElasticSearch_UrlPromote"), req.getParameter("packageName"));
        }

        SimpleHash hash = new SimpleHash();
        
        if (promoteRet != null) {
            hash.put("objRet", promoteRet);
        }
        
        tmp.process(hash, resp.getWriter());
    }

    private void handleWebPromote(String method, HttpServletRequest req, HttpServletResponse resp) throws Exception {
            Template tmp = _fm.getTemplate("classes/webpromote.html");
        ApiObject promoteRet = null;
//        if (req.getParameter("promote") != null) {
//            ApiPromote promote = new ApiPromote();
//            promote.setParser(_apiUtils.getParser());
//            promote.setWriter(_apiUtils.getWriter());
//
//            promoteRet = promote.promoteServices(_config.getProperty("ElasticSearch_Location"), _config.getProperty("ElasticSearch_UrlPromote"), req.getParameter("serviceName"));
//        }

        SimpleHash hash = new SimpleHash();
        
        if (promoteRet != null) {
            hash.put("objRet", promoteRet);
        }
        
        tmp.process(hash, resp.getWriter());
    }
	
	/**
	 * Read an Input Stream Completely, and process with the provided Encoding
	 *
	 * @param is The input stream to pull a string from
	 * @param encoding Encoding to set the result to. DEFAULT: UTF-8
	 *
	 * @return The inputstream as a String of the specific encoding
	 * @throws IOException
	 */
	public static String inputStreamToString(InputStream is, String encoding) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		String intEncoding = "UTF-8";

		if (encoding != null && !encoding.isEmpty()) {
			intEncoding = encoding;
		}

		if (is == null) {
			return null;
		}

		int len;
		int size = 1024;
		byte[] buf;

		try {
			if (is instanceof ByteArrayInputStream) {
				size = is.available();
				buf = new byte[size];
				len = is.read(buf, 0, size);
			} else {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				buf = new byte[size];
				while ((len = is.read(buf, 0, size)) != -1) {
					bos.write(buf, 0, len);
				}
				buf = bos.toByteArray();
			}

			if (buf.length == 0) {
				return null;
			} else {
				return new String(buf, intEncoding);
			}
		} catch (IOException iex) {
			throw new Exception("inputStreamToString Issue", iex);
		}
	}

}
