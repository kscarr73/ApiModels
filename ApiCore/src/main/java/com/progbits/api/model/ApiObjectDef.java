package com.progbits.api.model;

import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;

/**
 * Definition Classes for Objects
 *
 * @author scarr_jp
 */
public class ApiObjectDef {

    public static ApiClasses returnClassDef() {
        ApiClasses retClass = new ApiClasses();

        ApiClass cls = new ApiClass();
        cls.setName("apiClass");

        cls.setString("name", "apiClass");
        cls.setString("className", "com.progbits.api.ApiClass");
        cls.setString("desc", "Define a Business Model Class");

        cls.getListAdd("fields")
                .setName("apiField")
                .setString("name", "name")
                .setString("type", "String")
                .setString("desc", "The name of the Class");

        cls.getListAdd("fields")
                .setName("apiField")
                .setString("name", "className")
                .setString("type", "String")
                .setString("desc", "Package where the Class resides");

        cls.getListAdd("fields")
                .setName("apiField")
                .setString("name", "desc")
                .setString("type", "String")
                .setString("desc", "Description of the Class")
                .setLong("min", 1L)
                .setLong("max", 1L);

        cls.getListAdd("fields")
                .setName("apiField")
                .setString("name", "fields")
                .setString("type", "ArrayList")
                .setString("subType", "com.progbits.api.ApiField")
                .setString("desc", "List of Fields that makeup the Class")
                .setLong("min", 1L)
                .setLong("max", 0L);

        ApiClass fld = new ApiClass();

        fld.setString("name", "apiField");
        fld.setString("className", "com.progbits.api.ApiField");
        fld.setString("desc", "Define a Business Model Field");

        fld.getListAdd("fields")
                .setName("apiField")
                .setString("name", "name")
                .setString("type", "String")
                .setString("desc", "The name of the field");

        fld.getListAdd("fields")
                .setName("apiField")
                .setString("name", "type")
                .setString("type", "String")
                .setString("desc", "Type used for this field");

        fld.getListAdd("fields")
                .setName("apiField")
                .setString("name", "subType")
                .setString("type", "String")
                .setString("desc", "Object Name for a SubType");

        fld.getListAdd("fields")
                .setName("apiField")
                .setString("name", "desc")
                .setString("type", "String")
                .setString("desc", "Description of this Field");

        fld.getListAdd("fields")
                .setName("apiField")
                .setString("name", "sampleData")
                .setString("type", "String")
                .setString("desc", "Sample Information for this Field");

        fld.getListAdd("fields")
                .setName("apiField")
                .setString("name", "length")
                .setString("type", "Integer")
                .setString("desc", "The Length of this Field");

        fld.getListAdd("fields")
                .setName("apiField")
                .setString("name", "min")
                .setString("type", "Long")
                .setString("desc", "Minimum Number required for this Field.  1 in this field means required.");

        fld.getListAdd("fields")
                .setName("apiField")
                .setString("name", "max")
                .setString("type", "Long")
                .setString("desc", "Max Number required for this Field");

        fld.getListAdd("fields")
                .setName("apiField")
                .setString("name", "format")
                .setString("type", "String")
                .setString("desc", "A Type Specific Format to be used on output and input.");

        fld.getListAdd("fields")
                .setName("apiField")
                .setString("name", "status")
                .setString("type", "String")
                .setString("desc", "Status of this Object.  ENABLED, DISABLED");

        fld.getListAdd("fields")
                .setName("apiField")
                .setString("name", "default")
                .setString("type", "String")
                .setString("desc", "Default value when instantiating the Object");

        retClass.addClass(cls);
        retClass.addClass(fld);

        return retClass;
    }

    public static ApiClasses returnServiceDef() throws ApiException, ApiClassNotFoundException {
        ApiClasses classes = returnClassDef();

        ApiClasses retClass = new ApiClasses();

        ApiClass cls = new ApiClass(classes.getInstance(
                "com.progbits.api.ApiClass"));

        cls.setString("name", "apiService");
        cls.setString("className", "com.progbits.api.ApiService");
        cls.setString("desc", "Define a Service for the API System");

        cls.getListAdd("fields")
                .setString("name", "name")
                .setString("type", "String")
                .setString("desc", "The name of the Service");

        cls.getListAdd("fields")
                .setString("name", "packageName")
                .setString("type", "String")
                .setString("desc", "The base package location of the Service");

        cls.getListAdd("fields")
                .setString("name", "status")
                .setString("type", "String")
                .setString("desc", "Status of this API.  ENABLED, DISABLED");

        cls.getListAdd("fields")
                .setString("name", "url")
                .setString("type", "String")
                .setString("desc", "Base URL for this Service")
                .setLong("min", 1L)
                .setLong("max", 1L);

        cls.getListAdd("fields")
                .setName("apiField")
                .setString("name", "functions")
                .setString("type", "ArrayList")
                .setString("subType", "com.progbits.api.ApiFunction")
                .setString("desc", "List of Functions used with this Service")
                .setLong("min", 1L)
                .setLong("max", 1L);

        ApiClass funct = new ApiClass(classes.
                getInstance("com.progbits.api.ApiClass"));

        funct.setString("name", "apiFunction");
        funct.setString("className", "com.progbits.api.ApiFunction");
        funct.setString("desc", "Define a Function for a Service");

        funct.getListAdd("fields")
                .setName("apiField")
                .setString("name", "name")
                .setString("type", "String")
                .setString("desc", "Name of this Function");

        funct.getListAdd("fields")
                .setName("apiField")
                .setString("name", "desc")
                .setString("type", "String")
                .setString("desc", "Description of what this function is used for");

        retClass.addClass(cls);
        retClass.addClass(funct);

        return retClass;
    }
}
