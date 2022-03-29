package com.progbits.api.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.progbits.api.ObjectParser;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClass;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author scarr
 */
@Component(name = "JsonObjectParser", immediate = true, property
        = {
            "type=JSON", "name=JsonObjectParser"
        })
public class JsonObjectParser implements ObjectParser {

    private ApiObject _obj = null;

    private String _mainClass;
    private ApiClasses _classes;
    private JsonParser _parse = null;
    private JsonFactory _factory = new JsonFactory();
    private Map<String, String> _props;
    private List<String> parseErrors;
    private Throwable throwException;

    private Map<String, DateTimeFormatter> _dtFormats = new HashMap<>();

    public JsonObjectParser() {
    }

    public JsonObjectParser(boolean genericProcessor) {
        if (genericProcessor) {
            internalInit(null, null, null, null);
        }
    }

    private void internalInit(ApiClasses classes, String mainClass,
            Map<String, String> properties, Reader in) {
        if (in != null) {
            try {
                _parse = _factory.createParser(in);
            } catch (IOException io) {

            }
        }
        _props = properties;
        _classes = classes;
        _mainClass = mainClass;
        this.parseErrors = new ArrayList<>();
    }

    @Override
    public ObjectParser getParser() {
        return new JsonObjectParser();
    }

    @Override
    public void initStream(ApiClasses classes, String mainClass,
            Map<String, String> properties, InputStream in) throws ApiException {
        init(classes, mainClass, properties, new BufferedReader(new InputStreamReader(in)));
    }

    @Override
    public void init(ApiClasses classes, String mainClass,
            Map<String, String> properties, Reader in) throws ApiException {
        internalInit(classes, mainClass, properties, in);
    }

    @Override
    public boolean next() throws ApiException, ApiClassNotFoundException {
        this.parseErrors.clear();
        this.throwException = null;

        if (_classes != null) {
            _obj = _classes.getInstance(_mainClass);
        } else {
            _obj = new ApiObject();
        }
        try {
            parseJsontoObject(_classes, _mainClass, _parse, _obj, true);

        } catch (Exception ex) {
            if (!this.parseErrors.contains(ex.getMessage())) {
                this.parseErrors.add(ex.getMessage());
            }
            this.throwException = ex;
        }

        return true;
    }

    @Override
    public ApiObject getObject() {
        return _obj;
    }

