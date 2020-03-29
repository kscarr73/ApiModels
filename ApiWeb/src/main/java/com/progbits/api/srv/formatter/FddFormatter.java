package com.progbits.api.srv.formatter;

import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import javolution.text.TextBuilder;

/**
 *
 * @author scarr
 */
public class FddFormatter {
	private static final String STANDARD_SPACE = "  ";
	
	public static String convertToFdd(String mainClass, ApiClasses classes) {
		TextBuilder text = new TextBuilder();
		
		text.append("form: FLATFILE\n");
		
		// If more than 1 class, setup structures with data
		if (classes.getClassList().size() > 1) {
			text.append("structures:\n");
		
			convertMainData(classes.getClass(mainClass), text);
		} else {
			text.append("name: '").append(classes.getClass(mainClass).getString("name")).append("' \n");
		}
		
		text.append("segments:\n");
		
		classes.getClassList().forEach((lclClass) -> {
			if (!lclClass.getString("className").equals(mainClass) || classes.getClassList().size() == 1) {
				convertSegment(lclClass, text);
			}
		});
		
		return text.toString();
	}
	
	public static void convertMainData(ApiObject mainClass, TextBuilder text) {
		text.append("- id: '").append(mainClass.getString("name")).append("'\n");
		text.append(STANDARD_SPACE).append("name: ").append(removeTags(mainClass.getString("desc"))).append("\n");
		text.append(STANDARD_SPACE).append("data:\n");
		
		mainClass.getList("fields").forEach(field -> {
			text.append(STANDARD_SPACE);
			text.append("- { idRef: '").append(field.getString("name")).append("' }\n");
		});
		
	}
	
	public static void convertSegment(ApiObject mainClass, TextBuilder text) {
		text.append("- id: '").append(mainClass.getString("name")).append("'\n");
		text.append(STANDARD_SPACE).append("name: ").append(removeTags(mainClass.getString("desc"))).append("\n");
		text.append(STANDARD_SPACE).append("values:\n");
		
		mainClass.getList("fields").forEach(field -> {
			text.append(STANDARD_SPACE);
			text.append("- { name: '").append(field.getString("name")).append("', ");
			
			switch (field.getString("type")) {
				case "Double":
					text.append(" type: ").append("Decimal").append(", ");
					break;
				default:
					text.append(" type: ").append(field.getString("type")).append(", ");
					break;
			}
			
			text.append(" length: ").append(field.getLong("length")).append(" }\n");
		});
	}
	public static String removeTags(String subject) {
		if (subject != null) {
			return subject.replaceAll("\\<.*?\\>", "");
		} else {
			return null;
		}
	}
}
