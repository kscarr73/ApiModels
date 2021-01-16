package com.progbits.api.model;

import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiDataValidationException;
import com.progbits.api.exception.ApiException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import javax.script.Bindings;

import java.time.OffsetDateTime;


/**
 * Runtime Representation of API Classes
 *
 * @author scarr
 */
public class ApiObject implements Bindings {

    protected String _name;
    protected ApiClass _class;
    protected ApiClasses m_models;

    public static final int TYPE_NULL = 0;
    public static final int TYPE_INTEGER = 1;
    public static final int TYPE_INTEGERARRAY = 8;
    public static final int TYPE_LONG = 2;
    public static final int TYPE_DOUBLE = 3;
    public static final int TYPE_DOUBLEARRAY = 10;
    public static final int TYPE_DECIMAL = 4;
    public static final int TYPE_STRING = 20;
    public static final int TYPE_STRINGARRAY = 25;
    public static final int TYPE_OBJECT = 30;
    public static final int TYPE_ARRAYLIST = 40;
    public static final int TYPE_DATETIME = 50;
    public static final int TYPE_BOOLEAN = 60;

    public enum TYPES {
        NULL, Integer, IntegerArray, Long, Double, Decimal,
        String, StringArray, Object, ArrayList, DateTime, Boolean
    }

    public static enum FIELDNAMES {
        name, fields
    }

    // We want to preserve insert order
    protected Map<String, Object> _fields = new LinkedHashMap<>();

    public String getName() {
        return _name;
    }

    public void setName(String _name) {
        this._name = _name;
    }

    public ApiClass getApiClass() {
        return _class;
    }

    public void setApiClass(ApiClass apiCls) {
        _class = apiCls;
    }

    public ApiClasses getApiClasses() {
        return m_models;
    }

    public void setApiClasses(ApiClasses classes) {
        m_models = classes;
    }

    public Map<String, Object> getFields() {
        return _fields;
    }

    public void setFields(Map<String, Object> _fields) {
        this._fields = _fields;
    }

    public Integer getInteger(String key) {
        return (Integer) getCoreObject(key);
    }

    public Integer getInteger(String key, Integer defValue) {
        Integer iRet = (Integer) getCoreObject(key);

        if (iRet == null) {
            return defValue;
        } else {
            return iRet;
        }
    }

    public Boolean getBoolean(String key) {
        return (Boolean) getCoreObject(key);
    }

    public Boolean getBoolean(String key, Boolean defValue) {
        Boolean bRet = (Boolean) getCoreObject(key);

        if (bRet == null) {
            return defValue;
        } else {
            return bRet;
        }
    }

    public void setBoolean(String key, Boolean value) {
        _fields.put(key, value);
    }

    public void setInteger(String key, Integer value) {
        _fields.put(key, value);
    }

    public OffsetDateTime getDateTime(String key) {
        Object obj = getCoreObject(key);

        if (obj instanceof OffsetDateTime) {
            return (OffsetDateTime) obj;
        } else if (obj instanceof String) {
            return OffsetDateTime.parse((String) obj);
        }

        return null;
    }

    public OffsetDateTime getDateTime(String key, OffsetDateTime defValue) {
        OffsetDateTime dtRet = (OffsetDateTime) getCoreObject(key);

        if (dtRet == null) {
            return defValue;
        } else {
            return dtRet;
        }
    }

    public void setDateTime(String key, OffsetDateTime value) {
        _fields.put(key, value);
    }

    public void setObject(String key, ApiObject value) {
        _fields.put(key, value);
    }

    public void setDouble(String key, Double value) {
        _fields.put(key, value);
    }

    public void setLong(String key, Long value) {
        _fields.put(key, value);
    }

    public void setString(String key, String value) {
        _fields.put(key, value);
    }

    public void setDecimal(String key, BigDecimal value) {
        _fields.put(key, value);
    }

