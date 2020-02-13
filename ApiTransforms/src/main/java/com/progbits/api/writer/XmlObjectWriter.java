
package com.progbits.api.writer;

import com.progbits.api.ObjectWriter;
import com.progbits.api.exception.ApiException;
import com.progbits.api.formaters.TransformDate;
import com.progbits.api.formaters.TransformDecimal;
import com.progbits.api.formaters.TransformNumber;
import com.progbits.api.formaters.TransformString;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javolution.xml.stream.XMLOutputFactory;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamWriter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author scarr
 */
@Component(name = "XmlObjectWriter",
        immediate = true,
        property = {
            "type=XML", "name=XmlObjectWriter"
        }
)
public class XmlObjectWriter implements ObjectWriter {

    private ApiClasses _classes;
    private XMLOutputFactory _fact;
    private XMLStreamWriter _xml;
    private Map<String, String> _props;
    private List<String> writeErrors = new ArrayList<>();
    private Throwable throwException = null;
    private String mainClass = null;
    private Map<String, DateTimeFormatter> _dtFormats = new HashMap<>();
    private static String XMLSTARTELEMENTNAME = "XMLStartElement";

    Boolean _firstElement = true;

    private enum TYPES {
        String, Object, ArrayList, Decimal, Double, Integer, Long, Date, DateTime, Boolean, StringArray, IntegerArray
    }

    @Override
    public ObjectWriter getWriter() {
        XmlObjectWriter xml = new XmlObjectWriter();
        xml.setFactory(_fact);

        return xml;
    }

    @Reference
    public void setFactory(XMLOutputFactory factory) {
        this._fact = factory;
    }

    @Override
    public void init(ApiClasses classes, Map<String, String> properties,
            Writer out) throws ApiException {
        try {
            if (out != null) {
                _xml = _fact.createXMLStreamWriter(out);
            }

            _classes = classes;
            _props = properties;
        } catch (Exception ex) {
            throw new ApiException("Init XML Error", ex);
        }
    }

    @Override
    public void init(ApiClasses classes, String mainClassName, Map<String, String> properties,
            Writer out) throws ApiException {
        this.mainClass = mainClassName;

        init(classes, properties, out);
    }

    @Override
    public void initStream(ApiClasses classes, String mainClassName, Map<String, String> properties, OutputStream out) throws ApiException {
        try {
            this.mainClass = mainClassName;

            if (out != null) {
                BufferedOutputStream bout = null;

                if (out instanceof BufferedOutputStream) {
                    bout = (BufferedOutputStream) out;
                } else {
                    bout = new BufferedOutputStream(out);
                }

                _xml = _fact.createXMLStreamWriter(bout);
            }

            _classes = classes;
            _props = properties;
        } catch (Exception ex) {
            throw new ApiException("Init XML Error", ex);
        }
    }

    @Override
    public void write(ApiObject obj) throws ApiException {
        convertObjectToXml(_xml, _classes, obj, obj.getName(), _firstElement);

        try {
            _xml.flush();
        } catch (XMLStreamException iex) {
            throw new ApiException("Flush Command Failed", iex);
        }
    }

    public void convertObjectToXml(XMLStreamWriter writeOut,
            ApiClasses apiClasses, ApiObject apiObj,
            String name, boolean firstElement) throws ApiException {
        this.getWriteErrors().clear();
        this.throwException = null;

        try {
            if (name != null) {
                if (firstElement) {

                    if (_props != null && _props.containsKey("NameSpace") && _props.
                            get("NameSpace") != null) {
                        writeOut.writeStartElement("ns2:" + name);
                        writeOut.writeAttribute("xmlns:ns2", _props.get(
                                "NameSpace"));
                    } else {
                        writeOut.writeStartElement(name);
                    }

                    firstElement = false;
                } else {
                    writeOut.writeStartElement(name);
                }

                if (apiObj.getApiClass() != null) {
                    for (ApiObject fld : apiObj.getApiClass().getList("fields")) {
                        Boolean bAttr = fld.getBoolean("attribute");

                        if (bAttr != null && bAttr) {
                            writeOut.writeAttribute(
                                    fld.getString("name"),
                                    writeObject(fld, fld.getString("name"), null,
                                            apiObj.getCoreObject(fld.getString(
                                                    "name"))
                                    )
                            );
                        }
                    }
                }
            }

            for (Map.Entry<String, Object> flds : apiObj.getFields().entrySet()) {
                ApiObject fldDef = null;
                String format = null;

                try {
                    if (apiObj.getApiClass() != null) {
                        fldDef = apiObj.getApiClass().
                                getListSearch("fields", "name", flds.
                                        getKey());

                        if (fldDef != null) {
                            format = fldDef.getString("format");
                        }
                    }

                    if (flds.getValue() instanceof ArrayList) {
                        ArrayList arrList = (ArrayList) flds.getValue();

                        if (arrList.size() > 0) {
                            Object objTest = arrList.get(0);

                            if (objTest instanceof ApiObject) {
                                for (Object objs : arrList) {
                                    convertObjectToXml(writeOut, apiClasses,
                                            (ApiObject) objs,
                                            flds.getKey(), false);
                                }
                            } else if (objTest instanceof Integer) {
                                // TODO:  Fix issue with
                                for (Object objs : arrList) {
                                    writeOut.writeStartElement(flds.getKey());
                                    writeOut.writeCharacters(writeObject(fldDef,
                                            flds.
                                                    getKey(), format,
                                            objs));
                                    writeOut.writeEndElement();
                                }
                            } else if (objTest instanceof String) {
                                // TODO: Fix issue with no value
                                for (Object objs : arrList) {
                                    writeOut.writeStartElement(flds.getKey());
                                    writeOut.writeCharacters(writeObject(fldDef,
                                            flds.
                                                    getKey(), format,
                                            objs));

                                    writeOut.writeEndElement();
                                }
                            }
                        }
                        // writeOut.writeStartElement(flds.getKey());

                        // writeOut.writeEndElement();
                    } else if (flds.getValue() instanceof ApiObject) {
                        ApiObject obj = (ApiObject) flds.getValue();
                        // writeOut.writeStartElement(flds.getKey());

                        convertObjectToXml(writeOut, apiClasses, obj, flds.getKey(),
                                false);

                        // writeOut.writeEndElement();
                    } else if (flds.getValue() != null) {
                        writeOut.writeStartElement(flds.getKey());

                        writeOut.writeCharacters(writeObject(fldDef, flds.getKey(),
                                format, flds.
                                        getValue()));

                        writeOut.writeEndElement();
                    }
                } catch (Exception ex) {
                    if (!this.writeErrors.contains(ex.getMessage())) {
                        this.writeErrors.add(ex.getMessage());
                    }
                    this.throwException = ex;
                }
            }

            if (name != null) {
                writeOut.writeEndElement();
            }
        } catch (XMLStreamException xmlex) {
            throw new ApiException("convertObjectToXml. " + xmlex.getMessage(), xmlex);
        }
    }

