package com.progbits.api.srv.formatter;

import com.progbits.api.model.ApiClass;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import com.progbits.api.transforms.Transform;
import com.progbits.api.utils.oth.ApiUtilsInterface;

/**
 *
 * @author scarr
 */
public class ElasticSearchV5Formatter {

	private ApiUtilsInterface m_api;

	public void setApiUtils(ApiUtilsInterface api) {
		m_api = api;
	}

	public String format(ApiClasses classes, String mainClass) throws Exception {
		ApiObject retObj = new ApiObject();

		ApiClass subjClass = classes.getClass(mainClass);

		retObj.setString("template",
				  subjClass.getString("name").toLowerCase() + "-*");

		retObj.createObject("settings");
		retObj.getObject("settings").setInteger("number_of_shards", 1);
		retObj.getObject("settings").setInteger("number_of_replicas", 0);

		retObj.createObject("mappings");

		retObj.getObject("mappings").setObject("all", generateMapping(classes,
				  mainClass));

		return Transform.toString(m_api.getWriter().getWriter("JSON"),
				  null, retObj);
	}

	public ApiObject generateMapping(ApiClasses classes, String thisClass) throws Exception {
		ApiObject retProps = new ApiObject();
		final ApiObject mappings = new ApiObject();

		ApiClass subjClass = classes.getClass(thisClass);

		subjClass.getList("fields").forEach((fld) -> {
			ApiObject fldType = new ApiObject();

			switch (fld.getString("type")) {
				case "String":
					fldType.setString("type", "keyword");
					break;

				case "Decimal":
					fldType.setString("type", "double");
					break;

				case "DateTime":
					fldType.setString("format", "dateOptionalTime");
					fldType.setString("type", "date");
					break;

				case "ArrayList":
				case "Object":
					try {
						fldType = generateMapping(classes, fld.getString("subType"));
					} catch (Exception ex) {

					}
					break;

				default:
					fldType.setString("type", fld.getString("type").toLowerCase());
					break;
			}

			mappings.setObject(fld.getString("name"), fldType);
		});

		retProps.setObject("properties", mappings);
		return retProps;
	}
}
