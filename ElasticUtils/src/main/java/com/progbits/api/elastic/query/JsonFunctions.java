package com.progbits.api.elastic.query;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 *
 * @author scarr
 */
public class JsonFunctions {

	private static DateTimeFormatter format = ISODateTimeFormat.dateTime();

	public static void processFieldValue(StringBuilder sb, Object value) {
		processFieldValue(sb, value, true);
	}

	public static void processFieldValue(StringBuilder sb, Object value,
			boolean bLower) {

		if (value instanceof String) {
			sb.append("\"");
			if (bLower) {
				sb.append(((String) value).replace("\"", "\\\"").toLowerCase());
			} else {
				sb.append(((String) value).replace("\"", "\\\""));
			}
			sb.append("\"");
		} else if (value instanceof DateTime) {
			sb.append("\"");
			sb.append(((DateTime) value).toString(format));
			sb.append("\"");
		} else {
			sb.append(value);
		}
	}

	public static void processFieldName(StringBuilder sb, String fieldName) {
		sb.append("\"");
		sb.append(fieldName);
		sb.append("\": ");
	}
}