    private String writeObject(ApiObject fld, String name, String format,
            Object value) throws ApiException {
        String type = null;
        try {
            if (fld == null || fld.getString("type") == null) {
                // If type is null, then lets try to infer the type
                if (value instanceof String) {
                    type = TYPES.String.name();
                } else if (value instanceof Double) {
                    type = TYPES.Double.name();
                } else if (value instanceof BigDecimal) {
                    type = TYPES.Decimal.name();
                } else if (value instanceof Integer) {
                    type = TYPES.Integer.name();
                } else if (value instanceof Boolean) {
                    type = TYPES.Boolean.name();
                } else if (value instanceof OffsetDateTime) {
                    type = TYPES.DateTime.name();
                } else if (value instanceof Long) {
                    type = TYPES.Long.name();
                }
            } else {
                type = fld.getString("type");
            }

            if (type != null) {
                TYPES fieldType = TYPES.valueOf(type);

                switch (fieldType) {
                    case String:
                    case StringArray:
//                  if (fld.containsKey("length")) {
//                     value = IsgStringUtils.forceRightSize(TransformString.formatString(value, format),
//                             " ", fld.getLong("length").intValue());
//                  } else {
//                     value = TransformString.formatString(value, format);
//                  }
                        return (String) TransformString.formatString(value, format);

                    case Double:
                    case Decimal:
                        return (String) TransformDecimal.formatDecimal(value, format);
                    case Boolean:
                        if (value instanceof String) {
                            return (String) value;
                        } else if (value instanceof Boolean) {
                            return String.valueOf((Boolean) value);
                        } else if (value instanceof Integer) {
                            return ((Integer) value) != 0 ? "true" : "false";
                        }
                    case Integer:
                    case IntegerArray:
                        return String.valueOf(TransformNumber.formatInteger(value, format));

                    case Long:
                        return String.valueOf(TransformNumber.formatLong(value, format));

                    case Date:
                    case DateTime:
//                  if (!_dtFormats.containsKey(name)) {
//                     if (format != null && !format.isEmpty()) {
//                        DateTimeFormatter dtFormat = DateTimeFormat.
//                                forPattern(format);
//
//                        _dtFormats.put(name, dtFormat);
//                     } else {
//                        _dtFormats.put(name, ISODateTimeFormat.dateTime());
//                     }
//                  }
//
//                  if (value instanceof DateTime) {
//                     return ((DateTime) value).toString(_dtFormats.get(name));
//                  } else if (value instanceof String) {
//                     DateTime dtTemp = ISODateTimeFormat
//                             .dateTimeParser()
//                             .parseDateTime((String) value);
//
//                     return dtTemp.toString(_dtFormats.get(name));
//                  }
                        return TransformDate.formatDate(value, format);
                }
            } else {
                if (value == null) {
                    return "";
                } else {
                    return String.valueOf(value);
                }
            }
        } catch (Exception ex) {
            throw new ApiException(ex.getMessage(), ex);//Logger.getLogger(XmlObjectWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    @Override
    public String writeSingle(ApiObject obj) throws ApiException {
        StringWriter retStr = new StringWriter(10000);
        XMLStreamWriter writeOut;

        try {
            writeOut = _fact.createXMLStreamWriter(retStr);
        } catch (XMLStreamException ex) {
            throw new ApiException("writeSingle Exception",
                    ex);
        }

        Boolean firstElement = true;
        convertObjectToXml(writeOut, _classes, obj, obj.getName(), firstElement);

        try {
            writeOut.flush();
        } catch (XMLStreamException ex) {

        }
        return retStr.toString();
    }

    @Override
    public void writeHeader() throws ApiException {
        if (mainClass != null) {
            try {
                String startElementName = this._props.containsKey(XMLSTARTELEMENTNAME) ? this._props
                        .get(XMLSTARTELEMENTNAME) : _classes
                        .getClass(mainClass).getName();
                _xml.writeStartElement(startElementName);
            } catch (Exception ex) {

            }
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
