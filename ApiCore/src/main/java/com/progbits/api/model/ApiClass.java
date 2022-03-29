package com.progbits.api.model;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author scarr
 */
public class ApiClass extends ApiObject {

    public ApiClasses _classes = null;

    private Map<String, String> defaultFields = null;
    private Map<String, String> defaultFieldType = null;

    public ApiClass() {

    }

    @Override
    public void setApiClasses(ApiClasses classes) {
        _classes = classes;
    }

    public ApiClass(ApiObject obj) {
        this._name = obj._name;
        this._class = obj._class;
        this._fields = obj._fields;

        if (!_fields.isEmpty()) {
            for (var field : this.getList("fields")) {
                if (field.isSet("default")) {
                    if (defaultFields == null) {
                        defaultFields = new HashMap<>();
                        defaultFieldType = new HashMap<>();
                    }

                    Object oValue = field.get("default");

                    if (oValue instanceof String) {
                        defaultFields.put(field.getString("name"), (String) oValue);
                    } else if (oValue instanceof Long) {
                        defaultFields.put(field.getString("name"), Long.toString((Long) oValue));
                    } else if (oValue instanceof Double) {
                        defaultFields.put(field.getString("name"), Double.toString((Double) oValue));
                    } else if (oValue instanceof Float) {
                        defaultFields.put(field.getString("name"), Float.toString((Float) oValue));
                    }

                    defaultFieldType.put(field.getString("name"), field.getString("type"));
                }
            }
        }
    }

    public ApiObject createInstance() {
        ApiObject obj = new ApiObject();

        obj.setName(this.getString("name"));
        obj.setApiClass(this);
        obj.setApiClasses(_classes);

        if (defaultFields != null) {
            for (var field : defaultFields.entrySet()) {

                ApiObject.TYPES typeEnum = ApiObject.TYPES.valueOf(defaultFieldType.get(field.getKey()));
                String defaultValue = field.getValue();

                switch (typeEnum) {
                    case String:
                        obj.setString(field.getKey(), defaultValue);
                        break;

                    case Integer:
                        obj.setInteger(field.getKey(), Integer.valueOf(defaultValue));
                        break;

                    case Boolean:
                        obj.setBoolean(field.getKey(), Boolean.valueOf(defaultValue));
                        break;

                    case Long:
                        obj.setLong(field.getKey(), Long.valueOf(defaultValue));
                        break;

                    case Double:
                        obj.setDouble(field.getKey(), Double.valueOf(defaultValue));
                        break;

                    case DateTime:
                        if ("now".equals(defaultValue) || "now()".equals(defaultValue)) {
                            obj.setDateTime(field.getKey(), OffsetDateTime.now());
                        }
                    default:
                        break;

                }
            }
        }

        return obj;
    }

}
