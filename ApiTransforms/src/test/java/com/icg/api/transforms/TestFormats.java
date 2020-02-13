/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icg.api.transforms;

import com.progbits.api.formaters.TransformDate;
import com.progbits.api.formaters.TransformDecimal;
import com.progbits.api.formaters.TransformNumber;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class TestFormats {

    @Test
    public void testDecimal() throws Exception {
        BigDecimal bd = new BigDecimal("6146446");
        String format = "BN###.0#";

        System.out.println("Format: " + format + " Output: '" + TransformDecimal.formatDecimal(bd, format) + "'");

        format = "000000000";

        System.out.println("Format: " + format + " Output: '" + TransformDecimal.formatDecimal(bd, format) + "'");

        format = "N000000000";

        System.out.println("Format: " + format + " Output: '" + TransformDecimal.formatDecimal(bd, format) + "'");

        format = "AN000000000";

        System.out.println("Format: " + format + " Output: '" + TransformDecimal.formatDecimal(bd, format) + "'");
    }

    @Test
    public void testInteger() throws Exception {
        Integer i = 3;
        String format = null;
        String resp;

        resp = TransformNumber.formatInteger(i, format);

        assert "3".equals(resp);

        format = "%08d";

        resp = TransformNumber.formatInteger(i, format);

        assert "00000003".equals(resp);

        format = "%d";

        resp = TransformNumber.formatInteger(i, format);

        assert "3".equals(resp);

        format = null;

        resp = TransformNumber.formatInteger(i, format);

        assert "3".equals(resp);

        format = "default(2)";

        resp = TransformNumber.formatInteger(null, format);

        assert "2".equals(resp);

        format = "DFAN0";

        resp = TransformNumber.formatInteger(i, format);

        assert "3".equals(resp);

        format = "DFAN000000";

        resp = TransformNumber.formatInteger(i, format);

        assert "000003".equals(resp);

        format = "DFBAN#";

        resp = TransformNumber.formatInteger(0, format);

        assert "".equals(resp);

        resp = TransformNumber.formatInteger(i, format);

        assert "3".equals(resp);

    }

    @Test
    public void testDate() throws Exception {
        String strDate = "2010-01-01 05:12:22";
        String strFormat = "yyyy-MM-dd HH:mm:ss";

        OffsetDateTime dt = TransformDate.transformDate(strDate, strFormat);
    }
}
