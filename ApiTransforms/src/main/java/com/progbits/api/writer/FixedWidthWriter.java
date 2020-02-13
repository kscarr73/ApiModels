package com.progbits.api.writer;

import com.progbits.api.ObjectWriter;
import com.progbits.api.exception.ApiException;
import com.progbits.api.formaters.TransformDate;
import com.progbits.api.formaters.TransformDecimal;
import com.progbits.api.formaters.TransformNumber;
import com.progbits.api.formaters.TransformString;
import com.progbits.api.model.ApiClass;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
@Component(name = "FixedWidthObjectWriter", immediate = true, property = {
    "type=FIXED", "name=FixedWidthObjectWriter"
}
)
public class FixedWidthWriter implements ObjectWriter {

    private BufferedWriter _write;
    private Map<String, String> _props = null;
    private ApiClasses _classes = null;
    private Map<String, Object> attributeMaps = null;
    private Map<Integer, String> attributeFieldNames;
    private Map<String, DateTimeFormatter> _dtFormats = new HashMap<>();
    private int attributeSeqNo = 0;
    private String mainClassName = null;
    private Boolean bSegmented = null;
    private List<String> writeErrors = new ArrayList<>();
    private Throwable throwException = null;

    private enum CONSTANTS {
        format, name, attribute, value, field, fields, length, type, DisplayName, WithHeader
    }

    private enum TYPES {
        String, Object, ArrayList, Decimal, Double, Integer, Long, DateTime, Boolean
    }

    @Override
    public void init(ApiClasses ac, Map<String, String> props, Writer writer) throws ApiException {
        this._classes = ac;
        if (writer instanceof BufferedWriter) {
            this._write = (BufferedWriter) writer;
        } else {
            this._write = new BufferedWriter(writer);
        }

        this._props = props;

        // initialize the segment fixed-width attribute IDs
        this.attributeMaps = new HashMap<>();

    }

    @Override
    public void init(ApiClasses classes, String mainClassName, Map<String, String> properties,
            Writer out) throws ApiException {
        this._classes = classes;

        if (out instanceof BufferedWriter) {
            this._write = (BufferedWriter) out;
        } else {
            this._write = new BufferedWriter(out);
        }

        this._props = properties;
        this.mainClassName = mainClassName;
        // initialize the segment fixed-width attribute IDs
        this.attributeMaps = new HashMap<>();
    }

    @Override
    public void initStream(ApiClasses classes, String mainClassName, Map<String, String> properties,
            OutputStream out) throws ApiException {
        this._classes = classes;

        this._write = new BufferedWriter(new OutputStreamWriter(out));

        this._props = properties;
        this.mainClassName = mainClassName;
        // initialize the segment fixed-width attribute IDs
        this.attributeMaps = new HashMap<>();
    }

    @Override
    public void write(ApiObject ao) throws ApiException {
        if (bSegmented == null) {
            analyzeObject(ao);
        }

        if (bSegmented) {
            writeObjectSegmented(ao);
        } else {
            writeObjectSingle(ao);
        }
    }

    private void analyzeObject(ApiObject ao) {
        if (ao.getApiClass() != null) {
            boolean bTest = false;

            for (ApiObject fld : ao.getApiClass().getList(CONSTANTS.fields.name())) {
                if (TYPES.Object.name().equalsIgnoreCase(fld.getString(CONSTANTS.type.name()))
                        || TYPES.ArrayList.name().equalsIgnoreCase(fld.getString(CONSTANTS.type.name()))) {
                    bTest = true;
                    break;
                }
            }

            bSegmented = bTest;
        } else {
            bSegmented = false;
        }
    }

