package com.progbits.api.utils;

import com.progbits.api.utils.ApiMapping;
import com.progbits.api.model.ApiObject;
import com.progbits.api.testing.ReturnServices;
import com.progbits.api.utils.oth.ApiUtilsInterface;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class RunMap {

    private ApiUtilsInterface _api;
    private ApiMapping map = null;

    @BeforeClass
    public void setup() throws Exception {

        String script = Files.lines(
                Paths.get(getClass().getClassLoader()
                        .getResource("testmap.js")
                        .toURI()))
                .collect(Collectors.joining("\n"));

        _api = ReturnServices.returnApiUtils(
                "http://lvicisgnosd01.ingramcontent.com:9200");

        map = _api.getApiMapping("com.progbits.test.CustomField",
                "com.progbits.test.mapping.Test", script);
    }

    @Test(enabled = true)
    public void runMap() throws Exception {

        ApiObject obj = map.getInModels().getInstance(
                "com.progbits.test.CustomField");

        obj.setString("fieldName", "myTest");
        obj.setString("fieldValue", "value1");

        obj.setObject("anything", map.getOutModels().getInstance(
                "com.progbits.test.mapping.Test"));

        ApiObject objtar = map.map(obj, null);

        assert "myTest".equals(objtar.getString("message"));
    }

    @Test(enabled = true)
    public void runMap200() throws Exception {
        for (int x = 0; x <= 200; x++) {
            runMap();
        }
    }

    @Test(enabled = true)
    public void runMap2000() throws Exception {
        for (int x = 0; x <= 2000; x++) {
            runMap();
        }
    }

    @Test(enabled = true)
    public void runMap20000() throws Exception {
        for (int x = 0; x <= 20000; x++) {
            runMap();
        }
    }

    @Test(enabled = true)
    public void runMap20000_again() throws Exception {
        for (int x = 0; x <= 20000; x++) {
            runMap();
        }
    }

    @Test(enabled = false)
    public void returnMap() throws Exception {

        ApiMapping map = _api.getApiMapping("BisacMap");

        System.out.println();
    }
}
