package com.progbits.api.parser;

import com.progbits.api.ObjectParser;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClass;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.BufferedReader;
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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.events.CollectionEndEvent;
import org.yaml.snakeyaml.events.CollectionStartEvent;
import org.yaml.snakeyaml.events.DocumentEndEvent;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.MappingEndEvent;
import org.yaml.snakeyaml.events.MappingStartEvent;
import org.yaml.snakeyaml.events.ScalarEvent;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

/**
 *
 * @author scarr
 */
@Component(name = "YamlObjectParser", immediate = true, property
        = {
            "type=YAML", "name=YamlObjectParser"
        })
public class YamlObjectParser implements ObjectParser {

    private ApiObject _obj = null;

    private String _mainClass;
    private ApiClasses _classes;
    private Iterable<Event> _parser;
    private Yaml _factory = new Yaml(new Constructor(), new Representer(), new DumperOptions(),
            new Resolver());
    private Resolver resolver = new Resolver();
    private Map<String, String> _props;
    private List<String> parseErrors;
    private Throwable throwException;

    private Map<String, DateTimeFormatter> _dtFormats = new HashMap<>();

    public YamlObjectParser() {

    }

    public YamlObjectParser(boolean genericProcessor) {
        if (genericProcessor) {
            internalInit(null, null, null, null);
        }
    }

    private void internalInit(ApiClasses classes, String mainClass,
            Map<String, String> properties, Reader in) {
        if (in != null) {
            _parser = _factory.parse(in);
        }
        _props = properties;
        _classes = classes;
        _mainClass = mainClass;
        this.parseErrors = new ArrayList<>();
    }

    @Override
    public ObjectParser getParser() {
        return new YamlObjectParser();
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
            parseYamltoObject(_classes, _mainClass, _parser, _obj, true);

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

    public void parseYamltoObject(ApiClasses apiClasses, String curClass,
            Iterable<Event> parser, ApiObject obj, boolean bFirst) throws ApiException, ApiClassNotFoundException {
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
            while (parser.iterator().hasNext()) {
                Event event = parser.iterator().next();

                if (event instanceof DocumentEndEvent) {
                    break;
                }

                if (event instanceof MappingStartEvent) {
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

                        parseYamltoObject(apiClasses, nObj.getName(), parser, nObj,
                                false);

                        if (inArray) {
                            if (obj.getList(key) == null) {
                                obj.createList(key);
                            }

                            obj.getList(key).add(nObj);
                        } else {
                            obj.setObject(key, nObj);
                            key = null;
                        }
                    }
                } else if (event instanceof MappingEndEvent) {
                    return;
                } else if (event instanceof CollectionStartEvent) {
                    if (key == null) {
                        key = "root";
                        iFirstObj = false;
                    }
                    inArray = true;
                } else if (event instanceof CollectionEndEvent) {
                    key = null;
                    inArray = false;
                } else if (event instanceof ScalarEvent) {
                    ScalarEvent se = (ScalarEvent) event;

                    if (key == null) {
                        key = se.getValue();

                        if (apiClass != null) {
                            curField = apiClass.getListSearch("fields", "name", key);
                        } else {
                            curField = null;
                        }
                    } else {
                        Tag t = resolver.resolve(NodeId.scalar, se.getValue(), true);

                        processScalar(obj, key, curField, t.getValue(), se.getValue(), inArray);

                        if (!inArray) {
                            key = null;
                        }
                    }
                }
            }
        } catch (ApiClassNotFoundException | ApiException ex) {

        }
    }

    private void processScalar(ApiObject obj, String key, ApiObject curField, String resolvedType, String subject, boolean inArray) {

        switch (resolvedType) {
            case "tag:yaml.org,2002:str":
                if (inArray) {
                    if (!obj.containsKey(key)) {
                        obj.createStringArray(key);
                    }

                    obj.getStringArray(key).add(subject);
                } else {
                    obj.setString(key, subject);
                }
                break;

            case "tag:yaml.org,2002:timestamp":
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

                        if (subject.length() > 0) {
                            obj.setDateTime(key, OffsetDateTime.parse(subject, _dtFormats.get(key)));
                        }
                    }
                } else {
                    DateTimeFormatter dtFormat = _dtFormats.get(key);

                    if (null == dtFormat) {
                        dtFormat = _dtFormats.put(key, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    }

                    obj.setDateTime(key, OffsetDateTime.parse(subject, _dtFormats.get(key)));
                }
                break;

            case "tag:yaml.org,2002:null":
                break;

            case "tag:yaml.org,2002:int":
                if (inArray) {
                    if (!obj.containsKey(key)) {
                        obj.createIntegerArray(key);
                    }

                    obj.getIntegerArray(key).add(Integer.parseInt(subject));
                } else {
                    obj.setLong(key, Long.parseLong(subject));
                }
                break;

            case "tag:yaml.org,2002:float":
                if (inArray) {
                    if (!obj.containsKey(key)) {
                        obj.createDoubleArray(key);
                    }

                    obj.getDoubleArray(key).add(Double.parseDouble(subject));
                } else {
                    obj.setDouble(key, Double.parseDouble(subject));
                }
                break;

            case "tag:yaml.org,2002:bool":
                obj.setBoolean(key, Boolean.parseBoolean(subject));
                break;

        }
    }

    @Override
    public ApiObject parseSingle(Reader in) throws ApiException, ApiClassNotFoundException {
        return parseSingle(in, null);
    }

    @Override
    public ApiObject parseSingle(Reader in, String className) throws ApiException, ApiClassNotFoundException {
        ApiObject retObj;

        Iterable<Event> parse = _factory.parse(in);

        if (_classes != null && className != null) {
            retObj = _classes.getInstance(className);
        } else {
            retObj = new ApiObject();
        }

        parseYamltoObject(_classes, _mainClass, parse, retObj, true);

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