    public void writeObjectSingle(ApiObject fixedWidthObj) throws ApiException {
        for (ApiObject fld : fixedWidthObj.getApiClass().getList(CONSTANTS.fields.name())) {
            Object oFldLength = fld.getCoreObject(CONSTANTS.length.name());

            int iFieldLength = 0; //
            this.getWriteErrors().clear();
            this.throwException = null;

            if (oFldLength instanceof Integer) {
                iFieldLength = (Integer) oFldLength;
            } else if (oFldLength instanceof Long) {
                iFieldLength = ((Long) oFldLength).intValue();
            }

            Object fieldValue = fixedWidthObj.getCoreObject(fld.getString(CONSTANTS.name.name()));
            String fieldFormat = fld.getString(CONSTANTS.format.name());

            try {
                TYPES type = TYPES.String;
                try {
                    type = TYPES.valueOf(fld.getString(CONSTANTS.type.name()));
                } catch (Exception e) {
                    // use the String as default if no found on Eum.TYPES
                    type = TYPES.String;
                }

                switch (type) {
                    case String:
                        this._write.append(TransformString.forceRightSize(
                                TransformString.formatString(fieldValue, fieldFormat), " ", iFieldLength));
                        break;
                    case Double:
                    case Decimal:
                        if (fieldFormat.contains("L")) {
                            fieldFormat = fieldFormat.replace("L", "");

                            this._write.append(TransformString.forceRightSize(
                                    TransformDecimal.formatDecimal(fieldValue, fieldFormat),
                                    " ", iFieldLength)
                            );
                        } else {
                            this._write.append(TransformString.forceLeftSize(
                                    TransformDecimal.formatDecimal(fieldValue, fieldFormat),
                                    " ", iFieldLength)
                            );
                        }

                        break;
                    case DateTime:
                        this._write.append(TransformString.forceRightSize(TransformDate.formatDate(fieldValue, fieldFormat),
                                " ", iFieldLength));
                        break;
                    case Integer:
                        if (fieldFormat.contains("L")) {
                            fieldFormat = fieldFormat.replace("L", "");

                            this._write.append(TransformString.forceRightSize(TransformNumber
                                    .formatInteger(fieldValue, fieldFormat), " ", iFieldLength));
                        } else {
                            this._write.append(TransformString.forceLeftSize(TransformNumber
                                    .formatInteger(fieldValue, fieldFormat), " ", iFieldLength));
                        }

                        break;
                    case Boolean:
                        this._write.append(TransformString.forceRightSize(fixedWidthObj.getBoolean(fld.getString(CONSTANTS.name
                                .name())) ? "1" : "0", " ", iFieldLength));
                        break;
                    case Long:
                        if (fieldFormat.contains("L")) {
                            fieldFormat = fieldFormat.replace("L", "");

                            this._write.append(TransformString.forceRightSize(TransformNumber.
                                    formatLong(fieldValue, fieldFormat),
                                    " ", iFieldLength));
                        } else {
                            this._write.append(TransformString.
                                    forceLeftSize(TransformNumber.formatLong(fieldValue, fieldFormat),
                                            " ", iFieldLength));
                        }

                        break;
                }
            } catch (Exception ex) {
                throw new ApiException("Field: " + fld.getString(CONSTANTS.name.name()) + " " + ex.
                        getMessage(), ex);
            }
        }

        try {
            this._write.append("\n");
            this._write.flush();
        } catch (IOException io) {
            throw new ApiException(io.getMessage(), io);
        }

        return;
    }

