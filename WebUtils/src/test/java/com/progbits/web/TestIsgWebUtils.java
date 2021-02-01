package com.progbits.web;

import com.progbits.web.WebUtils;
import com.progbits.api.model.ApiObject;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class TestIsgWebUtils {
    @Test
    public void testParseDataTable() throws Exception {
        HttpServletRequestTest thttp = new HttpServletRequestTest("This is a test");
        
        thttp.putParam("search[value]", "");
        thttp.putParam("columns[5][data]", "FirstName");
        thttp.putParam("columns[5][search][value]", "");
        thttp.putParam("columns[3][data]", "LastName");
        thttp.putParam("columns[3][search][value]", "");
        thttp.putParam("columns[0][data]", "ID");
        thttp.putParam("columns[0][search][value]", "");
        thttp.putParam("columns[1][data]", "This");
        thttp.putParam("columns[1][search][value]", "");
        thttp.putParam("columns[4][data]", "That");
        thttp.putParam("columns[4][search][value]", "");
        thttp.putParam("columns[2][data]", "Other");
        thttp.putParam("columns[2][search][value]", "");
        thttp.putParam("start", "0");
        thttp.putParam("length", "20");
        
        
        ApiObject objRet = WebUtils.parseDataTableParams(thttp);
    }
}
