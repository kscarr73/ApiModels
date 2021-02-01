package com.progbits.web;

import com.progbits.api.model.ApiObject;
import com.progbits.util.http.HttpUtils;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author scarr
 */
public class WebUtils {

    /**
     * Recreates the HTTP Header with the Headers found in the HTTP Request.
     *
     * @param req Request Object to pull information from.
     *
     * @return String with Headers in standard HTTP Format.
     */
    public static String getHttpHeaders(HttpServletRequest req) {
        Enumeration<String> eHeaders = req.getHeaderNames();

        StringBuilder sb = new StringBuilder();

        while (eHeaders.hasMoreElements()) {
            String sHead = eHeaders.nextElement();

            if (sb.length() > 0) {
                sb.append("\r\n");
            }

            sb.append(sHead).append(": ").append(req.getHeader(sHead));
        }

        return sb.toString();
    }

    /**
     * Pull the Body from an HTTP Request. Returns the Body as a String, with
     * optional Encoding.
     *
     * @param req HTTP Request to pull the body from
     * @return String of the Body
     *
     * @throws ICGAppException Exception that occurs during processing.
     */
    public static String getReqBody(HttpServletRequest req) throws Exception {
        String charEnc = req.getCharacterEncoding();

        if (charEnc == null) {
            charEnc = "UTF-8";
        }

        try {
            return HttpUtils.inputStreamToString(req.getInputStream(), charEnc);
        } catch (Exception ex) {
            throw new Exception("getReqBody", ex);
        }
    }

    /**
     * Pulls the Parameters, and Lower Cases all the key names
     *
     * @param params hashmap to populate with the keys
     * @param req Request to pull the parameters from
     * @throws ICGAppException
     */
    public static void pullReqParams(HashMap<String, String> params, HttpServletRequest req) throws Exception {
        Enumeration keys = req.getParameterNames();

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();

            //To retrieve a single value
            params.put(key.toLowerCase(), req.getParameter(key));
        }