    public void writeObjectSegmented(ApiObject fixedWidthObj) throws ApiException {

        if (fixedWidthObj == null) {
            return;
        }
        int record_seq_id = 0;
        this.getWriteErrors().clear();
        this.throwException = null;

        // To clear the lower sort keys from the hashmap when the upper key changes
        // e.g. IN123456
        //      BTWhite co
        //      STSAMSUNG CO
        //      IN789123
        Map<String, Object> segmentObjects = fixedWidthObj.getFields();
        if (this.attributeFieldNames == null || this.attributeFieldNames.isEmpty()) {
            storeAttributeSoryFieldNames(segmentObjects);
        }

        for (String objectKey : segmentObjects.keySet()) {
            ApiObject object = (ApiObject) segmentObjects.get(objectKey);
            boolean ignoreDupliateRecord = false;
            // Set the sequence id of the record
            // e.g. 1 = HR
            //      2 = IN
            //      3 = BT etc
            record_seq_id++;

            try {
                for (ApiObject fld : object.getApiClass().getList(CONSTANTS.fields.name())) {
                    Object oFldLength = fld.getCoreObject(CONSTANTS.length.name());

                    int iFieldLength = 0; //

                    if (oFldLength instanceof Integer) {
                        iFieldLength = (Integer) oFldLength;
                    } else if (oFldLength instanceof Long) {
                        iFieldLength = ((Long) oFldLength).intValue();
                    }

                    Object fieldValue = object.getCoreObject(fld.getString(CONSTANTS.name.name()));
                    String fieldFormat = fld.getString(CONSTANTS.format.name());

                    // Validate and compare the attributeId to write or bypass the record
                    boolean attribute = fld.getBoolean(CONSTANTS.attribute.name());
                    if (attribute) {
                        //  Retrive the attribute's field name from the REC_TYPE field that has an attribute set on like partner id or invoice number etc....
                        String attributeName = fld.getString(CONSTANTS.format.name());
                        Object attributeValue = object.getCoreObject(attributeName);

                        // TO be considered for the null value of the attribute field whether we need to write the first record and the rest folloing record
                        // such as ean =null in IT record or San = null in DC Record
                        attributeValue = (null == attributeValue) ? "" : attributeValue;

                        if (null != this.attributeMaps
                                && this.attributeMaps.containsKey(attributeName) && this.attributeMaps.get(attributeName)
                                .equals(attributeValue)) {
                            ignoreDupliateRecord = true;
                            continue;
                        } else if (fieldValue != null && this.attributeMaps != null) {

                            if (!this.attributeMaps.containsKey(attributeName)) {
                                this.attributeMaps.put(attributeName, attributeValue);
                            } else {
                                // put the attribute id into the map
                                this.attributeMaps.replace(attributeName, attributeValue);

                                // Reset the lower attribute's field names in the attriteSortFieldName cache map
                                int indexOf = record_seq_id;
                                if (this.attributeFieldNames != null && this.attributeFieldNames.size() > record_seq_id) {
                                    indexOf++;
                                    int totalKeys = this.attributeFieldNames.size();
                                    for (int keyId : this.attributeFieldNames.keySet()) {
                                        if (keyId > record_seq_id) {
                                            String sortFieldName = this.attributeFieldNames.get(keyId);
                                            if (this.attributeMaps.containsKey(sortFieldName)) {
                                                this.attributeMaps.remove(sortFieldName);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // For getting the Rec_type =HR,IN,IT etc
                        // Do not use any format for the REC_TYPE Columns below
                        fieldFormat = null;
                    } else if (ignoreDupliateRecord) {
                        continue;
                    }

                    try {
                        TYPES type = TYPES.String;
                        try {
                            type = TYPES.valueOf(fld.getString(CONSTANTS.type.name()));
                        } catch (Exception e) {
                            // use the String as default if no found on Eum.TYPES
                            type = TYPES.String;
                        }

                        switch (type) {
                            case String:
                                this._write.append(TransformString.forceRightSize(
                                        TransformString.formatString(fieldValue, fieldFormat), " ", iFieldLength));
                                break;
                            case Decimal:
                            case Double:
                                if (fieldFormat.contains("L")) {
                                    fieldFormat = fieldFormat.replace("L", "");

                                    this._write.append(TransformString.forceRightSize(TransformDecimal.formatDecimal(fieldValue,
                                            fieldFormat), " ", iFieldLength));
                                } else {
                                    this._write.append(TransformString.forceLeftSize(TransformDecimal.formatDecimal(fieldValue,
                                            fieldFormat), " ", iFieldLength));
                                }
                                break;
                            case DateTime:
                                this._write.append(TransformString.forceRightSize(TransformDate.formatDate(fieldValue,
                                        fieldFormat), " ", iFieldLength));
                                break;
                            case Integer:
                                if (fieldFormat.contains("L")) {
                                    fieldFormat = fieldFormat.replace("L", "");
                                    this._write.append(TransformString.forceRightSize(TransformNumber.formatInteger(fieldValue,
                                            fieldFormat), " ", iFieldLength));
                                } else {
                                    this._write.append(TransformString.forceLeftSize(TransformNumber.formatInteger(fieldValue,
                                            fieldFormat), " ", iFieldLength));
                                }
                                break;
                            case Boolean:
                                this._write.append(TransformString.forceRightSize((object.getBoolean(fld.getString(CONSTANTS.name
                                        .name())) ? "1" : "0"), " ", iFieldLength));
                                break;
                            case Long:
                                if (fieldFormat.contains("L")) {
                                    fieldFormat = fieldFormat.replace("L", "");

                                    this._write.append(TransformString.forceRightSize(TransformNumber.formatLong(fieldValue,
                                            fieldFormat), " ", iFieldLength));
                                } else {
                                    this._write.append(TransformString.forceLeftSize(TransformNumber.formatLong(fieldValue,
                                            fieldFormat), " ", iFieldLength));
                                }

                                break;
                            case Object:
                                this._write.append(fld.getString("name"));
                                writeObjectSegmented(object.getObject(fld.getString(CONSTANTS.name.name())));
                                this._write.append("\n");
                                break;
                            case ArrayList:
                                for (ApiObject rowObj : object.
                                        getList(fld.getString(CONSTANTS.name.name()))) {
                                    this._write.append(TransformString.forceRightSize(fld.getString(CONSTANTS.name.name()), " ",
                                            iFieldLength));
                                    writeObjectSegmented(rowObj);
                                    this._write.append("\n");
                                }
                                break;
                        }
                    } catch (Exception ex) {
                        String errorMsg = "Field: " + fld.getString(CONSTANTS.name.name()) + " " + ex.
                                getMessage();
                        if (!this.writeErrors.contains(errorMsg)) {
                            this.writeErrors.add(errorMsg);
                        }
                        this.throwException = ex;
                    }
                }

                if (!ignoreDupliateRecord) {
                    this._write.append("\n");
                    this._write.flush();
                }
            } catch (Exception ex) {
                if (!this.writeErrors.contains(ex.getMessage())) {
                    this.writeErrors.add(ex.getMessage());
                }
                this.throwException = ex;
            } finally {
            }
        }//new iterator for the segment fixed-width
    }

    /**
     * For sorting multiple segments records, storing the sort key names
     *
     * @param segmentObjects
     */
    private void storeAttributeSoryFieldNames(Map<String, Object> segmentObjects) {

        if (this.attributeFieldNames == null) {
            this.attributeFieldNames = new HashMap<>();
        }
        attributeSeqNo = 1;
        for (String objectKey : segmentObjects.keySet()) {
            ApiObject object = (ApiObject) segmentObjects.get(objectKey);

            object.getApiClass().getList(CONSTANTS.fields.name()).forEach((fld) -> {
                boolean attribute = fld.getBoolean(CONSTANTS.attribute.name());
                if (attribute) {
                    if (!this.attributeFieldNames.containsKey(attributeSeqNo)) {
                        this.attributeFieldNames.put(attributeSeqNo, fld.getString(CONSTANTS.format.name()));
                        attributeSeqNo++;
                    }
                }
            });
        }
    }

    @Override
    public ObjectWriter getWriter() {
        FixedWidthWriter fww = new FixedWidthWriter();
        return fww;
    }

    @Override
    public void writeHeader() throws ApiException {
        boolean bDisplayName = "true".equalsIgnoreCase(this._props.get(CONSTANTS.DisplayName.name()));

        if ("true".equalsIgnoreCase(this._props.get(CONSTANTS.WithHeader.name()))) {
            ApiClass cls = this._classes.getClass(this.mainClassName);

            for (ApiObject fld : cls.getList(CONSTANTS.fields.name())) {
                String strFieldName = fld.getString(
                        bDisplayName ? CONSTANTS.DisplayName.name() : CONSTANTS.name.name());

                try {
                    Object objLength = fld.getCoreObject("length");
                    if (objLength instanceof Integer) {
                        _write.append(
                                TransformString.forceRightSize(strFieldName,
                                        " ", ((Integer) objLength)
                                )
                        );
                    } else if (objLength instanceof Long) {
                        _write.append(
                                TransformString.forceRightSize(strFieldName,
                                        " ", ((Long) objLength).intValue()
                                ));
                    }
                } catch (IOException io) {
                    throw new ApiException(io.getMessage(), io);
                }
            }

            try {
                _write.append("\n");
                _write.flush();
            } catch (IOException io) {
                throw new ApiException(io.getMessage(), io);

            }
        }
    }

    @Override
    public String writeSingle(ApiObject obj) throws ApiException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
