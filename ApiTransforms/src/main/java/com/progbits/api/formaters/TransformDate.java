package com.progbits.api.formaters;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarFr
 */
public class TransformDate {

	private final static Logger LOG = LoggerFactory.getLogger(
			  TransformDate.class);
	private static final DateTime EPOCH_START_INSTANT = new DateTime(0);

	public static DateTime transformDate(String value, String format) {
		DateTimeFormatter df;
		DateTime dt = null;

		if ("EPOCHDAYS".equals(format)) {
			dt = EPOCH_START_INSTANT.plusDays(Integer.parseInt(value));
		} else {
			if (format == null || format.isEmpty()) {
				df = DateTimeFormat.forPattern("MM/dd/yyyy");
			} else {
				df = DateTimeFormat.forPattern(format);
			}

			try {
				if (value != null && !value.replace("0", "").trim().isEmpty()) {
					dt = df.parseDateTime(value);
				}
			} catch (Exception ex) {
				LOG.info("transformDate: " + value + " Format: " + format, ex);
			}
		}

		return dt;
	}

	public static String formatDate(Object value, String format) {
		if (value instanceof DateTime) {
			DateTime dValue = (DateTime) value;

			if (format == null || format.isEmpty()) {
				return dValue.toString();
			} else {
				if (format.contains("|")) {
					int iLoc = format.indexOf("|");

					if (iLoc > -1) {
						format = format.substring(0, iLoc);
					}
				}

				return dValue.toString(format);
			}
		} else {
			String strRet = "";

			if (format.contains("|")) {
				int iLoc = format.indexOf("|");

				if (iLoc > -1) {
					strRet = format.substring(iLoc + 1);
				}
			}

			return strRet;
		}
	}
}
