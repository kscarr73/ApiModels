package com.progbits.api;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class VerifyDates {
    @Test
    public void testDates() {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd[[ ]'T'HH:mm[:ss][.SSS]][X]]").withResolverStyle(ResolverStyle.SMART);
        
        TemporalAccessor dt;
        
        dt = format.parse("2018-08-11T10:30:15.513");
        
        dt = format.parse("2018-08-11T10:30:15");
        
        dt = format.parse("2018-08-11T10:30");
        
        dt = format.parse("2018-08-11");
        
        dt = format.parse("2018-08-11T10:30:15.513+06");
        
        dt = format.parse("2018-08-11T10:30:15.513+06:00");
        
        dt = format.parse("2018-08-11T10:30:15.513Z");
        
        dt = format.parse("2018-08-11T10:30:15.513+03:00");
        
    }
}
