package com.progbits.api.utils;

import com.progbits.api.ApiInt;
import com.progbits.api.model.ApiClass;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import com.progbits.api.parser.ParserServiceImpl;
import com.progbits.api.testing.ReturnServices;
import com.progbits.api.transforms.JsonTransform;
import com.progbits.api.transforms.XmlTransform;
import com.progbits.api.transforms.XsdTransform;
import com.progbits.api.writer.WriterServiceImpl;
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
public class RunApiUtils {

	ApiUtils api;
	ParserServiceImpl _parser;
	WriterServiceImpl _writer;

	private Map<String, ApiObject> _apis = new HashMap<>();
	private Map<String, ApiClasses> _classes = new HashMap<>();
	private Map<String, ApiInt> _impl = new HashMap<>();

	@BeforeClass
	public void start() throws Exception {
		api = (ApiUtils) ReturnServices.returnApiUtils(
				"http://lvicisgnosq01.ingramcontent.com:9200/");

		//api.setup();
	}

	@Test(enabled = false)
	public void testSystem() throws Exception {
		ApiUtils api = new ApiUtils();

		Map<String, ApiClass> classes = new HashMap<>();

		Map<String, ApiObject> apis = api.getApiServices("default");

	}

	@Test(enabled = true)
	public void testApi() throws Exception {
		ApiClasses classes = new ApiClasses();

		api.retrieveClasses("default", "com.progbits.security.ws.ReturnAuthentication",
				classes);

		ApiObject rtnAuth = classes.getInstanceByName("returnAuthentication");

	}

	@Test(enabled = false)
	public void sendBookInstanceJson() throws Exception {
		ApiClasses classes = new ApiClasses();

		api.retrieveClasses("default", "com.progbits.usedbook.ws.SendBookInstance", classes);

		String strXsd = XsdTransform.convertToXsd(classes,
				"http://www.ingramcontent.com/usedbook");

		ApiObject bie = classes.getInstance(
				"com.progbits.usedbook.ws.SendBookInstance");

		bie.setName("sendBookInstance");

		bie.setString("authKey", "somevalue");
		bie.setString("referenceId", "WhatScottWanted");
		bie.createList("bookInstance");

		bie.getListAdd("bookInstance");
		bie.getListLast("bookInstance").setName("bookInstance");
		bie.getListLast("bookInstance").setString("bookInstanceId", "1614164");
		bie.getListLast("bookInstance").setString("ean", "9781513151");
		bie.getListLast("bookInstance").setString("facilityOwnedInventory",
				"true");
		bie.getListLast("bookInstance").setString("facilityOwnedType", "H");

		String jsonString = JsonTransform.convertToJson(_writer, classes, bie);
		String xmlString = XmlTransform.convertToXml(_writer, classes, bie);

		System.out.println("Here");
	}

	@Test(enabled = false)
	public void testBi() throws Exception {
		ApiClasses classes = new ApiClasses();

		api.retrieveClasses("default","com.progbits.usedbook.bi.bookinstancereport.Message",
				classes);

		System.out.println("Test");

		String strTest = XsdTransform.convertToHtml(
				"com.progbits.3pl.bi.bookInstanceReport.message", null, classes);

		System.out.println(strTest);
	}

	@Test(enabled = true)
	public void testWebServices() throws Exception {
		List<String> apiFound = new ArrayList<>();

		_apis.clear();
		_classes.clear();

		_apis = api.getApiServices("default");

		for (Map.Entry<String, ApiObject> service : _apis.entrySet()) {
			try {
				ApiClasses funcClasses = new ApiClasses();

				api.getApiClasses("default", service.getValue(), funcClasses);

				_classes.put(service.getKey(), funcClasses);

				apiFound.add(service.getKey() + " " + service.getValue().
						getString("URL"));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	@Test(enabled = false)
	public void testLegacy() throws Exception {
		ApiClasses classes = new ApiClasses();

		api.retrievePackage("default", "com.progbits.usedbook.bi.legacy", classes);

		System.out.println("Test");

		String strTest = XsdTransform.convertToHtml(
				"com.progbits.3pl.bi.bookInstanceReport.message", null, classes);

		System.out.println(strTest);
	}

	@Test(enabled = false)
	public void testUsedBook() throws Exception {
		ApiClasses classes = new ApiClasses();

		api.retrievePackage("default", "com.progbits.usedbook", classes);

		System.out.println("Test");

		String strTest = XsdTransform.convertToHtml(
				"com.progbits.3pl.bi.bookInstanceReport.message", null, classes);

		System.out.println(strTest);
	}

	@Test(enabled = false)
	public void testLogData() throws Exception {
		ApiClasses classes = new ApiClasses();

		api.retrievePackage("default", "com.progbits.log.LogData", classes);

		ApiObject apiProfile = classes.getInstance("com.progbits.log.LogProfile");

	}

	@Test(enabled = false)
	public void testPullPackage() throws Exception {

	}

	@Test(enabled = false)
	public void testImportStatement() throws Exception {
		String strSource = "import 'com.progbits.test.Testing';\n"
				+ "import 'com.progbits.second.Test';\n"
				+ "\n"
				+ "// This is a test;\n"
				+ "Something = else;";
		String strReturn = api.replaceImports(strSource);
	}

}
