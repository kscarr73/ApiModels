package com.progbits.api.parser;

import com.progbits.api.ObjectParser;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.formaters.TransformDate;
import com.progbits.api.formaters.TransformDecimal;
import com.progbits.api.formaters.TransformNumber;
import com.progbits.api.formaters.TransformString;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
@Component(name = "FixedWidthNoLineParser",
        immediate = true,
        property = {
            "type=FIXEDNOLINE", "name=FixedWidthNoLineParser"
        }
)
public class FixedWidthNoLineParser implements ObjectParser {

    private ApiObject _subject = null;

    private String _mainClass;
    private ApiClasses _classes;
    private BufferedReader br = null;
    private Map<String, String> _props;
    private List<String> parseErrors;
    private Throwable throwException;
    private Map<String, DateTimeFormatter> _dtFormats = new HashMap<>();

    @Override
    public void initStream(ApiClasses classes, String mainClass,
            Map<String, String> properties, InputStream in) throws ApiException {
        init(classes, mainClass, properties, new BufferedReader(new InputStreamReader(in)));
    }

    @Override
    public void init(ApiClasses classes, String mainClass,
            Map<String, String> properties, Reader in) throws ApiException {

        if (in != null) {
            if (in instanceof BufferedReader) {
                br = (BufferedReader) in;
            } else {
                br = new BufferedReader(in);
            }
        }

        _props = properties;
        _classes = classes;
        _mainClass = mainClass;
        parseErrors = new ArrayList<>();
    }

    @Override
    public boolean next() throws ApiException, ApiClassNotFoundException {
        boolean bNotEnd = false;

        try {
            String strLine = br.readLine();

            if (strLine != null) {
                int iCurLoc = 0;

                _subject = _classes.getInstance(_mainClass);

                parseObject(iCurLoc, strLine, _subject);

                bNotEnd = true;
            }
        } catch (Exception iex) {
            throw new ApiException(iex.getMessage(), iex);
        }

        return bNotEnd;
    }

    private int parseObject(int currLoc, String strLine, ApiObject obj) throws ApiException {
        for (ApiObject fld : obj.getApiClass().getList("fields")) {
            Object oFldLength = fld.getCoreObject("length");

            int iFieldLength = 0;

            if (oFldLength instanceof Integer) {
                iFieldLength = (Integer) oFldLength;
            } else if (oFldLength instanceof Long) {
                iFieldLength = ((Long) oFldLength).intValue();
            }

            String fieldValue = strLine.substring(currLoc,
                    currLoc + iFieldLength);

            currLoc += iFieldLength;

            try {
                switch (fld.getString("type")) {
                    case "String":
                        obj.getFields().put(fld.getString("name"),
                                TransformString.
                                        transformString(fieldValue, fld.
                                                getString("format")));
                        ///System.out.println(fld.getString("name") +  " " + fieldValue + " Length = " + iFieldLength);
                        break;

                    case "Decimal":
                        obj.getFields().put(fld.getString("name"),
                                TransformDecimal.
                                        transformDecimal(fieldValue, fld.
                                                getString("format")));
                        break;

                    case "Double":
                        obj.getFields().put(fld.getString("name"),
                                TransformDecimal.
                                        transformDouble(fieldValue, fld.
                                                getString("format")));
                        break;

                    case "DateTime":
                        obj.getFields().put(fld.getString("name"),
                                TransformDate.
                                        transformDate(fieldValue, fld.getString(
                                                "format")));
                        break;

                    case "Integer":
                        obj.getFields().put(fld.getString("name"),
                                TransformNumber.
                                        transformInteger(fieldValue, fld.
                                                getString("format")));
                        break;

                    case "Boolean":
                        if ("true".equalsIgnoreCase(fieldValue)) {
                            obj.getFields().put(fld.getString("name"), true);
                        } else {
                            obj.getFields().put(fld.getString("name"), false);
                        }

                        break;

                    case "Long":
                        obj.getFields().put(fld.getString("name"),
                                TransformNumber.
                                        transformLong(fieldValue, fld.getString(
                                                "format")));
                        break;

                    case "Object":
                        ApiObject newObj = _classes.getInstance(fld.getString(
                                "subType"));
                        currLoc = parseObject(currLoc, strLine, newObj);
                        obj.setObject(fld.getString("name"), newObj);
                        break;

                    case "ArrayList":
                        if (fld.containsKey("iterationCount")) {
                            int iCnt = obj.getInteger(fld.getString("iterationCount"));

                            if (!obj.isSet(fld.getString("name"))) {
                                obj.createList(fld.getString("name"));
                            }

                            for (var i = 0; i < iCnt; i++) {
                                ApiObject newArr = _classes.getInstance(fld.
                                        getString("subType"));
                                currLoc = parseObject(currLoc, strLine, newArr);

                                obj.getList(fld.getString("name")).add(newArr);
                            }
                        } else {
                            int totalBytes = strLine.length();
                            while (currLoc < totalBytes) {
                                ApiObject newArr = _classes.getInstance(fld.
                                        getString("subType"));
                                currLoc = parseObject(currLoc, strLine, newArr);
                                if (!obj.isSet(fld.getString("name"))) {
                                    obj.createList(fld.getString("name"));
                                }
                                obj.getList(fld.getString("name")).add(newArr);
                            }
                        }
                        break;
                }
            } catch (Exception ex) {
                if (!this.parseErrors.contains("Field: " + fld.getString("name") + " " + ex.getMessage())) {
                    this.parseErrors.add("Field: " + fld.getString("name") + " " + ex.getMessage());
                }
                this.throwException = ex;
            }
        }

        return currLoc;
    }

    @Override
    public ApiObject getObject() {
        return _subject;
    }

    @Override
    public ObjectParser getParser() {
        return new FixedWidthNoLineParser();
    }

    @Override
    public ApiObject parseSingle(Reader in) throws ApiException, ApiClassNotFoundException {
        return parseSingle(in, null);
    }

    @Override
    public ApiObject parseSingle(Reader in, String className) throws ApiException, ApiClassNotFoundException {
        BufferedReader br;
        ApiObject objResp = null;

        if (in instanceof BufferedReader) {
            br = (BufferedReader) in;
        } else {
            br = new BufferedReader(in);
        }

        try {
            String strLine = br.readLine();

            int iCurLoc = 0;

            objResp = _classes.getInstance(className);
            parseObject(iCurLoc, strLine, objResp);
        } catch (IOException io) {
            throw new ApiException(500, io.getMessage());
        }

        return objResp;
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
