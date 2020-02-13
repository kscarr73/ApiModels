package com.icg.api.transforms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class RunObjects {

    @BeforeClass
    public void setup() {

    }

    @Test
    public void testBi() {
        DateTimeFormatter format = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        OffsetDateTime dt = OffsetDateTime.parse("2016-08-25T00:00:00Z", format);
        dt = OffsetDateTime.parse("2016-08-25T00:00:00-05:00", format);
        dt = OffsetDateTime.parse("2014-07-24T20:01:00.0000000Z", format);

        ByteArrayOutputStream out = new ByteArrayOutputStream(10000);

        OutputStreamWriter write = new OutputStreamWriter(out);

        try {
            write.write("This is my test\n");

            write.flush();
        } catch (IOException iex) {

        }

        System.out.println(out.toByteArray());

        String strTemp = new String(out.toByteArray());

    }
}