    public BigDecimal getDecimal(String key) {
        Object obj = getCoreObject(key);

        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        } else {
            return null;
        }
    }

    public void createList(String key) {
        _fields.put(key, new ArrayList<ApiObject>());
    }

    public void setArrayList(String key, List<ApiObject> value) {
        _fields.put(key, value);
    }

    public ApiObject getObject(String key) {
        return (ApiObject) getCoreObject(key);
    }

    public Double getDouble(String key) {
        return (Double) getCoreObject(key);
    }

    public Long getLong(String key) {
        Object objValue = getCoreObject(key);

        if (objValue instanceof Long) {
            return (Long) objValue;
        } else if (objValue instanceof Integer) {
            return ((Integer) objValue).longValue();
        } else if (objValue instanceof String) {
            return Long.parseLong((String) objValue);
        } else if (objValue == null) {
            return null;
        } else {
            throw new ClassCastException(
                    getString(FIELDNAMES.name.name()) + ": " + key
                    + " Is Not Long Value: Type:" + objValue.getClass().getName());
        }

    }

    public Long getLong(String key, Long defValue) {
        Long retLng = getLong(key);

        if (retLng == null) {
            return defValue;
        } else {
            return retLng;
        }
    }

    public Double getDouble(String key, Double defValue) {
        Double retDbl = (Double) getCoreObject(key);

        if (retDbl == null) {
            return defValue;
        } else {
            return retDbl;
        }
    }

    public String getString(String key) {
        return (String) getCoreObject(key);
    }

    public String getString(String key, String defValue) {
        String retVal = (String) getCoreObject(key);

        if (retVal == null) {
            return defValue;
        } else {
            return retVal;
        }
    }

    public void createObject(String key) {
        _fields.put(key, newSubObject(key));
    }

    public ApiObject newSubObject(String key) {
        if (_class != null) {
            String subType = _class.
                    getString("fields[name=" + key + "].subType");

            return newObject(subType);
        } else {
            return new ApiObject();
        }
    }

    public ApiObject newObject(String className) {
        ApiObject obj = null;

        if (className != null) {
            try {
                obj = m_models.getInstance(className);
            } catch (ApiClassNotFoundException app) {

            }
        }
        if (obj != null) {
            return obj;
        } else {
            return new ApiObject();
        }
    }

    public ArrayList<ApiObject> getList(String key) {
        return (ArrayList<ApiObject>) getCoreObject(key);
    }

    public ApiObject getListAdd(String key) {
        ArrayList<ApiObject> lst = (ArrayList<ApiObject>) getCoreObject(key);

        lst.add(new ApiObject());

        return lst.get(lst.size() - 1);
    }

    public ApiObject getListSearch(String key, String match, String entry) {
        ArrayList<ApiObject> lst = (ArrayList<ApiObject>) getCoreObject(key);

        if (lst != null) {
            for (ApiObject obj : lst) {
                if (obj.getString(match).equals(entry)) {
                    return obj;
                }
            }
        }

        return null;
    }

    public ApiObject getListLast(String key) {
        ArrayList<ApiObject> lst = (ArrayList<ApiObject>) getCoreObject(key);

        if (lst != null && !lst.isEmpty()) {
            int iCnt = lst.size();

            return lst.get(iCnt - 1);
        } else {
            return null;
        }
    }

    /**
     * Returns the Underlying Object from the Map
     *
     * @param key field Name to return
     * @return Object stored for the field name.
     */
    public Object getCoreObject(String key) {
        Object resp = null;

        if (resp == null && key.contains(".")) {
            List<String> spKey = Arrays.asList(key.split("\\."));
            boolean bFound = true;
            ApiObject objSub = this;

            // Do not iterate to the LAST entry because that is the field
            int iCnt = 0;
            String strFieldName = null;

            for (String name : spKey) {
                if (iCnt == (spKey.size() - 1)) {
                    strFieldName = name;
                    break;
                }

                if (name.contains("[")) {
                    int iLoc = name.indexOf("[");
                    int iLoc2 = name.indexOf("]");

                    String arrKey = name.substring(0, iLoc);
                    String arrInt = name.substring(iLoc + 1, iLoc2);

                    int iType = objSub.getType(arrKey);

                    switch (iType) {
                        case TYPE_ARRAYLIST:
                            List<ApiObject> al = objSub.getList(arrKey);

                            if (al != null) {
                                char firstChar = arrInt.toLowerCase().charAt(0);

                                if ("0123456789".indexOf(firstChar) == -1) {
                                    // Is Not Digits, should have =
                                    String[] splKey = arrInt.split("=");

                                    for (ApiObject obj : al) {
                                        Object otst = obj.getFields().get(
                                                splKey[0]);

                                        if (otst instanceof String) {
                                            if (((String) otst).
                                                    equals(splKey[1])) {
                                                objSub = obj;
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    objSub = al.get(Integer.parseInt(arrInt));
                                }
                            }
                            break;

                        case TYPE_STRINGARRAY:
                        case TYPE_INTEGERARRAY:
                        case TYPE_DOUBLEARRAY:
                            List lstSubject = (List) objSub.getCoreObject(key);

                            resp = lstSubject.get(Integer.parseInt(arrInt));
                            objSub = null;
                            break;

                    }

                } else {
                    objSub = objSub.getObject(name);
                }

                // TODO:  Iterate over rest of object
                if (objSub == null) {
                    bFound = false;
                    break;
                }

                iCnt++;
            }

            if (objSub != null) {
                resp = objSub.getCoreObject(strFieldName);
            }
        } else if (key.contains("[")) {
            int iLoc = key.indexOf("[");
            int iLoc2 = key.indexOf("]");

            String arrKey = key.substring(0, iLoc);
            String arrInt = key.substring(iLoc + 1, iLoc2);

            List lstSubject = (List) getCoreObject(arrKey);

            resp = lstSubject.get(Integer.parseInt(arrInt));

        } else {
            resp = _fields.get(key);
        }

        return resp;
    }

    public static ApiClasses returnClassDef() {
        ApiClasses retClass = new ApiClasses();

        ApiClass cls = new ApiClass();
        cls.setName("apiClass");

        cls.setString("name", "apiClass");
        cls.setString("className", "com.icg.isg.api.ApiClass");
        cls.setString("desc", "Define a Business Model Class");

        cls.createList("fields");

        cls.getListAdd("fields");
        cls.getListLast("fields").setName("apiField");
        cls.getListLast("fields").setString("name", "name");
        cls.getListLast("fields").setString("type", "String");
        cls.getListLast("fields").setString("desc", "The name of the Class");

        cls.getListAdd("fields");
        cls.getListLast("fields").setName("apiField");
        cls.getListLast("fields").setString("name", "className");
        cls.getListLast("fields").setString("type", "String");
        cls.getListLast("fields").setString("desc",
                "Package where the Class resides");

        cls.getListAdd("fields");
        cls.getListLast("fields").setName("apiField");
        cls.getListLast("fields").setString("name", "desc");
        cls.getListLast("fields").setString("type", "String");
        cls.getListLast("fields").setString("desc", "Description of the Class");
        cls.getListLast("fields").setLong("min", 1L);
        cls.getListLast("fields").setLong("max", 1L);

        cls.getListAdd("fields");
        cls.getListLast("fields").setName("apiField");
        cls.getListLast("fields").setString("name", "fields");
        cls.getListLast("fields").setString("type", "ArrayList");
        cls.getListLast("fields").setString("subType",
                "com.icg.isg.api.ApiField");
        cls.getListLast("fields").setString("desc",
                "List of Fields that makeup the Class");
        cls.getListLast("fields").setLong("min", 1L);
        cls.getListLast("fields").setLong("max", 0L);

        ApiClass fld = new ApiClass();

        fld.setString("name", "apiField");
        fld.setString("className", "com.icg.isg.api.ApiField");
        fld.setString("desc", "Define a Business Model Field");

        fld.createList("fields");

        fld.getListAdd("fields");
        fld.getListLast("fields").setName("apiField");
        fld.getListLast("fields").setString("name", "name");
        fld.getListLast("fields").setString("type", "String");
        fld.getListLast("fields").setString("desc", "The name of the field");

        fld.getListAdd("fields");
        fld.getListLast("fields").setName("apiField");
        fld.getListLast("fields").setString("name", "type");
        fld.getListLast("fields").setString("type", "String");
        fld.getListLast("fields").setString("desc", "Type used for this field");

        fld.getListAdd("fields");
        fld.getListLast("fields").setName("apiField");
        fld.getListLast("fields").setString("name", "subType");
        fld.getListLast("fields").setString("type", "String");
        fld.getListLast("fields").setString("desc", "Object Name for a SubType");

        fld.getListAdd("fields");
        fld.getListLast("fields").setName("apiField");
        fld.getListLast("fields").setString("name", "desc");
        fld.getListLast("fields").setString("type", "String");
        fld.getListLast("fields").setString("desc", "Description of this Field");

        fld.getListAdd("fields");
        fld.getListLast("fields").setName("apiField");
        fld.getListLast("fields").setString("name", "sampleData");
        fld.getListLast("fields").setString("type", "String");
        fld.getListLast("fields").setString("desc",
                "Sample Information for this Field");

        fld.getListAdd("fields");
        fld.getListLast("fields").setName("apiField");
        fld.getListLast("fields").setString("name", "length");
        fld.getListLast("fields").setString("type", "Integer");
        fld.getListLast("fields").setString("desc", "The Length of this Field");

        fld.getListAdd("fields");
        fld.getListLast("fields").setName("apiField");
        fld.getListLast("fields").setString("name", "min");
        fld.getListLast("fields").setString("type", "Long");
        fld.getListLast("fields").setString("desc",
                "Minimum Number required for this Field.  1 in this field means required.");

        fld.getListAdd("fields");
        fld.getListLast("fields").setName("apiField");
        fld.getListLast("fields").setString("name", "max");
        fld.getListLast("fields").setString("type", "Long");
        fld.getListLast("fields").setString("desc",
                "Max Number required for this Field");

        fld.getListAdd("fields");
        fld.getListLast("fields").setName("apiField");
        fld.getListLast("fields").setString("name", "format");
        fld.getListLast("fields").setString("type", "String");
        fld.getListLast("fields").setString("desc",
                "A Type Specific Format to be used on output and input.");

        fld.getListAdd("fields");
        fld.getListLast("fields").setName("apiField");
        fld.getListLast("fields").setString("name", "status");
        fld.getListLast("fields").setString("type", "String");
        fld.getListLast("fields").setString("desc",
                "Status of this Object.  ENABLED, DISABLED");

        retClass.addClass(cls);
        retClass.addClass(fld);

        return retClass;
    }

    public static ApiClasses returnServiceDef() throws ApiException, ApiClassNotFoundException {
        ApiClasses classes = returnClassDef();

        ApiClasses retClass = new ApiClasses();

        ApiClass cls = new ApiClass(classes.getInstance(
                "com.icg.isg.api.ApiClass"));

        cls.setString("name", "apiService");
        cls.setString("className", "com.icg.isg.api.ApiService");
        cls.setString("desc", "Define a Service for the API System");

        cls.createList("fields");

        cls.getList("fields").add(classes.getClassByName("apiField"));
        cls.getListLast("fields").setString("name", "name");
        cls.getListLast("fields").setString("type", "String");
        cls.getListLast("fields").setString("desc", "The name of the Service");

        cls.getList("fields").add(classes.getClassByName("apiField"));
        cls.getListLast("fields").setString("name", "packageName");
        cls.getListLast("fields").setString("type", "String");
        cls.getListLast("fields").setString("desc",
                "The base package location of the Service");

        cls.getList("fields").add(classes.getClassByName("apiField"));
        cls.getListLast("fields").setString("name", "status");
        cls.getListLast("fields").setString("type", "String");
        cls.getListLast("fields").setString("desc",
                "Status of this API.  ENABLED, DISABLED");

        cls.getList("fields").add(classes.getClassByName("apiField"));
        cls.getListLast("fields").setString("name", "url");
        cls.getListLast("fields").setString("type", "String");
        cls.getListLast("fields").setString("desc", "Base URL for this Service");
        cls.getListLast("fields").setLong("min", 1L);
        cls.getListLast("fields").setLong("max", 1L);

        cls.getList("fields").add(classes.getClassByName("apiField"));
        cls.getListLast("fields").setName("apiField");
        cls.getListLast("fields").setString("name", "functions");
        cls.getListLast("fields").setString("type", "ArrayList");
        cls.getListLast("fields").
                setString("subType", "com.icg.isg.api.ApiFunction");
        cls.getListLast("fields").setString("desc",
                "List of Functions used with this Service");
        cls.getListLast("fields").setLong("min", 1L);
        cls.getListLast("fields").setLong("max", 1L);

        ApiClass funct = new ApiClass(classes.
                getInstance("com.icg.isg.api.ApiClass"));

        funct.setString("name", "apiFunction");
        funct.setString("className", "com.icg.isg.api.ApiFunction");
        funct.setString("desc", "Define a Function for a Service");

        funct.createList("fields");

        funct.getListAdd("fields");
        funct.getListLast("fields").setName("apiField");
        funct.getListLast("fields").setString("name", "name");
        funct.getListLast("fields").setString("type", "String");
        funct.getListLast("fields").setString("desc", "Name of this Function");

        funct.getListAdd("fields");
        funct.getListLast("fields").setName("apiField");
        funct.getListLast("fields").setString("name", "desc");
        funct.getListLast("fields").setString("type", "String");
        funct.getListLast("fields").setString("desc",
                "Description of what this function is used for");

        Map<String, ApiClass> retClasses = new HashMap<>();
        retClasses.put("com.icg.isg.api.ApiClass", cls);
        retClasses.put("com.icg.isg.api.ApiFunction", funct);

        retClass.addClass(cls);
        retClass.addClass(funct);

        return retClass;
    }

    public void createStringArray(String key) {
        _fields.put(key, new ArrayList<String>());
    }

    public List<String> getStringArray(String key) {
        return (List<String>) getCoreObject(key);
    }

    public void createIntegerArray(String key) {
        _fields.put(key, new ArrayList<Integer>());
    }

    public void createDoubleArray(String key) {
        _fields.put(key, new ArrayList<Double>());
    }

    public List<Integer> getIntegerArray(String key) {
        return (List<Integer>) getCoreObject(key);
    }

    public List<Double> getDoubleArray(String key) {
        return (List<Double>) getCoreObject(key);
    }

    /**
     * Tells whether an entry is
     *
     * @param key
     * @return
     */
    public boolean isNull(String key) {
        Object obj = getCoreObject(key);

        if (obj == null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Not Null, and has a non default value.
     *
     * @param key
     * @return
     */
    public boolean isSet(String key) {
        Object obj = getCoreObject(key);

        if (obj == null) {
            return false;
        } else if (obj instanceof String) {
            return !((String) obj).isEmpty();
        } else if (obj instanceof Integer) {
            return (((Integer) obj) != 0);
        } else if (obj instanceof Long) {
            return (((Long) obj) != 0);
        } else if (obj instanceof OffsetDateTime) {
            return (((OffsetDateTime) obj).getNano() != 0);
        } else if (obj instanceof Double) {
            return (((Double) obj) != 0);
        } else if (obj instanceof Boolean) {
            Boolean blnObj = (Boolean) obj;

            if (blnObj) {
                return true;
            } else {
                return false;
            }
        } else if (obj instanceof BigDecimal) {
            return (((BigDecimal) obj) != BigDecimal.ZERO);
        } else if (obj instanceof List) {
            return (((List) obj).size() > 0);
        } else if (obj instanceof ApiObject) {
            return true;
        } else {
            return false;
        }
    }

    public int getType(String key) {
        Object obj = getCoreObject(key);

        if (obj == null) {
            return TYPE_NULL;
        } else if (obj instanceof String) {
            return TYPE_STRING;
        } else if (obj instanceof Integer) {
            return TYPE_INTEGER;
        } else if (obj instanceof Long) {
            return TYPE_LONG;
        } else if (obj instanceof OffsetDateTime) {
            return TYPE_DATETIME;
        } else if (obj instanceof Double) {
            return TYPE_DOUBLE;
        } else if (obj instanceof BigDecimal) {
            return TYPE_DECIMAL;
        } else if (obj instanceof List) {
            List objTest = (List) obj;

            if (objTest.size() > 0) {
                Object objItemTest = objTest.get(0);

                if (objItemTest instanceof ApiObject) {
                    return TYPE_ARRAYLIST;
                } else if (objItemTest instanceof String) {
                    return TYPE_STRINGARRAY;
                } else if (objItemTest instanceof Integer) {
                    return TYPE_INTEGERARRAY;
                } else if (objItemTest instanceof Double) {
                    return TYPE_DOUBLEARRAY;
                } else {
                    return TYPE_NULL;
                }
            } else {
                return TYPE_ARRAYLIST;
            }
        } else if (obj instanceof ApiObject) {
            return TYPE_OBJECT;
        } else {
            return TYPE_NULL;
        }
    }

    public void validate() throws ApiDataValidationException {
        List<String> errors = new ArrayList<>();

        validateFields("", errors);

        if (!errors.isEmpty()) {
            throw new ApiDataValidationException("Validation Failure: " + errors,
                    null);
        }
    }

    protected boolean validateRequired(ApiObject field) {
        if (field.isSet("min")) {
            if (field.getLong("min") > 0L) {
                return true;
            }
        }

        return false;
    }

    protected void validateFields(String prefix, List<String> errors) {
        String strSetPrefix = "";

        if (prefix != null && !prefix.isEmpty()) {
            strSetPrefix += prefix + ".";
        }

        if (_class != null) {
            for (ApiObject field : _class.getList("fields")) {
                Object oFld = _fields.get(field.getString(FIELDNAMES.name.name()));

                switch (field.getString("type").toLowerCase()) {
                    case "string":
                        if (oFld == null) {
                            if (validateRequired(field)) {
                                errors.add(
                                        strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Required Field");
                            }
                        } else if (!(oFld instanceof String)) {
                            errors.add(
                                    strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Is Not a String");
                        } else if (validateRequired(field)) {
                            if (!isSet(field.getString(FIELDNAMES.name.name()))) {
                                errors.add(
                                        strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Required Field");
                            }
                        }

                        break;
                    case "integer":
                        if (oFld == null) {
                            if (validateRequired(field)) {
                                errors.add(
                                        strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Required Field");
                            }
                        } else if (!(oFld instanceof Integer)) {
                            errors.add(
                                    strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Is Not an Integer");
                        }

                        break;
                    case "long":
                        if (oFld == null) {
                            if (validateRequired(field)) {
                                errors.add(
                                        strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Required Field");
                            }
                        } else if (!(oFld instanceof Long)) {
                            errors.add(
                                    strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Is Not an Long");
                        }
                        break;
                    case "boolean":
                        if (oFld == null) {
                            if (validateRequired(field)) {
                                errors.add(
                                        strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Required Field");
                            }
                        } else if (!(oFld instanceof Boolean)) {
                            errors.add(
                                    strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Is Not an Boolean");
                        }
                        break;
                    case "decimal":
                        if (oFld == null) {
                            if (validateRequired(field)) {
                                errors.add(
                                        strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Required Field");
                            }
                        } else if (!(oFld instanceof BigDecimal)) {
                            errors.add(
                                    strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Is Not an Decimal");
                        }
                        break;
                    case "datetime":
                        if (oFld == null) {
                            if (validateRequired(field)) {
                                errors.add(
                                        strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Required Field");
                            }
                        } else if (!(oFld instanceof OffsetDateTime)) {
                            errors.add(
                                    strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Is Not an DateTime");
                        }
                        break;
                    case "object":
                        if (oFld == null) {
                            if (validateRequired(field)) {
                                errors.add(
                                        strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Required Field");
                            }
                        } else if (!(oFld instanceof ApiObject)) {
                            errors.add(
                                    strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Is Not an Object");
                        } else {
                            ((ApiObject) oFld).validateFields(
                                    strSetPrefix + field.getString(
                                            "name"), errors);
                        }
                        break;
                    case "arraylist":
                        if (oFld == null) {
                            if (validateRequired(field)) {
                                errors.add(
                                        strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Required Field");
                            }
                        } else if (!(oFld instanceof ArrayList)) {
                            errors.add(
                                    strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Is Not an ArrayList");
                        } else {
                            for (ApiObject obj : (ArrayList<ApiObject>) oFld) {
                                obj.validateFields(strSetPrefix + field.
                                        getString(FIELDNAMES.name.name()),
                                        errors);
                            }
                        }
                        break;
                    case "arraystring":
                        if (oFld == null) {
                            if (validateRequired(field)) {
                                errors.add(
                                        strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Required Field");
                            }
                        } else if (!(oFld instanceof ArrayList)) {
                            errors.add(
                                    strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Is Not an ArrayList");
                        }
                        break;
                    case "arrayinteger":
                        if (oFld == null) {
                            if (validateRequired(field)) {
                                errors.add(
                                        strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Required Field");
                            }
                        } else if (!(oFld instanceof ArrayList)) {
                            errors.add(
                                    strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Is Not an ArrayList");
                        }
                        break;
                    case "arraydouble":
                        if (oFld == null) {
                            if (validateRequired(field)) {
                                errors.add(
                                        strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Required Field");
                            }
                        } else if (!(oFld instanceof ArrayList)) {
                            errors.add(
                                    strSetPrefix + field.getString(FIELDNAMES.name.name()) + " Is Not an ArrayList");
                        }
                        break;
                }
            }
        } else {
            errors.add("No Class Definition");
        }
    }

    /**
     * Returns a deep clone of the current ApiObject.
     *
     * @return ApiObject clone of the current ApiObject
     *
     * @throws ApiException An API Exception
     */
    @SuppressWarnings("UnnecessaryBoxing")
    public ApiObject cloneApiObject() throws ApiException {
        final ApiObject retObj = this.getApiClass().createInstance();

        this.getFields().forEach((k, v) -> {
            try {
                int iFieldType = this.getType(k);

                switch (iFieldType) {
                    case TYPE_ARRAYLIST:
                        List<ApiObject> newList = new ArrayList<>();

                        ((List<ApiObject>) v).forEach((entry) -> {
                            try {
                                newList.add(entry.cloneApiObject());
                            } catch (ApiException aex) {

                            }
                        });
                        break;

                    case TYPE_OBJECT:
                        retObj.setObject(k, ((ApiObject) v).cloneApiObject());
                        break;

                    case TYPE_STRINGARRAY:
                        List<String> newStrArray = new ArrayList<>();

                        ((List<String>) v).forEach((entry) -> {
                            newStrArray.add(new String(entry.getBytes()));
                        });

                        retObj.getFields().put(k, newStrArray);

                        break;

                    case TYPE_INTEGERARRAY:
                        List<Integer> newIntArray = new ArrayList<>();

                        ((List<Integer>) v).forEach((entry) -> {
                            newIntArray.add(Integer.valueOf(entry.intValue()));
                        });

                        retObj.getFields().put(k, newIntArray);
                        break;
                        
                    case TYPE_DOUBLEARRAY:
                        List<Double> newDblArray = new ArrayList<>();

                        ((List<Integer>) v).forEach((entry) -> {
                            newDblArray.add(Double.valueOf(entry.doubleValue()));
                        });

                        retObj.getFields().put(k, newDblArray);
                        break;

                    case TYPE_BOOLEAN:
                        retObj.setBoolean(k, Boolean.valueOf((Boolean) v));
                        break;

                    case TYPE_STRING:
                        retObj.setString(k, new String((String) v));
                        break;

                    case TYPE_INTEGER:
                        retObj.setInteger(k, new Integer((Integer) v));
                        break;

                    case TYPE_DOUBLE:
                        retObj.setDouble(k, new Double((Double) v));
                        break;

                    case TYPE_DATETIME:
                        retObj.setDateTime(k, ((OffsetDateTime) v).plusNanos(0));
                        break;

                    case TYPE_DECIMAL:
                        retObj.setDecimal(k, new BigDecimal(((BigDecimal) v).
                                toString()));
                        break;

                    case TYPE_LONG:
                        retObj.setLong(k, new Long((Long) v));
                        break;

                    default:
                        break;
                }
            } catch (ApiException aex) {

            }
        });

        return retObj;
    }

    // <editor-fold desc="Bindings">
    @Override
    public Object put(String name, Object value) {
        if (_class != null) {
            String type = _class.getString(
                    "fields[name=" + name + "].type", "");

            switch (type) {
                case "String":
                    if (value instanceof String) {
                        return _fields.put(name, value);
                    }
                    if (value == null) {
                        return _fields.put(name, null);
                    } else {
                        return _fields.put(name, String.valueOf(value));
                    }

                case "Integer":
                    if (value instanceof Integer) {
                        return _fields.put(name, value);
                    } else if (value instanceof Long) {
                        return _fields.put(name, ((Long) value).intValue());
                    } else if (value instanceof Double) {
                        return _fields.put(name, ((Double) value).intValue());
                    } else if (value instanceof String) {
                        if (((String) value).trim().isEmpty()) {
                            return _fields.put(name, null);
                        } else {
                            return _fields.put(name, Integer.parseInt((String) value));
                        }
                    } else if (value instanceof BigDecimal) {
                        return _fields.put(name, ((BigDecimal) value).intValue());
                    } else {
                        return _fields.put(name, value);
                    }

                case "Long":
                    if (value instanceof Long) {
                        return _fields.put(name, value);
                    } else if (value instanceof Integer) {
                        return _fields.put(name, ((Integer) value).longValue());
                    } else if (value instanceof Double) {
                        return _fields.put(name, ((Double) value).longValue());
                    } else if (value instanceof String) {
                        if (((String) value).trim().isEmpty()) {
                            return _fields.put(name, null);
                        } else {
                            return _fields.put(name, Long.parseLong((String) value));
                        }
                    } else if (value instanceof BigDecimal) {
                        return _fields.put(name, ((BigDecimal) value).longValue());
                    } else {
                        return _fields.put(name, value);
                    }
                case "ArrayList":
                    return _fields.put(name, value);
                case "Object":
                    return _fields.put(name, value);
                case "DateTime":
                    if (value instanceof String) {
                        if (((String) value).isEmpty()) {
                            return _fields.put(name, null);
                        } else {
                            return _fields.put(name, OffsetDateTime.parse((String) value));
                        }

                    } else {
                        return _fields.put(name, value);
                    }
                case "Double":
                    if (value instanceof Double) {
                        return _fields.put(name, value);
                    } else if (value instanceof Integer) {
                        return _fields.put(name, ((Integer) value).doubleValue());
                    } else if (value instanceof Long) {
                        return _fields.put(name, ((Long) value).doubleValue());
                    } else if (value instanceof String) {
                        if (((String) value).trim().isEmpty()) {
                            return _fields.put(name, null);
                        } else {
                            return _fields.put(name, Double.parseDouble((String) value));
                        }
                    } else if (value instanceof BigDecimal) {
                        return _fields.put(name, ((BigDecimal) value).doubleValue());
                    } else {
                        return _fields.put(name, value);
                    }

                case "Decimal":
                    if (value instanceof BigDecimal) {
                        return _fields.put(name, value);
                    } else if (value instanceof Integer) {
                        return _fields.put(name, BigDecimal.valueOf((Integer) value).longValue());
                    } else if (value instanceof Double) {
                        return _fields.put(name, BigDecimal.valueOf((Double) value));
                    } else if (value instanceof String) {
                        if (((String) value).trim().isEmpty()) {
                            return _fields.put(name, null);
                        } else {
                            return _fields.put(name, new BigDecimal((String) value));
                        }
                    } else if (value instanceof Long) {
                        return _fields.put(name, BigDecimal.valueOf((Long) value));
                    } else {
                        return _fields.put(name, value);
                    }

                case "StringArray":
                    return _fields.put(name, value);

                case "IntegerArray":
                    return _fields.put(name, value);

                case "Boolean":
                    if (value instanceof Boolean) {
                        return _fields.put(name, value);
                    } else if (value instanceof String) {
                        if (((String) value).trim().isEmpty()) {
                            return _fields.put(name, null);
                        } else {
                            return _fields.put(name, Boolean.parseBoolean((String) value));
                        }
                    } else if (value instanceof Integer) {
                        if ((Integer) value != 0) {
                            return _fields.put(name, Boolean.TRUE);
                        } else {
                            return _fields.put(name, Boolean.FALSE);
                        }
                    } else {
                        return _fields.put(name, value);
                    }

                default:
                    return _fields.put(name, value);
            }
        } else {
            return _fields.put(name, value);
        }
    }

    @Override
    public void putAll(
            Map<? extends String, ? extends Object> map) {
        _fields.putAll(map);
    }

    @Override
    public boolean containsKey(Object o) {
        return _fields.containsKey(o);
    }

    @Override
    public Object get(Object o) {
        return _fields.get(o);
    }

    @Override
    public Object remove(Object o) {
        return _fields.remove(o);
    }

    @Override
    public int size() {
        return _fields.size();
    }

    @Override
    public boolean isEmpty() {
        return _fields.isEmpty();
    }

    @Override
    public boolean containsValue(Object o) {
        return _fields.containsValue(o);
    }

    @Override
    public void clear() {
        _fields.clear();
    }

    @Override
    public Set<String> keySet() {
        return _fields.keySet();
    }

    @Override
    public Collection<Object> values() {
        return _fields.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return _fields.entrySet();
    }

    @Override
    public void forEach(
            BiConsumer<? super String, ? super Object> bc) {
        _fields.forEach(bc);
    }

    @Override
    public Object compute(String k,
            BiFunction<? super String, ? super Object, ? extends Object> bf) {
        return _fields.compute(k, bf);
    }

    @Override
    public Object putIfAbsent(String k, Object v) {
        return _fields.putIfAbsent(k, v);
    }
    // </editor-fold>

}