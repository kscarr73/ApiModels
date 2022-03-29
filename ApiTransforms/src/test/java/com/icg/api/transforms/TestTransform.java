package com.icg.api.transforms;

import com.progbits.api.model.ApiClass;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import com.progbits.api.model.ApiObjectDef;
import com.progbits.api.parser.JsonObjectParser;
import com.progbits.api.parser.ParserServiceImpl;
import com.progbits.api.parser.XmlObjectParser;
import com.progbits.api.transforms.JsonTransform;
import com.progbits.api.transforms.XmlTransform;
import com.progbits.api.transforms.XsdTransform;
import com.progbits.api.writer.JsonObjectWriter;
import com.progbits.api.writer.WriterServiceImpl;
import com.progbits.api.writer.XmlObjectWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class TestTransform {

    ParserServiceImpl _parser;
    WriterServiceImpl _writer;

    @BeforeClass
    public void start() {
        _parser = new ParserServiceImpl();

        XmlObjectParser xml = new XmlObjectParser();
        //xml.setFactory(new XMLInputFactoryImpl());
        Map<String, String> xmlProps = new HashMap<>();
        xmlProps.put("type", "XML");

        _parser.addParser(xml, xmlProps);

        JsonObjectParser jsonParse = new JsonObjectParser();
        Map<String, String> jsonProps = new HashMap<>();
        jsonProps.put("type", "JSON");

        _parser.addParser(jsonParse, jsonProps);

        _writer = new WriterServiceImpl();

        _writer.addParser(new JsonObjectWriter(), jsonProps);

        XmlObjectWriter xmlWrite = new XmlObjectWriter();

        //xmlWrite.setFactory(new XMLOutputFactoryImpl());
        _writer.addParser(xmlWrite, xmlProps);
    }

    @Test(enabled = true)
    public void testJson() throws Exception {
        ApiClasses apiClasses = ApiObjectDef.returnClassDef();
        ApiClass rtnAuth = apiClasses.getClassByName("apiClass");
        rtnAuth.setString("name", "returnAuthentication");
        rtnAuth.setString("className",
                "com.progbits.security.ws.ReturnAuthentication");
        rtnAuth.createList("fields");

        rtnAuth.getList("fields").add(apiClasses.getClassByName("apiField"));
        rtnAuth.getListLast("fields").setString("name", "partnerId");
        rtnAuth.getListLast("fields").setString("type", "String");
        rtnAuth.getListLast("fields").setString("desc",
                "Defines a class for performing logins to the Ingram Systems.");
        rtnAuth.getListLast("fields").setLong("min", 1L);
        rtnAuth.getListLast("fields").setLong("max", 1L);

        rtnAuth.getList("fields").add(apiClasses.getClassByName(
                "apiField"));
        rtnAuth.getListLast("fields").setString("name", "applicationId");
        rtnAuth.getListLast("fields").setString("type", "String");
        rtnAuth.getListLast("fields").setString("desc",
                "The Application ID Provided by Ingram that the Partner is trying to log into.");
        rtnAuth.getListLast("fields").setLong("min", 1L);
        rtnAuth.getListLast("fields").setLong("max", 1L);

        rtnAuth.getList("fields").add(apiClasses.getClassByName(
                "apiField"));
        rtnAuth.getListLast("fields").setString("name", "password");
        rtnAuth.getListLast("fields").setString("type", "String");
        rtnAuth.getListLast("fields").setString("desc",
                "The password assigned to the vendor");
        rtnAuth.getListLast("fields").setLong("min", 1L);
        rtnAuth.getListLast("fields").setLong("max", 1L);

        ApiClasses classes = new ApiClasses();
        classes.addClass(rtnAuth);

        ApiObject objRoot = classes.getInstance(
                "com.progbits.security.ws.ReturnAuthentication");
        objRoot.setString("partnerId", "H818");
        objRoot.setString("applicationId", "UsedBook");
        objRoot.setString("password", "ThisIsATest");

        String jsonClasses = JsonTransform.convertToJson(_writer, classes,
                rtnAuth);
        String xmlClasses = XmlTransform.convertToXml(_writer, classes, rtnAuth);

        String jsonString = JsonTransform.convertToJson(_writer, classes,
                objRoot);

        ApiObject retObj = JsonTransform.jsonToApiObject(_parser, classes,
                "com.progbits.security.ws.ReturnAuthentication", jsonString);

        String xmlString = XmlTransform.convertToXml(_writer, classes, objRoot);

        ApiObject retXml = XmlTransform.xmlToApiObject(_parser, classes,
                "com.progbits.security.ws.ReturnAuthentication", xmlString);

        retXml.getString("partnerId");
        retXml.getString("applicationId");
        retXml.getString("password");
    }

    @Test(enabled = true)
    public void testXml() throws Exception {
        testJson();
    }

    @Test(enabled = false)
    public void testJsonArrays() throws Exception {
        ApiClasses classes = new ApiClasses();

        ApiClass cls = new ApiClass();
        cls.setName("apiClass");

        cls.setString("name", "testArray");
        cls.createList("fields");
        ApiObject objFirstName = cls.getListAdd("fields");
        objFirstName.setName("apiField");
        objFirstName.setString("name", "firstName");
        objFirstName.setString("type", "String");
        objFirstName.setString("desc", "The PartnerID Assigned by Vendor");
        objFirstName.setLong("min", 1L);
        objFirstName.setLong("max", 1L);

        ApiObject objLastName = cls.getListAdd("fields");
        objLastName.setName("apiField");
        objLastName.setString("name", "lastName");
        objLastName.setString("type", "String");

        ApiObject objNumbers = cls.getListAdd("fields");
        objNumbers.setName("apiField");
        objNumbers.setString("name", "phoneNumbers");
        objNumbers.setString("type", "ArrayList");
        objNumbers.setString("subType", "phoneNumber");
        objNumbers.setString("desc", "List of Phone Numbers for this Contact");
        objNumbers.setLong("min", 0L);
        objNumbers.setLong("max", 0L);

        classes.addClass(cls);

        ApiClass phoneCls = new ApiClass();
        phoneCls.setName("apiClass");
        phoneCls.createList("fields");

        ApiObject objPhone = phoneCls.getListAdd("fields");
        objPhone.setName("apiField");
        objPhone.setString("name", "phone");
        objPhone.setString("type", "String");

        ApiObject objType = phoneCls.getListAdd("fields");

        objType.setName("apiField");
        objType.setString("name", "type");
        objType.setString("type", "String");

        classes.addClass(phoneCls);

        ApiObject objRoot = new ApiObject();
        objRoot.setName("testArray");
        objRoot.setString("firstName", "Scott");
        objRoot.setString("lastName", "Carr");

        ApiObject objPhoneHome = new ApiObject();
        objPhoneHome.setName("phoneNumber");
        objPhoneHome.setString("phone", "615-829-9122");
        objPhoneHome.setString("type", "Home");

        List<ApiObject> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(objPhoneHome);

        ApiObject objPhoneWork = new ApiObject();

        objPhoneWork.setName("phoneNumber");
        objPhoneWork.setString("phone", "615-621-9122");
        objPhoneWork.setString("type", "Work");

        phoneNumbers.add(objPhoneWork);

        objRoot.setArrayList("phoneNumbers", phoneNumbers);

        String jsonString = JsonTransform.convertToJson(_writer, classes,
                objRoot);

        ApiObject retObj = JsonTransform.jsonToApiObject(_parser, classes,
                "testArray", jsonString);

        String xmlString = XmlTransform.convertToXml(_writer, classes, objRoot);

        ApiObject retXml = XmlTransform.xmlToApiObject(_parser, classes,
                "testArray", xmlString);

    }

    @Test(enabled = true)
    public void testXsdTransform() throws Exception {
        String xsdTest = "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:tns=\"http://ws.security.isg.icg.com/\" attributeFormDefault=\"unqualified\" elementFormDefault=\"unqualified\" targetNamespace=\"http://ws.security.isg.icg.com/\">\n"
                + "<xs:element name=\"returnAuthentication\" type=\"tns:returnAuthentication\"/>\n"
                + "<xs:element name=\"returnAuthenticationResponse\" type=\"tns:returnAuthenticationResponse\"/>\n"
                + "<xs:complexType name=\"returnAuthentication\">\n"
                + "<xs:sequence>\n"
                + "<xs:element minOccurs=\"0\" name=\"partnerId\" type=\"xs:string\"/>\n"
                + "<xs:element minOccurs=\"0\" name=\"applicationId\" type=\"xs:string\"/>\n"
                + "<xs:element minOccurs=\"0\" name=\"password\" type=\"xs:string\"/>\n"
                + "</xs:sequence>\n"
                + "</xs:complexType>\n"
                + "<xs:complexType name=\"returnAuthenticationResponse\">\n"
                + "<xs:sequence>\n"
                + "<xs:element minOccurs=\"0\" name=\"return\" type=\"xs:string\"/>\n"
                + "</xs:sequence>\n"
                + "</xs:complexType>\n"
                + "<xs:element name=\"Exception\" type=\"tns:Exception\"/>\n"
                + "<xs:complexType name=\"Exception\">\n"
                + "<xs:sequence>\n"
                + "<xs:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"classContext\" type=\"xs:string\"/>\n"
                + "<xs:element minOccurs=\"0\" name=\"message\" type=\"xs:string\"/>\n"
                + "</xs:sequence>\n"
                + "</xs:complexType>\n"
                + "</xs:schema>";

        ApiObject xsdRet = XsdTransform.convertFromXsd(xsdTest,
                "com.progbits.testing");

        String json = JsonTransform.convertToJson(_writer, null, xsdRet);

    }

    @Test(enabled = true)
    public void testApiToWsdl() throws Exception {
        ApiClasses apiServices = ApiObjectDef.returnServiceDef();
        ApiClasses apiClasses = ApiObjectDef.returnClassDef();

        ApiObject apiService = apiServices.getInstanceByName("apiService");

        apiService.setString("name", "IsgSecurity");
        apiService.setString("packageName", "com.progbits.security.ws");
        apiService.setString("url", "/security");
        apiService.setString("desc", "Authentication methods for Ingram APIs");

        apiService.getListAdd("functions")
                .setString("name", "returnAuthentication")
                .setString("desc", "Authentication Method for Ingram APIs");

        ApiObject rtnAuth = apiClasses.getInstanceByName("apiClass");
        rtnAuth.setString("name", "returnAuthentication");
        rtnAuth.setString("className",
                "com.progbits.security.ws.ReturnAuthentication");

        rtnAuth.getListAdd("fields")
                .setString("name", "partnerId")
                .setString("type", "String")
                .setString("desc", "Defines a class for performing logins to the Ingram Systems.")
                .setLong("min", 1L)
                .setLong("max", 1L);

        rtnAuth.getList("fields").add(apiClasses.getInstanceByName("apiField"));
        rtnAuth.getListLast("fields").setString("name", "applicationId");
        rtnAuth.getListLast("fields").setString("type", "String");
        rtnAuth.getListLast("fields").setString("desc",
                "The Application ID Provided by Ingram that the Partner is trying to log into.");
        rtnAuth.getListLast("fields").setLong("min", 1L);
        rtnAuth.getListLast("fields").setLong("max", 1L);

        rtnAuth.getList("fields").add(apiClasses.getInstanceByName("apiField"));
        rtnAuth.getListLast("fields").setString("name", "password");
        rtnAuth.getListLast("fields").setString("type", "String");
        rtnAuth.getListLast("fields").setString("desc",
                "The password assigned to the vendor");
        rtnAuth.getListLast("fields").setLong("min", 1L);
        rtnAuth.getListLast("fields").setLong("max", 1L);

        ApiClass rtnAuthResp = new ApiClass(apiClasses.getInstanceByName("apiClass"));
        rtnAuthResp.setString("name", "returnAuthenticationResponse");
        rtnAuthResp.setString("className",
                "com.progbits.security.ws.ReturnAuthenticationResponse");
        rtnAuthResp.setString("desc", "AuthKey from a Valid Login");
        rtnAuthResp.createList("fields");

        rtnAuthResp.getList("fields").add(apiClasses.getInstanceByName("apiField"));
        rtnAuthResp.getListLast("fields").setString("name", "return");
        rtnAuthResp.getListLast("fields").setString("type", "String");
        rtnAuthResp.getListLast("fields").setString("desc",
                "AuthKey for Future calls to Ingram Services");
        rtnAuthResp.getListLast("fields").setLong("min", 1L);
        rtnAuthResp.getListLast("fields").setLong("max", 1L);

        ApiObject rtnExcep = apiClasses.getInstanceByName("apiClass");
        rtnExcep.setString("name", "Exception");
        rtnExcep.setString("className", "com.progbits.web.Exception");
        rtnExcep.setString("desc", "Default Exception for Errors");
        rtnExcep.createList("fields");

        rtnExcep.getListAdd("fields")
                .setString("name", "message")
                .setString("type", "String")
                .setString("desc", "Message of this Exception")
                .setLong("min", 1L)
                .setLong("max", 1L);

        try {
            ApiClasses classes = new ApiClasses();

            classes.addClass(new ApiClass(rtnExcep));
            classes.addClass(new ApiClass(rtnAuth));
            classes.addClass(rtnAuthResp);

            String wsdl = XsdTransform.convertToWsdl(
                    "http://apitest.ingramcontent.com/api/security", apiService,
                    classes);
            String wadl = XsdTransform.convertToWadl(
                    "http://apitest.ingramcontent.com/api/security", apiService,
                    classes);
            String html = XsdTransform.convertToHtml(
                    "http://apitest.ingramcontent.com/api/security", apiService,
                    classes);

            System.out.println(wsdl);

            System.out.println("WADL:  " + wadl);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            System.out.println(ex.getMessage());
        }

    }

    @Test(enabled = false)
    public void quickTest() {
        StringBuilder sb = new StringBuilder();

        String apiName = "MyTest";
        String fullUrl = "http://mytest.location";

        String apiRepl = "<wsdl:service name=\"%apiName%ImplService\">\n"
                + "<wsdl:port binding=\"tns:%apiName%ImplServiceSoapBinding\" name=\"%apiName%ImplPort\">\n"
                + "<soap:address location=\"";

        apiRepl = apiRepl.replace("apiName", apiName);

        sb.append(apiRepl);
        sb.append(fullUrl).append("\"/>\n"
                + "</wsdl:port>\n"
                + "</wsdl:service>");

        System.out.println(sb.toString());
    }

    @Test
    public void testAction() throws Exception {
        ApiObject obj = new ApiObject();

        obj.setString("_index", "mytest");
        obj.setString("_type", "header");

        ApiObject action = new ApiObject();

        obj.setString("_id", "thenewid");

        action.setObject("index", obj);

        String strJson = JsonTransform.convertToJson(_writer, null, action);
    }

    @Test
    public void testRootObjWrite() throws Exception {
        ApiObject obj = new ApiObject();

        obj.createList("root");

        obj.getListAdd("root")
                .setString("myfield", "Other")
                .setString("this", "that");

        obj.getListAdd("root")
                .setString("this", "that")
                .setString("mywonder", "test");

        JsonObjectWriter writer = new JsonObjectWriter(true);

        String strResp = writer.writeSingle(obj);

        assert strResp != null;
    }

    @Test
    public void testRootObjWriteWithSubObject() throws Exception {
        ApiObject obj = new ApiObject();

        obj.createList("root");

        ApiObject obj1 = obj.getListAdd("root");

        obj1.setString("myfield", "Other")
                .setString("this", "that");

        ApiObject obj2 = obj.getListAdd("root");

        obj2.setString("this", "that")
                .setString("mywonder", "test");

        obj2.createObject("testing");
        obj2.getObject("testing")
                .setString("inside", "outside");

        JsonObjectParser parser = new JsonObjectParser(true);
        JsonObjectWriter writer = new JsonObjectWriter(true);

        String strResp = writer.writeSingle(obj);

        assert strResp != null;
        assert strResp.contains("\"inside\":\"outside\"");
        ApiObject parsedObj = parser.parseSingle(new StringReader(strResp));

    }

    @Test
    public void testRootObjParse() throws Exception {
        String strToParse = "[ { \"start\": \"This\", \"field2\": \"value\" }, "
                + "{ \"start\": \"other3\", \"field2\": \"other3\" } ]";

        JsonObjectParser parser = new JsonObjectParser(true);

        ApiObject obj = parser.parseSingle(new StringReader(strToParse));

        assert obj != null;
        assert obj.getString("root[1].field2").equals("other3");
    }
}
