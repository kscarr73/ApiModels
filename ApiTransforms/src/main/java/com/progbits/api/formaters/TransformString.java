package com.progbits.api.formaters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author scarr
 */
public class TransformString {

    /**
     * If there is not a format, returns the string
     *
     * If there is a format, currently will apply the regular expression and
     * return the first GROUP found.
     *
     * Future is to parse format as a function. REGEX(name\(.*\)test)
     *
     * Would run the regular expression name(.*)test
     *
     * @param value
     * @param format
     * @return
     */
    public static String transformString(String value, String format) {
        if (format == null || format.isEmpty()) {
            return value.trim();
        } else {
            Pattern p = Pattern.compile(format);
            Matcher m = p.matcher(value);

            if (m.matches()) {
                return m.group(1);
            } else {
                return "";
            }
        }
    }

    public static String formatString(Object value, String format) {
        if (value == null) {
            value = "";
        }

        if (value instanceof String) {
            String strValue = (String) value;

            if (format == null || format.isEmpty()) {
                return strValue;
            } else {
                String thisFormat = format;

                while (thisFormat.contains("s/")) {
                    int iFirst = thisFormat.indexOf("s/");
                    int iMiddle = thisFormat.indexOf("/", iFirst + 2);
                    int iLast = thisFormat.indexOf("/", iMiddle + 1);

                    String strFind = thisFormat.substring(iFirst + 2, iMiddle);
                    String strReplace = thisFormat.substring(iMiddle + 1, iLast);

                    strValue = strValue.replace(strFind, strReplace);

                    String strFirst = iFirst > 0 ? thisFormat.substring(0,
                            iFirst) : "";
                    String strLast = iLast + 1 < thisFormat.length() ? thisFormat.
                            substring(iLast + 1) : "";

                    thisFormat = strFirst + strLast;
                }

                int iDefault = thisFormat.indexOf("default(");

                if (iDefault > -1) {
                    int iStop = thisFormat.indexOf(")", iDefault);

                    if ((strValue != null && strValue.trim().isEmpty()) || strValue == null) {
                        String strDefault = thisFormat.substring(iDefault + 8,
                                iStop);

                        strValue = strDefault;
                    }

                    int iLength = thisFormat.length();

                    if (iDefault == 0 && iStop == iLength - 1) {
                        thisFormat = "";
                    } else {
                        thisFormat = thisFormat.substring(0, iDefault) + thisFormat.
                                substring(iStop + 1);
                    }
                }

                if (thisFormat.isEmpty()) {
                    return strValue;
                } else {
                    return String.format(thisFormat, strValue);
                }
            }
        } else {
            return "";
        }
    }

    /**
     * Repeat a Character n number of times
     *
     * @param padChar Character to repeat
     * @param iLength Number of times to repeat
     * @return String with the Character repeated n number of times
     */
    public static String repeat(String padChar, int iLength) {
        StringBuilder sb = new StringBuilder(iLength);

        for (int x = 0; x < iLength; x++) {
            sb.append(padChar);
        }

        return sb.toString();
    }

    /**
     * If subject not iLength, then padded on the right
     *
     * @param subject
     * @param padChar
     * @param iLength
     * @return
     */
    public static String forceRightSize(String subject, String padChar, int iLength) {
        if (subject == null) {
            return repeat(padChar, iLength);
        } else if (subject.length() == iLength) {
            return subject;
        } else if (subject.length() > iLength) {
            return subject.substring(0, iLength);
        } else {
            int iDiff = iLength - subject.length();
            return subject + repeat(padChar, iDiff);
        }
    }

    /**
     * If subject not iLength, then padded on the right
     *
     * @param subject
     * @param padChar
     * @param iLength
     */
    public static void forceRightSize(StringBuilder subject, String padChar, int iLength) {
        if (subject == null) {
            subject = new StringBuilder();
            subject.append(repeat(padChar, iLength));
        } else if (subject.length() > iLength) {
            subject.setLength(iLength);
        } else {
            int iDiff = iLength - subject.length();
            subject.append(repeat(padChar, iDiff));
        }
    }

    /**
     * If subject not iLength, then padded on the left
     *
     * @param subject
     * @param padChar
     * @param iLength
     * @return
     */
    public static String forceLeftSize(String subject, String padChar, int iLength) {
        if (subject == null) {
            return repeat(padChar, iLength);
        } else if (subject.length() == iLength) {
            return subject;
        } else if (subject.length() > iLength) {
            return subject.substring(0, iLength);
        } else {
            int iDiff = iLength - subject.length();
            return repeat(padChar, iDiff) + subject;
        }
    }
}
