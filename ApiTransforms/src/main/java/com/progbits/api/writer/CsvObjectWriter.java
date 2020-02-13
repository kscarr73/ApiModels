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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.joda.time.format.DateTimeFormatter;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author scarr
 */
@Component(name = "CsvObjectWriter",
        immediate = true,
        property = {
            "type=CSV", "name=CsvObjectWriter"
        }
)
/**
 * <p>
 * Writes CSV Files Using Apache Commons CSV</p>
 * <p>
 * Properties:</p>
 * <ul>
 * <li>delimiter - The delimiter to use for the file. Defaults to ,</li>
 * <li>escapeCharacter - The Escape Character to use. DEFAULTS to "</li>
 * <li>DisplayName - Use the Display Name instead of Field Name for Model.
 * Defaults to false</li>
 * <li>WithHeader - Write the Column Header for the CSV File</li>
 * </ul>
 */
public class CsvObjectWriter implements ObjectWriter {

    private ApiClasses _classes;
    private Writer _out = null;
    private String mainClassName;
    private CSVPrinter _print = null;
    private Map<String, String> _props = null;

    private char delimiter = ',';
    private char escapeChar = '"';
    private String nullString = "";
    private List<String> writeErrors = new ArrayList<>();
    private Throwable throwException = null;

    private Map<String, DateTimeFormatter> _dtFormats = new HashMap<>();

    private enum CONSTANTS {
        fields, name, type, format, header, DisplayName, WithHeader, escapeCharacter, Delimiter, nullString
    }

    private enum TYPES {
        String, Integer, Decimal, DateTime, Double, Long
    }
//
//   static <T> Consumer<T> HandleExceptionWapper(
//           HandleException<T, Exception> throwingConsumer) {
//
//      return i -> {
//         try {
//            throwingConsumer.accept(i);
//         } catch (Exception ex) {
//            throw new RuntimeException(ex);
//         }
//      };
//   }

    @Override
    public void init(ApiClasses classes, Map<String, String> properties,
            Writer out) throws ApiException {
        init(classes, null, properties, out);
    }

    @Override
    public void initStream(ApiClasses classes, String _mainClassName, Map<String, String> properties,
            OutputStream out) throws ApiException {
        this._props = properties;
        this._classes = classes;
        this._out = new OutputStreamWriter(out);
        this.mainClassName = _mainClassName;

        parseProps();
    }

    @Override
    public void init(ApiClasses classes, String _mainClassName, Map<String, String> properties,
            Writer out) throws ApiException {
        this._props = properties;
        this._classes = classes;
        this._out = out;
        this.mainClassName = _mainClassName;

        parseProps();
    }

    private void parseProps() {
        if (_props != null) {
            _props.forEach((key, value) -> {
                if (CONSTANTS.Delimiter.name().equals(key)) {
                    if ("tab".equalsIgnoreCase(value)) {
                        delimiter = "\t".charAt(0);
                    } else {
                        delimiter = value.charAt(0);
                    }
                }

                if (CONSTANTS.escapeCharacter.name().equals(key)) {
                    escapeChar = value.charAt(0);
                }

                if (CONSTANTS.nullString.name().equals(key)) {
                    nullString = value;
                }

            });
        }
    }

    @Override
    public void write(ApiObject obj) throws ApiException {
        try {
            if (this._print == null) {
                writeHeader();
            }
            List<String> values = new ArrayList<>();
            this.getWriteErrors().clear();
            this.throwException = null;

            obj.getApiClass().getList(CONSTANTS.fields.name()).forEach((field) -> {
                try {
                    TYPES type = TYPES.String;
                    try {
                        type = TYPES.valueOf(field.getString(CONSTANTS.type.name()));
                    } catch (Exception e) {
                        // use the String as default if no found on Eum.TYPES
                        type = TYPES.String;
                    }

                    switch (type) {
                        case String:
                            values.add(TransformString.formatString(obj.getCoreObject(field.getString(CONSTANTS.name.name())),
                                    field.getString(CONSTANTS.format.name())));
                            break;
                        case Integer:
                            values.add(TransformNumber.formatInteger(obj.getCoreObject(field.getString(CONSTANTS.name.name())),
                                    field.getString(CONSTANTS.format.name())));
                            break;
                        case Long:
                            values.add(TransformNumber.formatLong(obj.getCoreObject(field.getString(CONSTANTS.name.name())),
                                    field.getString(CONSTANTS.format.name())));
                            break;
                        case Double:
                        case Decimal:
                            values.add(TransformDecimal
                                    .formatDecimal(obj.getCoreObject(field.getString(CONSTANTS.name.name())),
                                            field.getString(CONSTANTS.format.name())));
                            break;
                        case DateTime:
                            values.add(TransformDate.formatDate(obj.getCoreObject(field.getString(CONSTANTS.name.name())),
                                    field.getString(CONSTANTS.format.name())));
                            break;
                        default:
                            values.add(TransformString.formatString(obj.getCoreObject(field.getString(CONSTANTS.name.name())),
                                    field.getString(CONSTANTS.format.name())));
                            break;
                    }
                } catch (Exception ae) {
                    if (!this.writeErrors.contains(ae.getMessage())) {
                        this.writeErrors.add(ae.getMessage());
                    }
                    this.throwException = ae;
                }
            });
            this._print.printRecord(values);
        } catch (Exception iex) {
            throw new ApiException(iex.getMessage(), iex);
        }
    }

    @Override
    public void writeHeader() throws ApiException {
        CSVFormat format = CSVFormat.DEFAULT
                .withEscape(escapeChar)
                .withDelimiter(delimiter)
                .withNullString(nullString);

        List<String> fldNames = null;

        try {
            if (_props != null) {
                boolean bDisplayName = "true".equalsIgnoreCase(this._props.get(CONSTANTS.DisplayName.name()));

                if ("true".equalsIgnoreCase(this._props.get(CONSTANTS.WithHeader.name()))) {
                    ApiClass cls = this._classes.getClass(this.mainClassName);

                    fldNames = new ArrayList<>();

                    for (ApiObject fld : cls.getList(CONSTANTS.fields.name())) {
                        fldNames.add(fld.getString(bDisplayName ? CONSTANTS.DisplayName.name() : CONSTANTS.name.name()));
                    }

                    String[] tmpStrArray = fldNames.toArray(new String[0]);
                    format.withHeader(tmpStrArray);
                }
            }

            this._print = format.print(_out);

            if (fldNames != null) {
                this._print.printRecord(fldNames);
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), e);
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

    @Override
    public ObjectWriter getWriter() {
        return new CsvObjectWriter();
    }

    @Override
    public String writeSingle(ApiObject obj) throws ApiException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
