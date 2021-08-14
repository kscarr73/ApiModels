package com.progbits.api.parseimport.imports;

import com.progbits.api.ObjectParser;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiObject;
import com.progbits.api.parseimport.ImportParser;
import com.progbits.api.parser.JsonObjectParser;
import com.progbits.api.srv.ApiWebServlet;
import com.progbits.api.utils.oth.ApiUtilsInterface;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author scarr
 */
@Component(name = "JsonSchemaImporter", immediate = true, property
		= {
			"type=JSONSCHEMA", "name=JsonSchemaImporter"
		})
public class JsonSchemaImporter implements ImportParser {

	private ApiUtilsInterface _apiUtils;
	private ObjectParser _parseJson;

	@Reference
	public void setApiUtils(ApiUtilsInterface api) {
		_apiUtils = api;

		_parseJson = new JsonObjectParser(true);
	}

	@Override
	public ApiObject parseImport(String packagePrefix, ApiWebServlet apiSrv, InputStream is) throws ApiException {
		ApiObject retObj = new ApiObject();

		retObj.createList("rows");

		try {
			ApiObject objReq = _parseJson.parseSingle(new InputStreamReader(is));

			ApiObject objCls = new ApiObject();

			String strTitle = objReq.getString("title");

			objCls.setString("name", strTitle);
			objCls.setString("desc", objReq.getString("description"));
			objCls.setString("className", packagePrefix + "." + strTitle);
			objCls.setString("classType", "Model");

			processFields(packagePrefix, objCls, objReq.getStringArray("required"), objReq.getObject("properties"));

			retObj.getList("rows").add(objCls);

			if (objReq.containsKey("definitions")) {
				objReq.getObject("definitions").entrySet().forEach((entry) -> {
					retObj.getList("rows").add(processObject(packagePrefix, entry.getKey(), (ApiObject) entry.getValue()));
				});
			}

			retObj.getList("rows").forEach((entry) -> {
				try {
					_apiUtils.saveApiModel(entry);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});

			return retObj;
		} catch (ApiClassNotFoundException clsEx) {
			throw new ApiException("Class Not Found Exception: " + clsEx.getMessage(), clsEx);
		}
	}

	private void processFields(String packagePrefix, ApiObject objSubject, List<String> required, ApiObject lstProps) {
		if (lstProps != null) {
			objSubject.createList("fields");

			lstProps.entrySet().forEach((entry) -> {
				ApiObject objNewFld = new ApiObject();

				objNewFld.setString("name", entry.getKey());
				objNewFld.setString("DisplayName", entry.getKey());

				ApiObject objFldDef = (ApiObject) entry.getValue();

				if (objFldDef.containsKey("$ref")) {
					objNewFld.setString("type", "Object");

					objNewFld.setString("subType", packagePrefix + "." + processRef(objFldDef.getString("$ref")));
				} else {
					objNewFld.setString("type", returnFieldType(objFldDef.getString("type")));
					objNewFld.setString("desc", objFldDef.getString("description"));

					if ("ArrayList".equals(objNewFld.getString("type"))) {
						ApiObject itemsObj = objFldDef.getObject("items");

						if (itemsObj != null) {
							if (itemsObj.getString("$ref") != null) {
								objNewFld.setString("subType", packagePrefix + "." + processRef(itemsObj.getString("$ref")));
							} else if ("string".equals(itemsObj.getString("type"))) {
								objNewFld.setString("type", "StringArray");
							}
						}
					}

					objNewFld.setInteger("min", objFldDef.getInteger("minimum"));
				}

				if (required != null) {
					if (required.contains(entry.getKey())) {
						objNewFld.setInteger("min", 1);
					}
				}

				objSubject.getList("fields").add(objNewFld);
			});
		}
	}

	private String processRef(String ref) {
		int iLoc = ref.lastIndexOf("/");

		return ref.substring(iLoc + 1);
	}

	private String returnFieldType(String type) {
		switch (type) {
			case "string":
				return "String";
			case "boolean":
				return "Boolean";
			case "integer":
				return "Integer";
			case "object":
				return "Object";
			case "number":
				return "Double";
			case "array":
				return "ArrayList";
		}

		return null;
	}

	private ApiObject processObject(String packagePrefix, String strTitle, ApiObject objReq) {
		ApiObject retCls = new ApiObject();

		retCls.setString("name", strTitle);
		retCls.setString("desc", objReq.getString("description"));
		retCls.setString("className", packagePrefix + "." + strTitle);
		retCls.setString("classType", "Model");

		processFields(packagePrefix, retCls, objReq.getStringArray("required"), objReq.getObject("properties"));

		return retCls;
	}
}
