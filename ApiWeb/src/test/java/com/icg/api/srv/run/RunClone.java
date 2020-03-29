package com.icg.api.srv.run;
//
//import com.progbits.api.elastic.query.EsSearch;
//import com.progbits.api.elastic.query.RegExpQuery;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//
///**
// *
// * @author scarr
// */
//public class RunClone {
//
//	private ApiUtilsInterface _api;
//
//	@BeforeClass
//	public void setup() {
//		_api = ReturnServices.returnApiUtils(
//				"http://localhost:9200"
//		);
//	}
//
//	@Test
//	public void runClone() throws Exception {
//		EsSearch search = new EsSearch();
//
//		search.getQuery().setQuery(new RegExpQuery("className",
//				"com.icg.isg.log.ws.*"));
//
//		search.setCount(1000);
//
//		ApiObject retObj = _api.getElasticUtils().getSearchRecords("apiclasses",
//				null, null, search);
//
//		if (retObj != null && retObj.isSet("hits")) {
//			retObj.getList("hits").forEach((row) -> {
//				ApiObject source = row.getObject("_source");
//
//				source.setString("className", source.getString("className").
//						replace("log.ws", "log.v2.ws"));
//
//				source.getList("fields").forEach((field) -> {
//					if (field.isSet("subType")) {
//						if (field.getString("subType").contains("log.ws")) {
//							field.setString("subType", field.
//									getString("subType").replace("log.ws",
//									"log.v2.ws"));
//						}
//					}
//				});
//
//				try {
//					_api.getElasticUtils().
//							saveRecord("apiclasses", "apiclass", null, source);
//				} catch (ICGAppException | ICGSystemException app) {
//					app.printStackTrace();
//				}
//			});
//		}
//	}
//}
