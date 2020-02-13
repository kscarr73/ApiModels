package com.progbits.api.formaters;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarFr
 */
public class TransformDate {

    private final static Logger LOG = LoggerFactory.getLogger(
            TransformDate.class);

    public static OffsetDateTime transformDate(String value, String format) {
        DateTimeFormatter df;
        OffsetDateTime dt = null;

        if ("EPOCHDAYS".equals(format)) {
            Instant instant = Instant.EPOCH.plus(Integer.parseInt(value), ChronoUnit.DAYS);

            dt = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
        } else {
            if (format == null || format.isEmpty()) {
                df = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            } else {
                df = DateTimeFormatter.ofPattern(format);
            }

            try {
                if (value != null && !value.replace("0", "").trim().isEmpty()) {
                    TemporalAccessor ta = df.parse(value);

                    LocalDateTime ldt = LocalDateTime.from(ta);
                    dt = ldt.atOffset(ZoneOffset.UTC);
                }
            } catch (Exception ex) {
                LOG.info("transformDate: " + value + " Format: " + format, ex);
            }
        }

        return dt;
    }

    public static String formatDate(Object value, String format) {
        if (value instanceof OffsetDateTime) {
            OffsetDateTime dValue = (OffsetDateTime) value;

            if (format == null || format.isEmpty()) {
                return dValue.toString();
            } else {
                if (format.contains("|")) {
                    int iLoc = format.indexOf("|");

                    if (iLoc > -1) {
                        format = format.substring(0, iLoc);
                    }
                }

                DateTimeFormatter df = DateTimeFormatter.ofPattern(format);

                return df.format(dValue);
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
