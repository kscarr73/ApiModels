package com.progbits.api.srv.formatter;

import com.progbits.api.model.ApiClass;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;

/**
 *
 * @author scarr
 */
public class RamlFormatter {

	public static String convertToRaml(String mainClass, ApiClasses classes) {
		StringBuilder retSb = new StringBuilder();

		addLine(retSb, 0, "#%RAML 1.0 Library");

		addLine(retSb, 0, "types:");

		classes.getClassList().forEach((lclClass) -> {
			convertSegment(lclClass, classes, retSb);
		});

		return retSb.toString();
	}

	private static void convertSegment(ApiClass lclClass, ApiClasses classes, StringBuilder sb) {
		addLine(sb, 1, lclClass.getString("name") + ":");

		addLine(sb, 2, "type: object");
		addLine(sb, 2, "properties:");

		StringBuilder fieldList = new StringBuilder();

		for (int cnt = 0; cnt < lclClass.getList("fields").size(); cnt++) {
			ApiObject field = lclClass.getList("fields").get(cnt);

			boolean lastField = false;

			String fieldLine = field.getString("name");

			if (field.getLong("min") != null && field.getLong("min") > 0) {
				fieldLine += "?";
			}

			fieldLine += ": ";

			switch (field.getString("type")) {
				case "Double":
				case "Integer":
				case "Float":
					fieldLine += "number";
					break;
				case "ArrayList":
					ApiClass subClass = classes.getClass(field.getString("subType"));
					
					fieldLine += subClass.getString("name") + "[]";
					
					break;
				case "Object":
					ApiClass subObject = classes.getClass(field.getString("subType"));
					
					fieldLine += subObject.getString("name");
					
					break;
				default:
					fieldLine += field.getString("type").toLowerCase();
					break;
			}

//			if (!lastField) {
//				fieldLine += ",";
//			}
			addLine(fieldList, 3, fieldLine);
		}

		sb.append(fieldList);
	}

	private static void addLine(StringBuilder sb, Integer indentCount, String value) {
		for (int x = 0; x < indentCount; x++) {
			sb.append("   ");
		}

		sb.append(value);

		sb.append("\n");
	}
}
