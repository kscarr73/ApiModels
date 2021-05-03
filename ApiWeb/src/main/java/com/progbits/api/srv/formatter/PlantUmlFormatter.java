package com.progbits.api.srv.formatter;

import com.progbits.api.model.ApiClass;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
public class PlantUmlFormatter {

	private Logger LOG = LoggerFactory.getLogger(PlantUmlFormatter.class);

	public String format(ApiClasses classes) throws Exception {
		StringBuilder tb = new StringBuilder();
		StringBuilder tbClasses = new StringBuilder();
		StringBuilder tbLinks = new StringBuilder();

		tb.append("@startuml\n");
		tb.append("scale 1.2\n");
		tb.append("set namespaceSeparator ::\n");

		formatClasses(classes, tbClasses, tbLinks);

		tb.append(tbClasses);
		tb.append(tbLinks);

		tb.append("@enduml\n");

		if (LOG.isDebugEnabled()) {
			LOG.debug("PlantUML: " + tb.toString());
		}

		return tb.toString();
	}

	private void formatClasses(ApiClasses classes, StringBuilder tbClasses,
			StringBuilder tbLinks) throws Exception {
		classes.getClassList().forEach((cls) -> {
			try {
				formatClass(classes, cls, tbClasses, tbLinks);
			} catch (Exception app) {

			}
		});
	}

	private void formatClass(ApiClasses classes, ApiClass cls,
			StringBuilder tbClasses, StringBuilder tbLinks) throws Exception {
		String strCurrName = returnClassName(cls);
		tbClasses.append("class ").append(strCurrName);

		if (cls.isSet("classType")) {
			switch (cls.getString("classType")) {
				case "Service Request":
					tbClasses.append(" << (S, #e3e0cc) >> #LightSeaGreen ");
					break;

				case "Service Response":
					tbClasses.append(" << (R, #e3e0cc) >> #LimeGreen ");
					break;

				case "Database":
					tbClasses.append(" << (D, #e3e0cc) >> #PowderBlue ");
					break;

				case "Third Party":
					tbClasses.append(" << (T, #e3e0cc) >> #Peru ");
					break;

				default:
					tbClasses.append(" << (M, #e3e0cc) >> #wheat ");
					break;
			}
		} else {
			tbClasses.append(" << (M, #e3e0cc) >> #wheat ");
		}

		tbClasses.append("{\n");

		for (ApiObject fld : cls.getList("fields")) {
			if (fld.isSet("min")) {
				if (fld.getLong("min") > 0) {
					tbClasses.append("#");
				}
			}

			tbClasses.append(fld.getString("name")).append(" : ").append(fld.
					getString(
							"type"));

			if ("ArrayList".equals(fld.getString("type"))) {
				ApiClass subClass = classes.getClass(fld.
						getString("subType"));
				String strSubName = returnClassName(subClass);

				tbClasses.append(" : ").append(subClass.getString("name"));

				if (fld.isSet("max")) {
					tbLinks.append(strCurrName).append(" --o \"").append(fld.
							getLong("max")).append("\" ").append(strSubName).
							append("\n");
				} else {
					tbLinks.append(strCurrName).append(" --o \"many\" ").
							append(strSubName).append("\n");
				}

			} else if ("Object".equals(fld.getString("type"))) {
				ApiClass subClass = classes.getClass(fld.
						getString("subType"));
				String strSubName = returnClassName(subClass);

				tbClasses.append(" : ").append(subClass.getString("name"));

				tbLinks.append(strCurrName).append(" -- ").append(strSubName).
						append(
								"\n");
			}

			tbClasses.append("\n");
		}

		tbClasses.append("}\n");
	}

	private String returnClassType(String classType) {
		String strRet = "model";

		if (classType != null) {
			switch (classType) {
				case "Service Request":
					strRet = "request";
					break;

				case "Service Response":
					strRet = "response";
					break;

				case "Database":
					strRet = "database";
					break;

				case "Third Party":
					strRet = "thirdparty";
					break;

				default:
					strRet = "model";
					break;
			}
		}

		return strRet;
	}

	private String returnClassName(ApiClass cls) {
		String strRet = returnClassType(cls.getString("classType"));

		strRet += "::" + cls.getString("name");

		return strRet;
	}
}
