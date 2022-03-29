package com.progbits.api.model;

import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class TestApiObject {

    @Test
    public void testApiObjectCreation() {
        ApiObject obj = new ApiObject();

        assert obj != null : "ApiObject was not created";
    }

    @Test
    public void testPullApiClasses() {
        ApiClasses classes = ApiObjectDef.returnClassDef();

        assert classes.getClasses().size() == 2 : "Expected 2 classes, but only found : " + classes.getClasses().size();
    }

    @Test
    public void testPullApiClassesMultiRun() {
        for (int x = 0; x < 100000; x++) {
            ApiObjectDef.returnClassDef();
        }
    }

    @Test
    public void testRetrieveData() {
        ApiObject obj = new ApiObject();

        obj.setString("mytest", "value");
        obj.setString("mytest1", "value1");
        obj.setString("mytest2", "value2");
        obj.setString("mytest3", "value3");
        obj.setString("mytest4", "value4");

        obj.createList("another");
        obj.getListAdd("another")
                .setString("field1", "value1")
                .setString("field2", "value2");

        for (int x = 0; x < 10000000; x++) {
            assert obj.getString("mytest3").equals("value3");
        }
    }
}
