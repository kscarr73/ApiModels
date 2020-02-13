package com.progbits.api.elastic;

import com.progbits.api.ObjectParser;
import com.progbits.api.ObjectWriter;
import com.progbits.api.elastic.query.EsSearch;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import com.progbits.api.parser.JsonObjectParser;
import com.progbits.api.writer.JsonObjectWriter;
import java.io.StringReader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
@Component(name = "ElasticUtils", 
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        property = {"name=ElasticUtils"},
        service = { ElasticUtils.class })
public class ElasticUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticUtils.class);

    private DateTimeFormatter isoFormat = ISODateTimeFormat.dateTime();
    private DateTimeFormatter formatShort = DateTimeFormat.forPattern("yyyy.MM.dd");

    private HttpClient httpClient = null;

    private String elasticUrl = "http://localhost:9200/";
    private String elasticUser = null;
    private String elasticPass = null;

    private ObjectParser _jsonParser;
    private ObjectWriter _jsonWriter;

    private final Base64.Encoder ENCODER = Base64.getEncoder();

    private URI elasticUri = null;

    private static Long _timeout = 10L;

    String lclType = "_doc";

    /**
     * Default null constructor
     */
    public ElasticUtils() {
        try {
            _jsonParser = new JsonObjectParser();
            _jsonParser.init(null, null, null, null);

            _jsonWriter = new JsonObjectWriter();
            _jsonWriter.init(null, null, null);
        } catch (ApiException ex) {

        }
    }

    public void setElasticUser(String elasticUser) {
        this.elasticUser = elasticUser;
    }

    public void setElasticPass(String elasticPass) {
        this.elasticPass = elasticPass;
    }

    public void setElasticUrl(String elasticUrl) {
        if (!elasticUrl.endsWith("/")) {
            this.elasticUrl = elasticUrl + "/";
        } else {
            this.elasticUrl = elasticUrl;
        }
    }

    public ObjectParser getJsonParser() {
        return _jsonParser;
    }
    
    public ObjectWriter getJsonWriter() {
        return _jsonWriter;
    }
    
    @Activate
    public void setup(Map<String, String> props) {
        update(props);
        
        SslContextFactory sslContext = new SslContextFactory();
        httpClient = new HttpClient(sslContext);

        try {
            elasticUri = new URI(elasticUrl);

            if (elasticUser != null) {
                AuthenticationStore store = httpClient.getAuthenticationStore();

                store.addAuthenticationResult(new BasicAuthentication.BasicResult(new URI(elasticUrl), elasticUser, elasticPass));
            }

            httpClient.start();
        } catch (Exception uri) {
            LOG.error("Settings Error", uri);
        }
    }

    @Deactivate
    public void destroy() {
        try {
            httpClient.stop();
        } catch (Exception ex) {
            
        }
    }
    
    @Modified
    public void update(Map<String, String> params) {
        if (params.get("ElasticUrl") != null) {
            setElasticUrl(params.get("ElasticUrl"));
        }
        if (params.get("ElasticUser") != null) {
            setElasticUser(params.get("ElasticUser"));
        }
        if (params.get("ElasticPass") != null) {
            setElasticPass(params.get("ElasticPass"));
        }
    }
    
    public String generateID() {
        UUID uuid = UUID.randomUUID();

        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        LongBuffer longBuffer = bb.asLongBuffer();

        longBuffer.put(new long[]{
            uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()
        });

        String strResp = new String(ENCODER.encode(bb.array()));
        strResp = strResp.replaceAll("[-+/]", "_").replace("=", "");
        strResp = strResp.replace("\r\n", "").replace("\n", "");

        return strResp;
    }

    public ApiObject runMethod(String url, String method) throws ApiElasticException {
        return runMethod(url, method, null, _timeout);
    }

    public ApiObject runApiMethod(String url, String method, ApiObject post, Long timeout) throws ApiElasticException {
        ApiObject respObj = null;

        try {
            if (post != null) {
                respObj = runMethod(url, "POST", _jsonWriter.writeSingle(post), _timeout);
            }
        } catch (ApiException ex) {
            throw new ApiElasticException("runApiMethod", ex);
        }

        return respObj;
    }

    public ApiObject runMethod(String url, String method, String strPost, Long timeout) throws ApiElasticException {
        ApiObject retObj = null;

        URI uriDoc = elasticUri.resolve(url);

        Request req = httpClient.newRequest(uriDoc).method(method).timeout(timeout, TimeUnit.SECONDS);

        if (strPost != null && !strPost.isEmpty()) {
            req.header("Content-Type", "application/json");
            req.content(new StringContentProvider(strPost));
        }

        try {
            ContentResponse resp = req.send();

            if (resp.getStatus() >= 200 && resp.getStatus() <= 299) {
                ApiObject elasticJson = _jsonParser.parseSingle(new StringReader(resp.getContentAsString()));

                retObj = elasticJson;
            } else {
                // TODO: Handle Error Case
            }
        } catch (ExecutionException | InterruptedException | TimeoutException | ApiException | ApiClassNotFoundException ex) {
            throw new ApiElasticException("runMethod", ex);
        }

        return retObj;
    }

    public ApiObject getRecord(String index, String type, String id) throws ApiElasticException {
        ApiObject retObj = runMethod(index + "/" + type + "/" + id, "GET");

        return retObj.getObject("_source");
    }

    public void saveRecord(String index, String id, ApiObject json) throws ApiElasticException {
        if (id == null) {
            if (json.getString("_id") != null) {
                id = json.getString("_id");
                json.remove("_id");
            }
        }

        if (id == null) {
            runApiMethod(index + "/" + lclType + "/", "POST", json, _timeout);
        } else {
            runApiMethod(index + "/" + lclType + "/" + id, "POST", json, _timeout);
        }
    }

    public void deleteRecord(String index, String id) throws ApiElasticException {
        if (id == null) {
            throw new ApiElasticException("ID Is Required for Delete function", null);
        }

        runMethod(index + "/" + lclType + "/" + id, "DELETE");
    }

    public void saveRecord(String index, DateTime storeDate, String id, ApiObject json) throws ApiElasticException {
        String curDate = storeDate.toString(formatShort);

        saveRecord(index + "-" + curDate, id, json);
    }

    public void deleteByQuery(String index, EsSearch search) throws ApiElasticException {
        this.runMethod(index + "/_delete_by_query",
                "POST", search.toJson(), 30L);
    }

    /**
     *
     * @param sb
     * @param action create - Fails if ID already exists, index - update or
     * create record
     * @param index Index where we should store the entry
     * @param id ID to Use for the entry, if NULL generateID is used
     * @param entryObj
     * @throws ApiElasticException Error parsing entryObj
     */
    public void setBulkLine(StringBuilder sb, String action, String index,
            String id, ApiObject entryObj) throws ApiElasticException {
        ApiObject obj = new ApiObject();

        String indexEntry = index.toLowerCase();

        obj.setString("_index", indexEntry);
        obj.setString("_type", lclType);

        if (id != null) {
            obj.setString("_id", id);
        } else {
            obj.setString("_id", generateID());
        }

        ApiObject actionObj = new ApiObject();

        actionObj.setObject(action, obj);

        try {
            sb.append(_jsonWriter.writeSingle(actionObj)).append("\n");

            if (entryObj != null && !"DELETE".equalsIgnoreCase(action)) {
                sb.append(_jsonWriter.writeSingle(entryObj)).append("\n");
            }
        } catch (ApiException ex) {
            throw new ApiElasticException("setBulkLine", ex);
        }
    }

    // <editor-fold desc="Bulk Functions">
    public ApiObject sendBulk(StringBuilder data) throws ApiElasticException {
        return sendBulk(data, 30L);
    }

    public ApiObject sendBulk(StringBuilder data, Long timeout) throws ApiElasticException {
        ApiObject objTrans = new ApiObject();

        ApiObject objBulkResp = runMethod("_bulk", "POST", data.toString(), timeout);

        if (objBulkResp == null) {
            throw new ApiElasticException("Request Failed", null);
        }

        objTrans.setLong("took", objBulkResp.getLong("took"));
        objTrans.setBoolean("errors", objBulkResp.getBoolean("errors"));

        if (objTrans.isSet("errors") && objTrans.getBoolean("errors")) {
            objTrans.createList("errorItems");

            String[] bulkRows = data.toString().split("\n");
            objTrans.setInteger("currCount", 0);

            objBulkResp.getList("items").forEach((item) -> {
                // Get the First Object
                ApiObject functionObj = (ApiObject) item.getFields().values().toArray()[0];

                Long lStatus = functionObj.getLong("status");

                if (lStatus != null && lStatus > 299) {

                    ApiObject errorItem = functionObj.getObject("error");

                    if (errorItem != null) {
                        ApiObject errorEntry = new ApiObject();

                        errorEntry.setString("type", errorItem.getString("type"));
                        errorEntry.setString("reason", errorItem.getString("reason"));
                        errorEntry.setString("causedBy", errorItem.getString("caused_by.reason"));

                        int rowLoc = objTrans.getInteger("currCount") * 2 + 1;

                        errorEntry.setString("row", bulkRows[rowLoc]);

                        objTrans.getList("errorItems").add(errorEntry);
                    }
                }

                objTrans.setInteger("currCount", objTrans.getInteger("currCount") + 1);
            });
        }

        return objTrans;
    }
    // </editor-fold>

    // <editor-fold desc="Elastic Search">       
    public ApiObject getSearchRecords(String index, ApiClasses classes,
            String type, EsSearch search) throws ApiElasticException {
        return getSearchRecords(index, classes, type, search, null);
    }

    public ApiObject getSearchRecords(String index, ApiClasses classes,
            String type, EsSearch search, String scrollHoldTime) throws ApiElasticException {
        return getSearchRecords(index, classes, search, scrollHoldTime, 30);
    }

    public ApiObject getSearchRecords(String index, ApiClasses classes,
            EsSearch search, String scrollHoldTime, long timeout) throws ApiElasticException {
        ApiObject retObj;
        ApiObject hits = null;
        String sUrl = index + "/" + lclType;

        sUrl += "/_search";

        if (scrollHoldTime != null) {
            sUrl += "?scroll=" + scrollHoldTime;
        }

        search.setVersion("5");

        String strJson = search.toJson();

        retObj = runMethod(sUrl, "POST", strJson, 30L);

        if (retObj != null) {
            hits = retObj.getObject("hits");

            if (retObj.isSet("_scroll_id")) {
                hits.setString("_scroll_id", retObj.getString("_scroll_id"));
            }

            if (retObj.getObject("aggregations") != null) {
                hits.setObject("aggregations", retObj.getObject("aggregations"));
            }
        }

        return hits;
    }

    public ApiObject continueScroll(String scrollId, String scrollHoldTime) throws ApiElasticException {
        return continueScroll(scrollId, scrollHoldTime, 30L);
    }

    public ApiObject continueScroll(String scrollId, String scrollHoldTime,
            Long timeout) throws ApiElasticException {
        String sUrl = "_search/scroll";
        ApiObject objRet = null;

        String strRequest = "{ \"scroll\": \"" + scrollHoldTime + "\", \"scroll_id\": \"" + scrollId + "\" }";

        objRet = runMethod(sUrl, "POST", strRequest, 10L);

        if (objRet == null) {
            throw new ApiElasticException("Elastic Search Did not return correctly", null);
        }

        ApiObject hits = objRet.getObject("hits");

        if (objRet.isSet("_scroll_id")) {
            hits.setString("_scroll_id", objRet.getString("_scroll_id"));
        }

        return hits;
    }
    // </editor-fold>

    // <editor-fold desc="Data Tables Retrieve">
    public ApiObject retrieveDataTablesFormat(String index, ApiObject mustQuery, 
            List<String> searchFields, Map<String, String[]> params) throws ApiElasticException {
        ApiObject retObj = new ApiObject();

        retObj.setString("draw", pullField("draw", params));

        retObj.createList("data");

        ApiObject objSearch = new ApiObject();

        String sStart = pullField("start", params);
        String sLength = pullField("length", params);
        ApiObject sortField = pullSortField(params);

        if (sStart != null) {
            objSearch.setInteger("from", Integer.parseInt(sStart));
        }

        if (sLength != null) {
            objSearch.setInteger("size", Integer.parseInt(sLength));
        }

        if (sortField != null) {
            objSearch.createList("sort");
            objSearch.getList("sort").add(sortField);
        }

        objSearch.setObject("query", getDataTableElasticQuery(mustQuery, searchFields, params));

        ApiObject elasticObj = runApiMethod(index + "/_search", "POST", objSearch, 30L);

        retObj.setLong("recordsTotal", elasticObj.getLong("hits.total"));

        if (elasticObj.getList("hits.hits") != null) {
            elasticObj.getList("hits.hits").forEach((obj) -> {
                ApiObject objRec = obj.getObject("_source");
                objRec.setString("_id", obj.getString("_id"));
                retObj.getList("data").add(objRec);
            });
        }

        retObj.setLong("recordsFiltered", elasticObj.getLong("hits.total"));

        return retObj;
    }

    public String pullField(String keyField, Map<String, String[]> params) {
        String strField = null;

        if (params != null) {
            String[] fieldValue = params.get(keyField);

            if (fieldValue != null) {
                strField = fieldValue[0];
            }
        }

        return strField;
    }

    public ApiObject getDataTableElasticQuery(ApiObject mustQuery, List<String> searchFields, Map<String, String[]> params) {
        ApiObject objQuery = new ApiObject();

        String lclQueryString = pullField("search[value]", params);

        if (lclQueryString != null && !lclQueryString.isEmpty()) {
            lclQueryString = "*" + lclQueryString + "*";
        } else {
            lclQueryString = null;
        }

        objQuery.createObject("bool");

        if (mustQuery != null) {
            objQuery.getObject("bool").createList("must");

            mustQuery.entrySet().forEach((entry) -> {
                ApiObject searchHost = new ApiObject();
                searchHost.createObject("term");

                searchHost.getObject("term").setString(entry.getKey(), (String) entry.getValue());

                objQuery.getList("bool.must").add(searchHost);
            });
        }

        if (lclQueryString != null) {
            final String queryString = lclQueryString;

            ApiObject objFilter = new ApiObject();

            objFilter.createObject("bool");

            objFilter.getObject("bool").createList("should");

            searchFields.forEach((searchFieldName) -> {
                ApiObject searchName = new ApiObject();
                searchName.createObject("wildcard");
                searchName.getObject("wildcard").setString(searchFieldName, queryString);

                objFilter.getList("bool.should").add(searchName);
            });

            objQuery.getObject("bool").createList("filter");
            objQuery.getList("bool.filter").add(objFilter);
        }

        return objQuery;
    }

    public ApiObject pullSortField(Map<String, String[]> params) {
        ApiObject retSort = null;

        if (params.get("order[0][column]") != null) {
            retSort = new ApiObject();

            String dataIndex = pullField("order[0][column]", params);

            if ("desc".equals(pullField("order[0][dir]", params))) {
                retSort.setString(pullField("columns[" + dataIndex + "][data]", params), "desc");
            } else {
                retSort.setString(pullField("columns[" + dataIndex + "][data]", params), "asc");
            }
        }

        return retSort;
    }
    // </editor-fold>
}