        // Sets ItemId to the Item from a URL.
        // Assuming format: /ebook/items/13461341412345
        if (req.getRequestURI().contains("items")) {
            params.put("itemid", req.getRequestURI().substring(req.getRequestURI().lastIndexOf("/") + 1));
        }
    }

    /**
     * Pulls the Parameters, and Lower Cases all the key names
     *
     * @param params hashmap to populate with the keys
     * @param req Request to pull the parameters from
     * @throws ICGAppException
     */
    public static void pullReqParamValues(Map<String, String[]> params, HttpServletRequest req) throws Exception {
        Enumeration keys = req.getParameterNames();

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();

            //To retrieve a single value
            params.put(key.toLowerCase(), req.getParameterValues(key));
        }

        // Sets ItemId to the Item from a URL.
        // Assuming format: /ebook/items/13461341412345
        if (req.getRequestURI().contains("items")) {
            params.put("itemid", new String[]{req.getRequestURI().substring(req.getRequestURI().lastIndexOf("/") + 1)});
        }
    }

    /**
     * This function pulls the Parameters from a Request that DOES NOT have form
     * encoding content type turned on. It pulls the request body, and parses
     * manually.
     *
     * NOTE: All Params are lowercase, to make it easier to check parameters.
     *
     * @param params Map to put the headers found.
     * @param req Http Request to pull the information from
     *
     * @throws ICGAppException
     */
    public static void pullBadPost(HashMap<String, String> params, HttpServletRequest req) throws Exception {
        String strBody = getReqBody(req);

        // Pull All Params from Get Line
        pullReqParams(params, req);

        String[] sParams = strBody.split("&");

        if (sParams != null) {
            for (String p : sParams) {
                String[] cp = p.split("=");

                try {
                    if (cp.length == 1) {
                        params.put(URLDecoder.decode(cp[0], "UTF-8").toLowerCase(), "");
                    } else {
                        params.put(URLDecoder.decode(cp[0], "UTF-8").toLowerCase(),
                                URLDecoder.decode(cp[1], "UTF-8"));
                    }
                } catch (Exception ex) {
                    throw new Exception("pullBadPost", ex);
                }
            }
        }
    }

    public static HashMap<String, String> pullParams(String methodType, HttpServletRequest req) throws Exception {
        HashMap<String, String> reqParams = new HashMap<String, String>();

        String contentType = req.getHeader("content-type");
        if (contentType == null) {
            contentType = "";
        }

        if (methodType.equalsIgnoreCase("post")
                && !contentType.contains("application/x-www-form-urlencoded")) {
            pullBadPost(reqParams, req);
        } else {
            pullReqParams(reqParams, req);
        }

        return reqParams;
    }

    /**
     * Return the IP Address from X-Forwarded-For header or RemoteAddr.
     *
     * @param req
     * @return
     */
    public static String getIpAddress(HttpServletRequest req) {
        String strRet = req.getRemoteAddr();

        String strTest = req.getHeader("X-Forwarded-For");

        try {
            if (strTest != null) {
                strRet = strTest;
            } else {
                strTest = req.getHeader("Forwarded");

                if (strTest != null) {
                    int iLoc1 = strTest.indexOf("for");

                    if (iLoc1 > -1) {
                        int iLoc2 = strTest.indexOf(";", iLoc1);
                        if (iLoc2 > -1) {
                            strRet = strTest.substring(iLoc1 + 4, iLoc2 - 1);
                        } else {
                            strRet = strTest.substring(iLoc1 + 4);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // Don't care if an exception occured
        }

        return strRet;
    }

    /**
     * Pull the Authorization Header from an HTTP Request and parse the result
     *
     * @param req Http Request to Parse the Authorization Header
     * @return NULL if Header not found. ApiObject with following fields.
     * AuthType: BASIC, UserName, Password Or NULL if it doesn't exist
     *
     */
    public static ApiObject parseAuthorization(HttpServletRequest req) throws Exception {
        String auth = req.getHeader("Authorization");

        if (auth == null) {
            return null;
        } else {
            ApiObject retObj = new ApiObject();
            String[] splitAuth = auth.split(" ", 2);

            if (splitAuth.length == 1) {
                retObj.setString("AuthType", "BASIC");
                retObj.setString("AuthData", splitAuth[0]);

                if (retObj.getString("AuthData") != null && "BASIC".equalsIgnoreCase(retObj.getString("AuthData"))) {
                    // We have an invalid Authorization Object.
                    return retObj;
                }
            } else {
                retObj.setString("AuthType", splitAuth[0].toUpperCase());
                retObj.setString("AuthData", splitAuth[1]);
            }

            if ("BASIC".equals(retObj.getString("AuthType"))) {
                String sAuth = null;

                if (retObj.getString("AuthData") != null && !retObj.getString("AuthData").isEmpty()) {
                    try {
                        sAuth = Arrays.toString(Base64.getDecoder().decode(retObj.getString("AuthData")));

                        String[] elements = sAuth.split(":");

                        if (sAuth.length() > 1) {
                            retObj.setString("UserName", elements[0]);
                            retObj.setString("Password", elements[1]);

                        } else {
                            retObj.setString("UserName", elements[0]);
                        }
                    } catch (Exception ex) {
                        throw new Exception("Invalid Authorization Header: " + auth, ex);
                    }
                } else {
                    throw new Exception("Invalid Authorization Header: " + auth, null);
                }
            }

            return retObj;
        }
    }

    public static Map<String, String> pullHeaders(HttpServletRequest req) {
        Enumeration<String> eHdr = req.getHeaderNames();
        Map<String, String> retHdrs = new HashMap<>();

        while (eHdr.hasMoreElements()) {
            String hdr = eHdr.nextElement();

            retHdrs.put(hdr, req.getHeader(hdr));
        }

        return retHdrs;
    }

    /**
     * Return an Integer from a Http Parameter List.
     *
     * @param req HttpServletRequest to test the parameter.
     * @param param Parameter name to pull.
     *
     * @return Integer or null if not found
     */
    public static Integer pullIntegerParam(HttpServletRequest req, String param) {
        Integer iRet = null;

        try {
            iRet = Integer.parseInt(req.getParameter(param));
        } catch (Exception ex) {

        }

        return iRet;
    }

    public static ApiObject parseDataTableParams(HttpServletRequest req) throws Exception {
        ApiObject objRet = new ApiObject();

        Enumeration<String> enumParams = req.getParameterNames();

        while (enumParams.hasMoreElements()) {
            String strParam = enumParams.nextElement();

            String[] sLevels = strParam.replace("]", "").split("\\[");

            if (sLevels.length > 1) {
                if (isNumeric(sLevels[1])) {
                    Integer iCount = Integer.parseInt(sLevels[1]);

                    if (objRet.getList(sLevels[0]) == null) {
                        objRet.createList(sLevels[0]);
                    }

                    Integer iMakeUp = (iCount + 1) - objRet.getList(sLevels[0]).size();

                    if (iMakeUp > 0) {
                        for (int x = 0; x < iMakeUp; x++) {
                            objRet.getList(sLevels[0]).add(new ApiObject());
                        }
                    }

                    if (sLevels.length > 4) {
                        // No clue
                    } else if (sLevels.length == 4) {
                        objRet.getList(sLevels[0])
                                .get(iCount)
                                .setObject(sLevels[2], new ApiObject());
                        objRet.getList(sLevels[0]).get(iCount)
                                .getObject(sLevels[2])
                                .setString(sLevels[3],
                                        req.getParameter(strParam));
                    } else if (sLevels.length == 3) {
                        objRet.getList(sLevels[0]).get(iCount)
                                .setString(sLevels[2],
                                        req.getParameter(strParam));
                    }
                } else {
                    if (objRet.getObject(sLevels[0]) == null) {
                        objRet.setObject(sLevels[0], new ApiObject());
                    }

                    objRet.getObject(sLevels[0]).setString(sLevels[1], req.getParameter(strParam));
                }
            } else {
                objRet.setString(sLevels[0], req.getParameter(strParam));
            }
        }

        return objRet;
    }

    public static boolean isNumeric(String inputData) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(inputData, pos);
        return inputData.length() == pos.getIndex();
    }
}
