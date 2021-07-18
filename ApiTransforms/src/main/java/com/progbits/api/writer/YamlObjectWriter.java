package com.progbits.api.writer;

import com.progbits.api.ObjectWriter;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
@Component(name = "YamlObjectWriter",
        immediate = true,
        property = {
            "type=YAML", "name=YamlObjectWriter"
        }
)
public class YamlObjectWriter implements ObjectWriter {

    private static final Logger log = LoggerFactory.getLogger(YamlObjectWriter.class);

    private ApiClasses _classes;
    private Writer _writer = null;
    private Map<String, String> _props = null;
    private List<String> writeErrors = new ArrayList<>();
    private Throwable throwException = null;

    private String mainClassName = null;

    private Map<String, DateTimeFormatter> _dtFormats = new HashMap<>();

    @Override
    public ObjectWriter getWriter() {
        return new YamlObjectWriter();
    }

    public YamlObjectWriter() {
    }

    public YamlObjectWriter(boolean genericProcessor) {
        if (genericProcessor) {
            internalInit(null, null, null);
        }
    }

    private void internalInit(ApiClasses classes, Map<String, String> properties, Writer out) {
        _writer = out;
        _props = properties;
        _classes = classes;
    }

    @Override
    public void init(ApiClasses classes, Map<String, String> properties,
            Writer out) throws ApiException {
        internalInit(classes, properties, out);
    }

    @Override
    public void init(ApiClasses classes, String mainClassName, Map<String, String> properties,
            Writer out) throws ApiException {
        this.mainClassName = mainClassName;

        internalInit(classes, properties, out);
    }

    @Override
    public void initStream(ApiClasses classes, String mainClassName, Map<String, String> properties,
            OutputStream out) throws ApiException {
        this.mainClassName = mainClassName;

        BufferedOutputStream bout = null;

        if (out instanceof BufferedOutputStream) {
            bout = (BufferedOutputStream) out;
        } else {
            bout = new BufferedOutputStream(out);
        }

        _writer = new OutputStreamWriter(bout);
        _props = properties;
        _classes = classes;
    }

    @Override
    public void write(ApiObject obj) throws ApiException {
        convertObjectToYaml(_writer, obj, null, 0);
        try {
            _writer.flush();
        } catch (IOException io) {

        }
    }