    public void parseJsontoObject(ApiClasses apiClasses, String curClass,
            JsonParser parse, ApiObject obj, boolean bFirst) throws ApiException, ApiClassNotFoundException {
        boolean iFirstObj = bFirst;

        ApiClass apiClass = null;

        if (apiClasses != null) {
            if (curClass != null) {
                if (curClass.contains(".")) {
                    apiClass = apiClasses.getClass(curClass);
                } else {
                    apiClass = apiClasses.getClassByName(curClass);
                }
            } else {
                throw new ApiException(
                        "Field Name is Null. Main Class: " + _mainClass, null);
            }
        }

        String key = null;
        ApiObject nObj = null;
        ApiObject curField = null;
        boolean inArray = false;
        int arrayType = ApiObject.TYPE_ARRAYLIST;

        try {
            OUTER:
            while (parse.nextToken() != JsonToken.END_OBJECT) {
                JsonToken event;
                if (parse.currentToken() == null) {
                    break;
                } else {
                    event = parse.currentToken();
                }

                switch (event) {
                    case START_OBJECT:
                        if (iFirstObj) {
                            iFirstObj = false;
                        } else {
                            if (apiClasses != null) {
                                if (curField != null) {
                                    if (curField.getString("subType") != null) {
                                        nObj = apiClasses.getInstance(curField.
                                                getString("subType"));
                                    } else {
                                        try {
                                            nObj = apiClasses.getInstanceByName(key);
                                        } catch (Exception ex) {
                                            nObj = new ApiObject();
                                            nObj.setName(key);
                                        }
                                    }
                                } else {
                                    try {
                                        nObj = apiClasses.getInstanceByName(key);
                                    } catch (Exception ex) {
                                        nObj = new ApiObject();
                                        nObj.setName(key);
                                    }
                                }
                            } else {
                                nObj = new ApiObject();
                                nObj.setName(key);
                            }

                            parseJsontoObject(apiClasses, nObj.getName(), parse, nObj,
                                    false);

                            if (inArray) {
                                if (obj.getList(key) == null) {
                                    obj.createList(key);
                                }

                                obj.getList(key).add(nObj);
                            } else {
                                obj.setObject(key, nObj);
                            }
                        }
                        break;
                    case FIELD_NAME:
                        key = parse.getText();
                        if (apiClass != null) {
                            curField = apiClass.getListSearch("fields", "name", key);
                        } else {
                            curField = null;
                        }
                        break;
                    case VALUE_NUMBER_INT:
                    case VALUE_NUMBER_FLOAT:
                        if (curField != null) {
                            String strFldType = curField.getString("type");

                            switch (strFldType.toLowerCase()) {
                                case "integerarray":
                                    if (obj.isNull(key)) {
                                        obj.createIntegerArray(key);
                                    }

                                    obj.getIntegerArray(key).add(parse.getIntValue());
                                    break;
                                case "integer":
                                    obj.setInteger(key, parse.getIntValue());
                                    break;

                                case "double":
                                    obj.setDouble(key, parse.getDoubleValue());

                                    break;

                                case "doublearray":
                                    if (obj.isNull(key)) {
                                        obj.createDoubleArray(key);
                                    }

                                    obj.getDoubleArray(key).add(parse.getDoubleValue());
                                    break;
                                default:
                                    if (inArray) {
                                        if (obj.getCoreObject(key) == null) {
                                            if (parse.currentToken() == JsonToken.VALUE_NUMBER_INT) {
                                                obj.createIntegerArray(key);
                                                arrayType = ApiObject.TYPE_INTEGERARRAY;
                                            } else {
                                                obj.createDoubleArray(key);
                                                arrayType = ApiObject.TYPE_DOUBLEARRAY;
                                            }
                                        }

                                        if (arrayType == ApiObject.TYPE_INTEGERARRAY) {
                                            obj.getIntegerArray(key).add(parse.getIntValue());
                                        } else {
                                            obj.getDoubleArray(key).add(parse.getDoubleValue());
                                        }
                                    } else {
                                        if (parse.currentToken() == JsonToken.VALUE_NUMBER_INT) {
                                            obj.setLong(key, parse.getLongValue());
                                        } else {
                                            obj.setDouble(key, parse.getDoubleValue());
                                        }
                                    }

                                    break;
                            }
                        } else {
                            if (inArray) {
                                if (obj.getCoreObject(key) == null) {
                                    if (parse.currentToken() == JsonToken.VALUE_NUMBER_INT) {
                                        obj.createIntegerArray(key);
                                        arrayType = ApiObject.TYPE_INTEGERARRAY;
                                    } else {
                                        obj.createDoubleArray(key);
                                        arrayType = ApiObject.TYPE_DOUBLEARRAY;
                                    }
                                }

                                if (arrayType == ApiObject.TYPE_INTEGERARRAY) {
                                    obj.getIntegerArray(key).add(parse.getIntValue());
                                } else {
                                    obj.getDoubleArray(key).add(parse.getDoubleValue());
                                }
                            } else {
                                if (parse.currentToken() == JsonToken.VALUE_NUMBER_INT) {
                                    obj.setLong(key, parse.getLongValue());
                                } else {
                                    obj.setDouble(key, parse.getDoubleValue());
                                }
                            }
                        }

                        break;
                    case VALUE_STRING:
                        if (curField != null) {
                            if ("Date".equals(curField.getString("type"))
                                    || "DateTime".equals(curField.getString("type"))) {
                                if (!_dtFormats.containsKey(key)) {
                                    String format = curField.getString("format");

                                    if (format != null && !format.isEmpty()) {
                                        DateTimeFormatter dtFormat = DateTimeFormatter.
                                                ofPattern(format);

                                        _dtFormats.put(key, dtFormat);
                                    } else {
                                        _dtFormats.put(key, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                                    }
                                }

                                if (parse.getText().length() > 0) {
                                    obj.setDateTime(key, OffsetDateTime.parse(parse.getText(), _dtFormats.get(key)));
                                }
                            } else if ("ArrayString".equals(curField.getString("type"))) {
                                if (obj.isNull(key)) {
                                    obj.createStringArray(key);
                                }

                                obj.getStringArray(key).add(parse.getText());
                            } else {
                                if (inArray) {
                                    if (obj.getStringArray(key) == null) {
                                        obj.createStringArray(key);
                                    }

                                    obj.getStringArray(key).add(parse.getText());
                                } else {
                                    obj.setString(key, parse.getText());
                                }
                            }
                        } else {
                            if (inArray) {
                                if (obj.getStringArray(key) == null) {
                                    obj.createStringArray(key);
                                }

                                obj.getStringArray(key).add(parse.getText());
                            } else {
                                obj.setString(key, parse.getText());
                            }
                        }
                        break;
                    case VALUE_FALSE:
                        obj.setBoolean(key, false);
                        break;
                    case VALUE_TRUE:
                        obj.setBoolean(key, true);
                        break;
                    case VALUE_NULL:
                        break;
                    case START_ARRAY:
                        if (key == null) {
                            key = "root";
                            iFirstObj = false;
                        }
                        inArray = true;
                        break;
                    case END_ARRAY:
                        inArray = false;
                        break;
                    case END_OBJECT:
                        // We are at the end of this object
                        break OUTER;
                    default:
                        break;
                }
            }
        } catch (IOException io) {
            throw new ApiException(io.getMessage(), io);
        }
    }

    @Override
    public ApiObject parseSingle(Reader in) throws ApiException, ApiClassNotFoundException {
        return parseSingle(in, null);
    }

    @Override
    public ApiObject parseSingle(Reader in, String className) throws ApiException, ApiClassNotFoundException {
        ApiObject retObj;

        try {
            JsonParser parse = _factory.createParser(in);

            if (_classes != null && className == null) {
                retObj = _classes.getInstance(_mainClass);
            } else if (_classes != null && className != null) {
                retObj = _classes.getInstance(className);
            } else {
                retObj = new ApiObject();
            }

            parseJsontoObject(_classes, _mainClass, parse, retObj, true);
        } catch (IOException io) {
            throw new ApiException(io.getMessage(), io);
        }

        return retObj;
    }

    @Override
    public List<String> getParseErrors() {
        return this.parseErrors;
    }

    @Override
    public Throwable getThrowException() {
        return throwException;
    }
}
