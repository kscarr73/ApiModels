package com.progbits.api.utils;

import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author scarr_jp
 */
public class TestApiUtilsYamlLoader {

    private ApiUtilsYamlLoader _loader;

    @BeforeClass
    public void setup() {
        _loader = new ApiUtilsYamlLoader(this.getClass().getClassLoader(), null, null);
    }

    @Test
    public void testDefaultClass() throws ApiException, ApiClassNotFoundException {
        ApiClasses testClasses = new ApiClasses();

        _loader.retrieveClasses(null, "com.test.Default", testClasses);
    }

    @Test
    public void testDefaultPackage() throws ApiException, ApiClassNotFoundException {
        ApiClasses testClasses = new ApiClasses();

        _loader.retrievePackage(null, "com.test", testClasses);

        ApiObject objTest = testClasses.getInstance("com.test.Default");

        assert objTest.isSet("age");
    }

    @Test
    public void testDefaultPackage2() throws ApiException, ApiClassNotFoundException {
        ApiClasses testClasses = new ApiClasses();

        _loader.retrievePackage(null, "com.test", testClasses);

        ApiObject objTest = testClasses.getInstance("com.test.Default");

        assert objTest.isSet("age");
    }
}
