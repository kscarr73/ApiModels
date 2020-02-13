package com.progbits.api.formaters;

/**
 *
 * @author scarr
 */
public class TransformFactory {

	/**
	 * Sends Transform requests to the appropriate type. Defaults to String
	 *
	 * @param strType String, Date, Int, Integer, Decimal
	 * @param format Format applied to value
	 * @param strValue The Value to Transform
	 *
	 * @return Object containing the transformed value
	 */
	public static Object transformField(String strType, String format,
			  String strValue) throws Exception {
		if ("String".equals(strType)) {
			return TransformString.transformString(strValue, format);
		} else if ("Date".equals(strType)) {
			return TransformDate.transformDate(strValue, format);
		} else if ("Int".equals(strType) || "Integer".equals(strType)) {
			return TransformNumber.transformInteger(strValue, format);
		} else if ("Number".equals(strType) || "Decimal".equals(strType)) {
			return TransformDecimal.transformDecimal(strValue, format);
		} else {
			return TransformString.transformString(strValue, format);
		}
	}

	public static String formatField(String strType, String format,
			  Object oValue) throws Exception {
		if ("String".equals(strType)) {
			return TransformString.formatString(oValue, format);
		} else if ("Int".equals(strType) || "Integer".equals(strType)) {
			return TransformNumber.formatInteger(oValue, format);
		} else if ("Date".equals(strType)) {
			return TransformDate.formatDate(oValue, format);
		} else if ("Number".equals(strType) || "Decimal".equals(strType)) {
			return TransformDecimal.formatDecimal(oValue, format);
		} else {
			return TransformString.formatString(oValue, format);
		}
	}
}
