package com.progbits.api.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class TestApiObjectUtils {
    private ApiObject getSubject() {
        ApiObject retObj = new ApiObject();
        
        retObj.setString("this", "That");
        retObj.setString("something", "else");
        retObj.setInteger("age", 35);
        retObj.setDecimal("testDecimal", new BigDecimal("0.34"));
        retObj.setBoolean("blnTest", true);
        
        retObj.getListAdd("subList")
                .setString("name", "Scott")
                .setString("something", "Scott")
                .setString("else", "Scott");
        retObj.getListAdd("subList")
                .setString("name", "Scott2")
                .setString("something", "Scott2")
                .setString("else", "Scott2");
        retObj.createObject("myObj")
                .setString("local", "Value")
                .setDouble("myDouble", 0.324);
        return retObj;
    }
    
    @Test
    public void testFullClone() {
        ApiObject objSubject = getSubject();
        
        ApiObject deepClone = ApiObjectUtils.cloneApiObject(objSubject, null);
        
        assert deepClone != null;
    }
    
    @Test
    public void testPartialClone() {
        ApiObject objSubject = getSubject();
        
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("this", "this");
        fieldMap.put("something", "something");
        fieldMap.put("testDecimal", "testDecimal");
        fieldMap.put("myObj", "somethingElse");
        fieldMap.put("myObj.local", "local");
        fieldMap.put("myObj.myDouble", "myDouble");
        fieldMap.put("subList.name", "name");
        fieldMap.put("subList", "subList");
        
        ApiObject deepClone = ApiObjectUtils.cloneApiObject(objSubject, "", true, fieldMap);
        
        assert deepClone != null;
        assert !deepClone.isSet("age");
    }
    
    @Test
    public void testBoolean() {
        ApiObject objSubject = getSubject();
        
        objSubject.setInteger("myNull", null);
        
        ApiObject deepClone = ApiObjectUtils.cloneApiObject(objSubject, null);
        
        assert deepClone != null;
    }
}
