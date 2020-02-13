package com.progbits.api.formaters;

import com.progbits.api.exception.ApiException;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 *
 * @author scarr
 */
public class TransformDecimal {

	public static Double transformDouble(String value, String format) throws ApiException {
		Double retDec;
		String strTest = value;

		try {
			if (format != null) {
				int iLoc = format.indexOf("V");

				if (iLoc > -1) {
					int decLength = format.length() - iLoc;

					int iTest = strTest.length() - decLength;

					strTest = strTest.substring(0, iTest + 1) + "." + strTest.
							  substring(
										 iTest + 1);
				}
			}

			if (strTest == null || strTest.trim().isEmpty()) {
				retDec = 0D;
			} else {
				retDec = Double.parseDouble(strTest.trim());
			}
		} catch (NumberFormatException nfe) {
			throw new ApiException("Format Exception",
					  nfe);
		}

		return retDec;
	}

	public static BigDecimal transformDecimal(String value, String format) throws ApiException {
		BigDecimal retDec;
		String strTest = value.trim();

		try {
			if (format != null) {
				int iLoc = format.indexOf("V");

				if (iLoc > -1) {
					int decLength = format.length() - iLoc;

					int iTest = strTest.length() - decLength;

					strTest = strTest.substring(0, iTest + 1) + "." + strTest.
							  substring(
										 iTest + 1);
				}
			}

			if (strTest == null || strTest.trim().isEmpty()) {
				retDec = new BigDecimal(0);
			} else {
				retDec = new BigDecimal(strTest.trim());
			}
		} catch (Exception ex) {
			throw new ApiException("Parse Exception",
					  ex);
		}

		return retDec;
	}

	public static String formatDecimal(Object value, String format) throws ApiException {
		String strDefault = "";

		try {
			if (format != null) {
				int iDLoc = format.indexOf("|");

				if (iDLoc > -1) {
					strDefault = format.substring(iDLoc + 1);
					format = format.substring(0, iDLoc);
				}
			}

			// Convert a null value to long for returning the padded value witht the spaces if the format configured
			value = (value == null && format != null) ? "0" : value;

			Double d = null;

			if (value instanceof BigDecimal) {
				d = ((BigDecimal) value).doubleValue();
			} else if (value instanceof Double) {
				d = (Double) value;
			} else if (value instanceof Integer || value instanceof Long || value instanceof String) {
				d = Double.parseDouble(value.toString());
			} else {
				return strDefault;
			}

			boolean bNoSign = false;
			boolean bNoDecimal = false;
			boolean bAbsolute = false;
			boolean bBlank = false;

			if (format != null) {
				int iLoc = format.indexOf("N");

				if (iLoc > -1) {
					bNoSign = true;

					format = format.replace("N", "");
				}

				iLoc = format.indexOf("V");

				if (iLoc > -1) {
					bNoDecimal = true;

					format = format.replace("V", ".");
				}

				iLoc = format.indexOf("B");

				if (iLoc > -1) {
					bBlank = true;

					format = format.replace("B", "");
				}

				iLoc = format.indexOf("A");

				if (iLoc > -1) {
					bAbsolute = true;

					format = format.replace("A", "");
				}
			} else {
				format = "0";
			}

			DecimalFormat df = new DecimalFormat(format);

			if (bAbsolute) {
				d = Math.abs(d);
			}

			String sRet = df.format(d);

			if (bNoDecimal) {
				sRet = sRet.replace(".", "");
			}

			if (d == 0 && bBlank) {
				return "";
			} else {
				if (d >= 0D) {
					if (bNoSign) {
						return sRet;
					} else {
						return " " + sRet;
					}
				} else {
					return sRet;
				}
			}
		} catch (Exception ex) {
			throw new ApiException("Format Exception",
					  ex);
		}

	}
}
