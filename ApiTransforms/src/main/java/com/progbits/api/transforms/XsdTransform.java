package com.progbits.api.transforms;

import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClass;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author scarr
 */
public class XsdTransform {

    public static String DEFAULT_EXCEPTION = "com.progbits.web.Exception";

    public static ApiObject convertFromXsd(String xsd, String pkgName) throws Exception {
        ApiObject retObject = new ApiObject();
        retObject.createList("types");

        XMLStreamReader in = XMLInputFactory.newInstance()
                .createXMLStreamReader(new StringReader(xsd));

        ApiObject curObject = null;
        boolean inComplexType = false;

        while (in.hasNext()) {
            int iCurEvent = in.next();

            if (iCurEvent == XMLStreamReader.START_ELEMENT) {
                String key = in.getLocalName();

                if (key.contains("schema")) {

                } else if (key.contains("element")) {
                    String typeAttr = in.getAttributeValue(null, "type");

                    if (curObject == null && typeAttr == null) {
                        curObject = new ApiObject();
                        curObject.setName("apiClass");
                        curObject.setString("name", in.getAttributeValue(null,
                                "name"));
//                        curObject.setString("className", pkgName + "." + CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_CAMEL, curObject.getString("name")));
                        curObject.createList("fields");
                    } else if (inComplexType) {
                        // Anything else should be valid
                        ApiObject fieldObj = new ApiObject();

                        fieldObj.setName("apiField");

                        String strTemp = in.getAttributeValue(null, "name");
                        fieldObj.setString("name", strTemp);

                        String max = in.getAttributeValue(null, "maxOccurs");
                        String min = in.getAttributeValue(null, "minOccurs");

                        if (min != null && !min.isEmpty()) {
                            fieldObj.setLong("min", Long.parseLong(min));
                        } else {
                            fieldObj.setLong("min", 1L);
                        }

                        strTemp = in.getAttributeValue(null, "type");

                        String[] entries = strTemp.split(":");

                        if (max != null && ("unbounded".equals(max) || Integer.
                                parseInt(max) > 0)) {
                            fieldObj.setLong("max", 0L);
                            fieldObj.setString("type", "ArrayList");
                        } else if ("xs".equals(entries[0])) {
                            fieldObj.setString("type", entries[1]);
                        } else {
                            // Assume anything else is tns
                            //fieldObj.setString("subType", pkgName + "." + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, entries.get(1)));
                        }

                        curObject.getList("fields").add(fieldObj);
                    }
                } else if (key.contains("complexType")) {
                    inComplexType = true;
                    if (curObject == null) {
                        curObject = new ApiObject();
                        curObject.setName("apiClass");
                        curObject.setString("name", in.getAttributeValue(null,
                                "name"));
                        //curObject.setString("className", pkgName + "." + CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_CAMEL, curObject.getString("name")));
                        curObject.createList("fields");
                    }
                } else if (key.contains("sequence")) {

                } else {

                }
            } else if (iCurEvent == XMLStreamReader.END_ELEMENT) {
                String key = in.getLocalName();

                if (key.equals("schema")) {
                    break;
                } else if (key.equals("complexType")) {
                    inComplexType = false;
                    retObject.getList("types").add(curObject);

                    curObject = null;
                }
            }
        }

        return retObject;
    }

