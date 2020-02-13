package com.progbits.api.map.testing;

import com.progbits.api.ApiMapping;
import com.progbits.api.model.ApiObject;
import com.progbits.api.utils.oth.ApiUtilsInterface;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author scarr
 */
@Component(name="ApiMapTest", immediate = true)
public class ApiMapTest {
    ApiUtilsInterface api;
    ApiMapping map;
    
    @Reference
    public void setApi(ApiUtilsInterface api) {
        this.api = api;
    }
    
    @Activate
    public void setup() throws Exception {
        map = api.getApiMapping("testmap");
        
        runMap();
    }
    
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
}
