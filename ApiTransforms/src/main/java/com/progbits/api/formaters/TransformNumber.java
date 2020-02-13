package com.progbits.api.formaters;

/**
 *
 * @author scarr
 */
public class TransformNumber {

	public static Integer transformInteger(String value, String format) {
		Integer retInt;

		if (value != null && !value.trim().isEmpty()) {
			retInt = Integer.parseInt(value.trim());
		} else {
			retInt = null;
		}

		return retInt;
	}

	public static Long transformLong(String value, String format) {
		Long retLng;

		if (value != null && !value.trim().isEmpty()) {
			retLng = Long.parseLong(value.trim());
		} else {
			retLng = null;
		}

		return retLng;
	}

	public static String formatInteger(Object value, String format) {
		if (value == null && (format == null || format.isEmpty())) {
			return "";
		}

		if (format != null && format.startsWith("DF")) {
			String thisFormat = format.substring(2);

			try {
				return TransformDecimal.formatDecimal(value, thisFormat);
			} catch (Exception ex) {
				return "";
			}
		}

		Integer iValue = null;

		String thisFormat = format;

		if (value instanceof Integer) {
			iValue = (Integer) value;

			if (format == null || format.isEmpty()) {
				return String.valueOf(iValue);
			}
		} else if (value == null) {
			int iDefault = thisFormat.indexOf("default(");

			if (iDefault > -1) {
				int iStop = thisFormat.indexOf(")", iDefault);

				if (iValue == null) {
					String strDefault = thisFormat.
							  substring(iDefault + 8, iStop);

					iValue = Integer.valueOf(strDefault);
				}

				int iLength = thisFormat.length();

				if (iDefault == 0 && iStop == iLength - 1) {
					thisFormat = "";
				} else {
					thisFormat = thisFormat.substring(0, iDefault) + thisFormat.
							  substring(
										 iStop + 1);
				}
			}
		}

		if (thisFormat.isEmpty()) {
			return String.valueOf(iValue);
		} else {
			return String.format(thisFormat, iValue);
		}
	}

	public static String formatLong(Object value, String format) {
		if (value == null) {
			return "";
		}

		Long iValue = null;

		String thisFormat = format;

		if (value instanceof Long) {
			iValue = (Long) value;

			if (format == null || format.isEmpty()) {
				return String.valueOf(iValue);
			}
		} else if (value == null) {
			int iDefault = thisFormat.indexOf("default(");

			if (iDefault > -1) {
				int iStop = thisFormat.indexOf(")", iDefault);

				if (iValue == null) {
					String strDefault = thisFormat.
							  substring(iDefault + 8, iStop);

					iValue = Long.valueOf(strDefault);
				}

				int iLength = thisFormat.length();

				if (iDefault == 0 && iStop == iLength - 1) {
					thisFormat = "";
				} else {
					thisFormat = thisFormat.substring(0, iDefault) + thisFormat.
							  substring(
										 iStop + 1);
				}
			}
		}

		if (thisFormat.isEmpty()) {
			return String.valueOf(iValue);
		} else {
			return String.format(thisFormat, iValue);
		}
	}
}