    /**
     *
     * @param clsObjects Should be the class com.progbits.xsd.Input
     * @param targetNamespace
     * @return
     */
    public static String convertToXsd(ApiClasses clsObjects,
            String targetNamespace) throws ApiException {
        StringBuilder sb = new StringBuilder();
        String strElementTemplate = "<xs:element name=\"%elename%\" type=\"tns:%elename%\" />\n"
                + "    <xs:complexType name=\"%elename%\">\n"
                + "        <xs:sequence>";

        sb.append("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" ")
                .append("xmlns:tns=\"").append(targetNamespace).append("\" ")
                .append("attributeFormDefault=\"unqualified\" ")
                .append("elementFormDefault=\"unqualified\" ")
                .append("targetNamespace=\"").append(targetNamespace).
                append("\">\n");

        try {
            for (ApiObject apiClass : clsObjects.getClassList()) {
                sb.append(strElementTemplate.replace("%elename%", apiClass.
                        getString(
                                "name")));

                for (ApiObject fld : apiClass.getList("fields")) {

                    sb.append("<xs:element name=\"")
                            .append(fld.getString("name"))
                            .append("\" ");

                    String strFldType = fld.getString("type", "String").
                            toLowerCase();

                    switch (strFldType) {
                        case "string":
                            sb.append("type=\"xs:string\" ");
                            break;

                        case "stringarray":
                            sb.append("type=\"xs:string\" ");
                            break;

                        case "datetime":
                            sb.append("type=\"xs:dateTime\" ");
                            break;

                        case "integer":
                            sb.append("type=\"xs:integer\" ");
                            break;
                        case "long":
                            sb.append("type=\"xs:long\" ");
                            break;
                        case "double":
                            sb.append("type=\"xs:decimal\" ");
                            break;
                        case "boolean":
                            sb.append("type=\"xs:boolean\" ");
                            break;
                        case "object":
                            ApiClass objSub = clsObjects.getClass(fld.getString(
                                    "subType"));

                            if (objSub == null) {
                                throw new ApiException(
                                        "Class: " + apiClass.getString("name") + " Field: " + fld.
                                        getString("name") + " Sub Object: [" + fld.
                                        getString(
                                                "subType") + "] is Not Found", null);
                            }

                            String subTypeName = objSub.getString("name");

                            sb.append("type=\"tns:").append(subTypeName).append(
                                    "\" ");
                            break;
                        case "arraylist":
                            ApiClass objSub2 = clsObjects.getClass(fld.getString(
                                    "subType"));

                            if (objSub2 == null) {
                                throw new ApiException(
                                        "SubType: " + fld.getString("subType") + " Doesn't Exist",
                                        null);
                            }

                            String subTypeName2 = objSub2.getString("name");

                            sb.append("type=\"tns:").append(subTypeName2).append(
                                    "\" ");
                            break;

                        default:
                            sb.append("type=\"xs:string\" ");
                            break;
                    }

                    sb.append("minOccurs=\"").append(fld.getLong("min")).append(
                            "\" ");

                    if (fld.getLong("max", 0L) == 0 && strFldType.contains("array")) {
                        sb.append("maxOccurs=\"unbounded\" ");
                    } else if (fld.getLong("max", 0L) > 0 && strFldType.contains(
                            "array")) {
                        sb.append("maxOccurs=\"").append(fld.getLong("max", 0L)).
                                append("\" ");
                    } else if ("object".equals(strFldType)) {
                        sb.append("maxOccurs=\"1\" ");
                    }

                    sb.append("/>\n");
                }

                sb.append("    </xs:sequence>\n");
                sb.append("  </xs:complexType>\n");
            }
        } catch (ApiClassNotFoundException ex) {
            throw new ApiException(550, ex.getMessage());
        }

        sb.append("</xs:schema>");

        return sb.toString();
    }

    public static String convertToWsdl(String fullUrl, ApiObject apiService,
            ApiClasses classes) throws ApiException, ApiClassNotFoundException {
        String strTemplate = null;
        String strWsdlMsgTemp = "<wsdl:message name=\"%1$s\">\n"
                + "<wsdl:part element=\"tns:%1$s\" name=\"parameters\"></wsdl:part>\n"
                + "</wsdl:message>\n";
        String strWsdlOperationTemp = "<wsdl:operation name=\"%1$s\">\n"
                + "<wsdl:input message=\"tns:%2$s\" name=\"%2$s\"></wsdl:input>\n"
                + "<wsdl:output message=\"tns:%3$s\" name=\"%3$s\"></wsdl:output>\n"
                + "<wsdl:fault message=\"tns:%4$s\" name=\"Exception\"></wsdl:fault>\n"
                + "</wsdl:operation>";
        String strWsdlOpBindingTemplate = "<wsdl:operation name=\"%1$s\">\n"
                + "<soap:operation soapAction=\"\" style=\"document\"/>\n"
                + "<wsdl:input name=\"%2$s\">\n"
                + "<soap:body use=\"literal\"/>\n"
                + "</wsdl:input>\n"
                + "<wsdl:output name=\"%3$s\">\n"
                + "<soap:body use=\"literal\"/>\n"
                + "</wsdl:output>\n"
                + "<wsdl:fault name=\"%4$s\">\n"
                + "<soap:fault name=\"%4$s\" use=\"literal\"/>\n"
                + "</wsdl:fault>\n"
                + "</wsdl:operation>\n";
        String strWsdlServiceTemplate = "<wsdl:service name=\"%1$s\">\n"
                + "<wsdl:port binding=\"tns:%1$sSoapBinding\" name=\"%2$sPort\">\n"
                + "<soap:address location=\"%3$s\"/>\n"
                + "</wsdl:port>\n"
                + "</wsdl:service>\n"
                + "</wsdl:definitions>";

        String apiType = apiService.getString("name").replace(" ", "");
        String apiName = apiType + "Service";
        String apiBinding = apiName + "SoapBinding";

        StringBuilder sb = new StringBuilder(10000);

        String nameSpace = "http://" + reversePackageName(apiService.getString(
                "packageName")) + "/";

        sb.append("<?xml version='1.0' encoding='UTF-8'?>\n");

        sb.append(
                "<wsdl:definitions \nxmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" \n");
        sb.append("xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" \n");
        sb.append("xmlns:tns=\"").append(nameSpace);
        sb.
                append("\" \nxmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" \n");
        sb.append("xmlns:ns1=\"http://schemas.xmlsoap.org/soap/http\" \n");
        sb.append("name=\"").append(apiName).append("\" ");

        sb.append("targetNamespace=\"").append(nameSpace);
        sb.append("\">\n");
        sb.append("  <wsdl:types>\n");

        sb.append(convertToXsd(classes, nameSpace));

        sb.append("</wsdl:types>");

        StringBuilder sbMsgs = new StringBuilder();
        StringBuilder sbPortType = new StringBuilder();
        StringBuilder sbBinding = new StringBuilder();

        sbBinding.append("<wsdl:binding name=\"").append(apiBinding)
                .append("\" type=\"tns:").append(apiType).append("\">\n");

        sbBinding.append(
                "<soap:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>\n");

        sbPortType.append("<wsdl:portType name=\"");
        sbPortType.append(apiType);
        sbPortType.append("\">\n");

        List<String> arrNames = new ArrayList<>();

        int iCnt = 0;
        for (ApiObject function : apiService.getList("functions")) {
            String funcName = function.getString("name");
            String packageName = apiService.getString("packageName");

            String inboundClassDef = packageName + "." + funcName.
                    substring(0, 1).
                    toUpperCase() + funcName.substring(1);
            String outboundClassDef = inboundClassDef + "Response";
            String exceptionClassDef = DEFAULT_EXCEPTION;

            ApiObject inboundClass = classes.getClass(inboundClassDef);
            ApiObject outboundClass = classes.getClass(outboundClassDef);
            ApiObject exceptionClass = classes.getClass(exceptionClassDef);

            if (inboundClass != null) {
                if (!arrNames.contains(inboundClass.getString("name"))) {
                    sbMsgs.append(String.format(strWsdlMsgTemp, inboundClass.
                            getString(
                                    "name")));

                    arrNames.add(inboundClass.getString("name"));
                }
            } else {
                throw new ApiException(
                        "inboundClass: " + inboundClassDef + " does not exist.",
                        null);
            }

            if (outboundClass != null) {
                if (!arrNames.contains(outboundClass.getString("name"))) {
                    sbMsgs.append(String.format(strWsdlMsgTemp, outboundClass.
                            getString(
                                    "name")));

                    arrNames.add(outboundClass.getString("name"));
                }
            } else {
                throw new ApiException(
                        "outboundClass: " + outboundClassDef + " does not exist.",
                        null);
            }

            if (exceptionClass != null) {
                if (!arrNames.contains(exceptionClass.getString("name"))) {
                    String exception = String.format(strWsdlMsgTemp,
                            exceptionClass.
                                    getString("name"));
                    exception = exception.replace("parameters", "Exception");

                    sbMsgs.append(exception);

                    arrNames.add(exceptionClass.getString("name"));
                }
            } else {
                throw new ApiException(
                        "exceptionClass: " + exceptionClassDef + " does not exist.", null);
            }

            sbPortType.append(
                    String.format(strWsdlOperationTemp,
                            funcName, inboundClass.getString("name"),
                            outboundClass.getString("name"),
                            exceptionClass.getString("name")
                    )
            );

            sbBinding.append(String.format(strWsdlOpBindingTemplate, funcName,
                    inboundClass.getString("name"), outboundClass.getString(
                    "name"),
                    exceptionClass.getString("name")));

            iCnt++;
        }

        sbPortType.append("</wsdl:portType>");
        sbBinding.append("</wsdl:binding>");

        sb.append(sbMsgs);
        sb.append(sbPortType);
        sb.append(sbBinding);

        sb.append(String.format(strWsdlServiceTemplate, apiName, apiType,
                fullUrl));

        return sb.toString();
    }

    public static String convertToWadl(String fullUrl, ApiObject apiService,
            ApiClasses classes) throws ApiException {
        StringBuilder sb = new StringBuilder();

        String strResourceTemplate = "<resource path=\"/%funcName%\">\n"
                + "            <method id=\"%funcName%_POST\" name=\"POST\">\n"
                + "                <request>\n"
                + "                    <representation mediaType=\"application/xml\" element=\"tns:%requestObject%\"/>\n"
                + "                    <representation mediaType=\"application/json\" element=\"tns:%requestObject%\"/>\n"
                + "                </request>\n"
                + "                <response status=\"200\">\n"
                + "                    <representation mediaType=\"application/xml\" element=\"tns:%responseObject%\"/>\n"
                + "                    <representation mediaType=\"application/json\" element=\"tns:%responseObject%\"/>\n"
                + "                </response>\n"
                + "                <response status=\"400\">\n"
                + "                    <representation mediaType=\"application/xml\" element=\"tns:%exceptionObject%\"/>\n"
                + "                    <representation mediaType=\"application/json\" element=\"tns:%exceptionObject%\"/>\n"
                + "                </response>\n"
                + "                <response status=\"500\">\n"
                + "                    <representation mediaType=\"application/xml\" element=\"tns:%exceptionObject%\"/>\n"
                + "                    <representation mediaType=\"application/json\" element=\"tns:%exceptionObject%\"/>\n"
                + "                </response>\n"
                + "            </method>\n"
                + "        </resource>";

        sb.append(
                "<application xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n "
                + "xmlns:tns=\"" + apiService.getString("namespace") + "\" "
                + "             xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n"
                + "             xmlns=\"http://wadl.dev.java.net/2009/02\">\n");

        sb.append("<grammars>");

        String nameSpace = "http://" + reversePackageName(apiService.getString(
                "packageName")) + "/";

        sb.append(convertToXsd(classes, nameSpace));

        sb.append("</grammars>\n");

        sb.append("<resources base=\"").append(fullUrl).append("\">\n");

        try {
            for (ApiObject function : apiService.getList("functions")) {
                String funcName = function.getString("name");
                String packageName = apiService.getString("packageName");

                String inboundClassDef = packageName + "." + funcName.
                        substring(0, 1).
                        toUpperCase() + funcName.substring(1);
                String outboundClassDef = inboundClassDef + "Response";
                String exceptionClassDef = DEFAULT_EXCEPTION;

                ApiObject inboundClass = classes.getClass(inboundClassDef);
                ApiObject outboundClass = classes.getClass(outboundClassDef);
                ApiObject exceptionClass = classes.getClass(exceptionClassDef);

                String strResource = strResourceTemplate.replace("%funcName%",
                        function.
                                getString("name"));

                if (inboundClass != null) {
                    strResource = strResource.replace("%requestObject%",
                            inboundClass.
                                    getString("name"));
                } else {
                    throw new ApiException(
                            "inboundClass: " + inboundClassDef + " does not exist.",
                            null);
                }

                if (outboundClass != null) {
                    strResource = strResource.replace("%responseObject%",
                            outboundClass.
                                    getString("name"));
                } else {
                    throw new ApiException(
                            "outboundClass: " + outboundClassDef + " does not exist.",
                            null);
                }

                if (exceptionClass != null) {
                    strResource = strResource.replace("%exceptionObject%",
                            exceptionClass.
                                    getString("name"));
                } else {
                    throw new ApiException(
                            "exceptionClass: " + exceptionClassDef + " does not exist.",
                            null);
                }

                sb.append(strResource);
            }
        } catch (ApiClassNotFoundException ex) {
            throw new ApiException(550, ex.getMessage());
        }

        sb.append("</resources>\n");
        sb.append("</application>\n");

        return sb.toString();
    }

    public static String convertToHtml(String fullUrl, ApiObject apiService,
            ApiClasses classes) throws ApiException {
        StringBuilder sb = new StringBuilder();

        sb.append("<html>\n");
        sb.append("<body>\n");

        sb.append("<div class=\"classDef\">");

        sb.append("<h1>").append("Name: &nbsp;").
                append(apiService.getString("name")).append("</h1>");
        String nameSpace = "http://" + reversePackageName(apiService.getString(
                "packageName")) + "/";
        sb.append("<h3>").append("Name Space: &nbsp;").append(nameSpace).append(
                "</h3>");
        sb.append("<h3>").append("Url: &nbsp;").append(fullUrl).
                append("</h3>\n");

        sb.append("<h2>Functions</h2>");

        for (ApiObject func : apiService.getList("functions")) {
            String funcName = func.getString("name");
            String packageName = apiService.getString("packageName");

            String inboundClassDef = packageName + "." + funcName.
                    substring(0, 1).
                    toUpperCase() + funcName.substring(1);
            String outboundClassDef = inboundClassDef + "Response";
            String exceptionClassDef = DEFAULT_EXCEPTION;

            sb.append("<h2>Function: ").append(funcName).append("</h2>");

            sb.append("<div class=\"funcDesc\">").append(func.getString("desc")).
                    append("</div>");

            sb.append("<h3>Request</h3>");

            sb.append(convertClassToHtml(inboundClassDef, classes));

            sb.append("<h3>Response</h3>");

            sb.append(convertClassToHtml(outboundClassDef, classes));

            sb.append("<h3>Exception</h3>");

            sb.append(convertClassToHtml(exceptionClassDef, classes));
        }

        sb.append("</div>");
        sb.append("</body>\n");
        sb.append("</html>\n");

        return sb.toString();
    }

    public static String convertClassToHtml(String curClass, ApiClasses classes) throws ApiException {
        StringBuilder sb = new StringBuilder();

        try {
            ApiClass thisClass = classes.getClass(curClass);

            if (thisClass == null) {
                return "";
            }

            sb.append("<h4>").append(thisClass.getString("name")).append("</h4>\n");
            //sb.append("<div class=\"className\">").append(thisClass.getString("className")).append("</div>\n");
            sb.append("<div class=\"classEntry\">").append(thisClass.getString(
                    "desc")).
                    append("</div>");

            sb.append("<table>");

            sb.append(
                    "<thead><tr><th>Field Name</th><th>Field Type</th><th>Required</th><th>Description</th></tr></thead>");

            sb.append("<tbody>");

            for (ApiObject fld : thisClass.getList("fields")) {
                sb.append("<tr>");

                sb.append("<td>").append(fld.getString("name")).append("</td>\n");
                sb.append("<td>").append(fld.getString("type")).append("</td>\n");
                sb.append("<td>");

                if (fld.getLong("min") != null && fld.getLong("min") == 1) {
                    sb.append("required");
                } else {
                    sb.append("optional");
                }

                sb.append("</td>\n");

                sb.append("<td>").append(fld.getString("desc")).append("</td>\n");

                sb.append("</tr>");

                if ("arraylist".equalsIgnoreCase(fld.getString("type")) || "object".
                        equalsIgnoreCase(fld.getString("type"))) {
                    sb.append("<tr><td colspan=\"4\">");
                    sb.append(convertClassToHtml(fld.getString("subType"), classes));
                    sb.append("</td></tr>");
                }
            }
        } catch (ApiClassNotFoundException ex) {
            throw new ApiException(550, ex.getMessage());
        }
        sb.append("</tbody>");

        sb.append("</table>");

        return sb.toString();
    }

    public static String reversePackageName(String packageName) {
        String[] sEntries = packageName.split("\\.");
        StringBuilder tb = new StringBuilder(1000);

        for (int x = sEntries.length - 1; x >= 0; x--) {
            if (x < sEntries.length - 1) {
                tb.append(".");
            }

            tb.append(sEntries[x]);
        }

        return tb.toString();
    }
}
