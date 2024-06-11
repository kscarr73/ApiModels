package com.progbits.api.model;

import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiDataValidationException;
import com.progbits.api.exception.ApiException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import javax.script.Bindings;

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

    public ApiObject setName(String _name) {
        this._name = _name;

        return this;
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

    public ApiObject setFields(Map<String, Object> _fields) {
        this._fields = _fields;

        return this;
    }

    public Integer getInteger(String key) {
        Object objValue = getCoreObject(key);

        if (objValue instanceof Integer) {
            return (Integer) objValue;
        } else if (objValue instanceof Long) {
            return ((Long) objValue).intValue();
        } else if (objValue instanceof String) {
            return Integer.valueOf((String) objValue);
        } else {
            return null;
        }
    }

    public Integer getInteger(String key, Integer defValue) {
        Integer iRet = getInteger(key);

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
        Boolean bRet = getBoolean(key);

        if (bRet == null) {
            return defValue;
        } else {
            return bRet;
        }
    }

    public ApiObject setBoolean(String key, Boolean value) {
        _fields.put(key, value);

        return this;
    }

    public ApiObject setInteger(String key, Integer value) {
        _fields.put(key, value);
        return this;
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

    public ApiObject setDateTime(String key, OffsetDateTime value) {
        _fields.put(key, value);

        return this;
    }

    public ApiObject setObject(String key, ApiObject value) {
        _fields.put(key, value);

        return this;
    }

    public ApiObject setDouble(String key, Double value) {
        _fields.put(key, value);

        return this;
    }

    public ApiObject setLong(String key, Long value) {
        _fields.put(key, value);

        return this;
    }

    public ApiObject setString(String key, String value) {
        _fields.put(key, value);

        return this;
    }

    public ApiObject setDecimal(String key, BigDecimal value) {
        _fields.put(key, value);

        return this;
    }

    public BigDecimal getDecimal(String key) {
        Object obj = getCoreObject(key);

        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        } else {
            return null;
        }
    }

    /**
     * Adds a new List to the Object with a named key.
     *
     * @param key The name of the List.
     * @return An array of ApiObjects that was created.
     */
    public List<ApiObject> createList(String key) {
        _fields.put(key, new ArrayList<ApiObject>());

        return getList(key);
    }

    public ApiObject setArrayList(String key, List<ApiObject> value) {
        _fields.put(key, value);

        return this;
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

    /**
     * Create a new ApiObject as an Element.
     *
     * @param key The key to create
     *
     * @return ApiObject the created ApiObject
     */
    public ApiObject createObject(String key) {
        _fields.put(key, newSubObject(key));
        
        return this.getObject(key);
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

        if (className != null && m_models != null) {
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

    /**
     * Adds a new object in a List.
     *
     * If the list doesn't already exist, it is created.
     *
     * @param key The List to Add the Object to
     *
     * @return The newly created Object from the List
     */
    public ApiObject getListAdd(String key) {
        List<ApiObject> lst = (List<ApiObject>) getCoreObject(key);

        if (lst == null) {
            lst = this.createList(key);
        }

        lst.add(newSubObject(key));

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
     * Copies fields from a Source Object into this one
     *
     * @param fldList Fields to copy from Source Object
     *
     * @param src The Source object to copy from
     */
    public void copyInto(String[] fldList, ApiObject src) {
        for (String fldName : fldList) {
            if (src.containsKey(fldName)) {
                this.put(fldName, src.get(fldName));
            }
        }
    }

    /**
     * Given a list of fields, ensure only these fields remain
     *
     * @param fldList The list of fields to ensure remain
     */
    public void removeAllExcept(String[] fldList) {
        List<String> lstFields = Arrays.asList(fldList);
        List<String> removeFields = new ArrayList<>();

        for (String fldName : this.keySet()) {
            if (!lstFields.contains(fldName)) {
                removeFields.add(fldName);
            }
        }

        for (String rFldName : removeFields) {
            this.remove(rFldName);
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

        if (_fields.containsKey(key)) {
            resp = _fields.get(key);
        } else {
            if (key.contains(".")) {
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
                                            } else if (otst instanceof Long) {
                                                if (((Long) otst).
                                                        equals(Long.parseLong(splKey[1]))) {
                                                    objSub = obj;
                                                    break;
                                                }
                                            } else if (otst instanceof Integer) {
                                                if (((Integer) otst).
                                                        equals(Integer.parseInt(splKey[1]))) {
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

                List<ApiObject> lstSubject = (List) getCoreObject(arrKey);
                ApiObject objSub = null;

                if (lstSubject == null) {
                    objSub = null;
                } else {
                    char firstChar = arrInt.toLowerCase().charAt(0);

                    if ("0123456789".indexOf(firstChar) == -1) {
                        // Is Not Digits, should have =
                        String[] splKey = arrInt.split("=");

                        for (ApiObject obj : lstSubject) {
                            Object otst = obj.getFields().get(
                                    splKey[0]);

                            if (otst instanceof String) {
                                if (((String) otst).
                                        equals(splKey[1])) {
                                    objSub = obj;
                                    break;
                                }
                            } else if (otst instanceof Long) {
                                if (((Long) otst).
                                        equals(Long.parseLong(splKey[1]))) {
                                    objSub = obj;
                                    break;
                                }
                            } else if (otst instanceof Integer) {
                                if (((Integer) otst).
                                        equals(Integer.parseInt(splKey[1]))) {
                                    objSub = obj;
                                    break;
                                }
                            }
                        }
                    } else {
                        objSub = lstSubject.get(Integer.parseInt(arrInt));
                    }
                }

                resp = objSub;
            } else {
                resp = _fields.get(key);
            }
        }

        return resp;
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
            return (!((List) obj).isEmpty());
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
        } else if (obj instanceof Boolean) {
            return TYPE_BOOLEAN;
        } else if (obj instanceof OffsetDateTime) {
            return TYPE_DATETIME;
        } else if (obj instanceof Double) {
            return TYPE_DOUBLE;
        } else if (obj instanceof BigDecimal) {
            return TYPE_DECIMAL;
        } else if (obj instanceof List) {
            List objTest = (List) obj;

            if (!objTest.isEmpty()) {
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
    public ApiObject cloneApiObject() throws ApiException {
       return ApiObjectUtils.cloneApiObject(this, null);
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