    public void convertObjectToYaml(Writer writeOut, ApiObject apiObj,
            String name, Integer indentSpacing) throws ApiException {
        try {
            boolean bFirstTst = true;
            
            if (name == null) {
                writeStartMapping(writeOut, indentSpacing);
            } else {
                bFirstTst = false;
            }

            
            
            final AtomicBoolean bFirst = new AtomicBoolean(bFirstTst);

            this.writeErrors.clear();
            this.throwException = null;

            apiObj.getFields().forEach((fldKey, fldValue) -> {
                ApiObject fldDef = null;
                String format = null;

                try {
                    if (apiObj.getApiClass() != null) {
                        fldDef = apiObj.getApiClass().getListSearch(
                                "fields", "name", fldKey);

                        if (fldDef != null) {
                            format = fldDef.getString("format");
                        }
                    }
                    if (fldValue instanceof String) {
                        writeField(writeOut, fldKey, (String) fldValue, indentSpacing, bFirst.get());
                    } else if (fldValue instanceof List) {
                        String fldType = "arraylist";

                        if (fldDef != null) {
                            fldType = fldDef.getString("type", "arraylist");
                        } else {
                            int iType = apiObj.getType(fldKey);

                            List lstValue = (List) fldValue;

                            if (lstValue.size() > 0) {
                                Object obj = lstValue.get(0);

                                if (obj instanceof ApiObject) {
                                    fldType = "arraylist";
                                } else if (obj instanceof String) {
                                    fldType = "stringarray";
                                } else if (obj instanceof Integer) {
                                    fldType = "integerarray";
                                } else if (obj instanceof Double) {
                                    fldType = "doublearray";
                                }
                            }
                        }

                        switch (fldType.toLowerCase()) {
                            case "stringarray":
                                List<String> arrStrList = (List<String>) fldValue;

                                writeStartArrayFlow(writeOut, fldKey, indentSpacing);

                                int iCnt = 0;

                                for (String objs : arrStrList) {
                                    if (iCnt > 0) {
                                        writeValue(writeOut, ", ", indentSpacing);
                                    }

                                    writeValue(writeOut, objs, indentSpacing);

                                    iCnt++;
                                }

                                writeEndArrayFlow(writeOut, fldKey, indentSpacing);
                                break;

                            case "integerarray":
                                List<Integer> arrIntList = (List<Integer>) fldValue;

                                writeStartArrayFlow(writeOut, fldKey, indentSpacing);

                                int iCnt2 = 0;

                                for (Integer objs : arrIntList) {
                                    if (iCnt2 > 0) {
                                        writeValue(writeOut, ", ", indentSpacing);
                                    }

                                    writeValue(writeOut, String.valueOf(objs), indentSpacing);

                                    iCnt2++;
                                }

                                writeEndArrayFlow(writeOut, fldKey, indentSpacing);
                                break;

                            case "doublearray":
                                List<Double> arrDblList = (List<Double>) fldValue;

                                writeStartArrayFlow(writeOut, fldKey, indentSpacing);

                                int iCnt3 = 0;

                                for (Double objs : arrDblList) {
                                    if (iCnt3 > 0) {
                                        writeValue(writeOut, ", ", indentSpacing);
                                    }

                                    writeValue(writeOut, String.valueOf(objs), indentSpacing);

                                    iCnt3++;
                                }

                                writeEndArrayFlow(writeOut, fldKey, indentSpacing);
                                break;

                            default:
                                List<ApiObject> arrList = (List<ApiObject>) fldValue;

                                writeArrayList(writeOut, fldKey, arrList, indentSpacing);

                                break;
                        }
                    } else if (fldValue instanceof ApiObject) {
                        ApiObject obj = (ApiObject) fldValue;
                        try {
                            writeStartMapping(writeOut, fldKey, indentSpacing);

                            convertObjectToYaml(writeOut, obj, fldKey, indentSpacing + 4);
                        } catch (ApiException app) {
                            log.error("Internal Error", app);
                        }
                    } else if (fldValue instanceof Double) {
                        writeField(writeOut, fldKey, String.valueOf(fldValue), indentSpacing, bFirst.get());
                    } else if (fldValue instanceof BigDecimal) {
                        writeField(writeOut, fldKey, String.valueOf(fldValue), indentSpacing, bFirst.get());
                    } else if (fldValue instanceof Integer) {
                        writeField(writeOut, fldKey, String.valueOf(fldValue), indentSpacing, bFirst.get());
                    } else if (fldValue instanceof Boolean) {
                        writeField(writeOut, fldKey, String.valueOf(fldValue), indentSpacing, bFirst.get());
                    } else if (fldValue instanceof Long) {
                        writeField(writeOut, fldKey, String.valueOf(fldValue), indentSpacing, bFirst.get());
                    } else if (fldValue instanceof OffsetDateTime) {
                        if (!_dtFormats.containsKey(fldKey)) {
                            if (format != null && !format.isEmpty()) {
                                DateTimeFormatter dtFormat = DateTimeFormatter.ofPattern(format);

                                _dtFormats.put(fldKey, dtFormat);
                            } else {
                                _dtFormats.put(fldKey, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                            }
                        }

                        OffsetDateTime dtValue = (OffsetDateTime) fldValue;

                        writeField(writeOut, fldKey, dtValue.format(_dtFormats.get(fldKey)), indentSpacing, bFirst.get());
                    }
                } catch (IOException ex) {
                    if (!this.writeErrors.contains(ex.getMessage())) {
                        this.writeErrors.add(ex.getMessage());
                    }
                    this.throwException = ex;
                }

                if (bFirst.get()) {
                    bFirst.set(false);
                }
            });

            if (name != null) {
                writeEndMapping(writeOut);
            } else {
                writeEndMapping(writeOut);
            }
        } catch (IOException io) {
            throw new ApiException(io.getMessage(), io);
        }
    }

    private void createSpacing(Writer writeOut, Integer indentSpacing) throws IOException {
        for (int x = 0; x < indentSpacing; x++) {
            writeOut.append(" ");
        }
    }

    private void writeField(Writer writeOut, String fieldName, String fieldValue, Integer indentSpacing, boolean bFirst) throws IOException {
        if (!bFirst) {
            createSpacing(writeOut, indentSpacing);
        }
        writeOut.append(fieldName).append(": ").append(fieldValue).append("\n");
    }

    private void writeValue(Writer writeOut, String fieldValue, Integer indentSpacing) throws IOException {
        createSpacing(writeOut, indentSpacing);
        writeOut.append(fieldValue);
    }

    private void writeStartDocument(Writer writeOut) throws IOException {
        writeOut.append("---").append("\n");
    }

    private void writeEndDocument(Writer writeOut) throws IOException {
        writeOut.append("...").append("\n");
    }

    private void writeStartMapping(Writer writeOut, Integer indentSpacing) throws IOException {
        if (indentSpacing >= 4) {
            createSpacing(writeOut, indentSpacing - 2);
            writeOut.append("- ");
        }
    }

    private void writeStartMapping(Writer writeOut, String name, Integer indentSpacing) throws IOException {
        createSpacing(writeOut, indentSpacing);
        //writeOut.append("- ");
        writeOut.append(name).append(":\n");
    }

    private void writeEndMapping(Writer writeOut) throws IOException {
        // Nothing really to do here
    }

    private void writeStartArray(Writer writeOut, String fieldName, Integer indentSpacing) throws IOException {
        if (indentSpacing > 0) {
            createSpacing(writeOut, indentSpacing);
        }

        writeOut.append(fieldName).append(": \n");
    }

    private void writeStartArrayFlow(Writer writeOut, String fieldName, Integer indentSpacing) throws IOException {
        if (indentSpacing > 0) {
            createSpacing(writeOut, indentSpacing);
        }

        writeOut.append(fieldName).append(": [ ");
    }

    private void writeEndArrayFlow(Writer writeOut, String fieldName, Integer indentSpacing) throws IOException {
        if (indentSpacing > 0) {
            createSpacing(writeOut, indentSpacing);
        }

        writeOut.append(" ] \n");
    }

    private void writeEndArray(Writer writeOut) throws IOException {
        // Nothing really to do here
    }

    private void writeStreamStart(Writer writeOut) throws IOException {
        // Nothing really to do here
    }

    private void writeStreamEnd(Writer writeOut) throws IOException {
        // Nothing really to do here
    }

    private void writeArrayList(Writer writeOut, String fldKey, List<ApiObject> arrList, Integer indentSpacing) {
        try {
            if (null != fldKey) {
                writeStartArray(writeOut, fldKey, indentSpacing);
            }

            for (ApiObject objs : arrList) {
                try {
                    convertObjectToYaml(writeOut, objs, null, indentSpacing + 4);
                } catch (ApiException app) {
                    log.error("writeArrayList", app);
                }
            }

            writeEndArray(writeOut);
        } catch (IOException io) {
            log.error("writeArrayList", io);
        }
    }

    @Override
    public String writeSingle(ApiObject obj) throws ApiException {
        StringWriter writeOut = new StringWriter(10000);

        try {
            writeStreamStart(writeOut);
            writeStartDocument(writeOut);

            if (obj.size() == 1 && obj.containsKey("root")) {
                writeArrayList(writeOut, null, obj.getList("root"), 0);
            } else {
                convertObjectToYaml(writeOut, obj, null, 0);
            }

            writeEndDocument(writeOut);
            writeStreamEnd(writeOut);
            writeOut.flush();
        } catch (IOException io) {
            throw new ApiException(io.getMessage());
        }

        return writeOut.toString();
    }

    @Override
    public void writeHeader() throws ApiException {
        try {
            writeStartArray(_writer, null, 0);
        } catch (IOException io) {
            throw new ApiException(io.getMessage(), io);
        }
    }

    @Override
    public List<String> getWriteErrors() {
        return this.writeErrors;
    }

    @Override
    public Throwable getThrowException() {
        return this.throwException;
    }
}
