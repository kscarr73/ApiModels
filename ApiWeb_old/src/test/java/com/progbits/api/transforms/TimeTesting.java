package com.progbits.api.transforms;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class TimeTesting {
    DateTimeFormatter dtFormat = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    DateTimeFormatter dtFormatNoMillis = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    
    @Test
    public void testDateFormat() {
        String date = "2015-01-02T03:31:03.310-05:00";
        
        OffsetDateTime dt = OffsetDateTime.parse(date, dtFormat);
        
        date = "2015-01-02T03:31:03.000-05:00";
        
        dt = OffsetDateTime.parse(date, dtFormat);
        
        date = "2015-01-02T03:31:03-05:00";
        dt = OffsetDateTime.parse(date, dtFormat);
        
    }
}
