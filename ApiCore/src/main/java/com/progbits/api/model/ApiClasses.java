package com.progbits.api.model;

import com.progbits.api.exception.ApiClassNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Storage for all Classes Registered to a particular Implementation.
 *
 * @author scarr
 */
public class ApiClasses {

    private final Map<String, ApiClass> classes = new HashMap<>();
    private final Map<String, String> mapName = new HashMap<>();

    public void addClass(ApiClass apiCls) {
        apiCls.setApiClasses(this);
        classes.put(apiCls.getString("className"), apiCls);
        mapName.put(apiCls.getString("name"), apiCls.getString("className"));
    }

    public ApiClass getClass(String className) throws ApiClassNotFoundException {
        if (classes.containsKey(className)) {
            return classes.get(className);
        } else {
            throw new ApiClassNotFoundException(
                    "API Class: [" + className + "] not in Map", className, null);
        }
    }

    public Map<String, ApiClass> getClasses() {
        return classes;
    }

    public Map<String, String> getMaps() {
        return mapName;
    }

    public List<ApiClass> getClassList() {
        return new ArrayList<>(classes.values());
    }

    public ApiClass getClassByName(String name) throws ApiClassNotFoundException {
        String clsName = mapName.get(name);
        if (clsName != null) {
            ApiClass apiClass = classes.get(clsName);

            apiClass.setApiClass(apiClass);

            return apiClass;
        } else {
            throw new ApiClassNotFoundException(
                    "Element Name: [" + name + "] does not exist in map",
                    name, null);
        }
    }

    public ApiObject getInstance(String className) throws ApiClassNotFoundException {
        ApiClass apiCls = classes.get(className);

        if (apiCls == null) {
            throw new ApiClassNotFoundException(
                    "API Class: [" + className + "] not in Map", className, null);
        }

        return apiCls.createInstance();
    }

    public ApiObject getInstanceByName(String name) throws ApiClassNotFoundException {
        String className = mapName.get(name);

        if (className != null) {
            return getInstance(className);
        } else {
            throw new ApiClassNotFoundException("Element Name: [" + name + "] not in Map", name,
                    null);
        }
    }
}
