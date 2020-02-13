package com.progbits.api.parser;

import com.progbits.api.ObjectParser;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.formaters.TransformDate;
import com.progbits.api.formaters.TransformDecimal;
import com.progbits.api.formaters.TransformNumber;
import com.progbits.api.formaters.TransformString;
import com.progbits.api.model.ApiClass;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author scarr
 */
@Component(name = "CsvObjectParser",
        immediate = true,
        property = {
           "type=CSV", "name=CsvObjectParser"
        }
)
public class CsvObjectParser implements ObjectParser {

   private ApiObject _obj = null;

   private String _mainClass;
   private ApiClasses _classes;
   private BufferedReader br = null;
   private Map<String, String> _props;

   private CSVParser _parser = null;
   private Iterable<CSVRecord> _iteration;
   private Iterator<CSVRecord> _iterator;
   private List<String> parseErrors;
   private Throwable throwException;

   private Map<String, DateTimeFormatter> _dtFormats = new HashMap<>();

   private static enum CONSTANTS {
      IgnoreHeader, escapeCharacter, Delimiter
   };

   @Override
   public void initStream(ApiClasses classes, String mainClass,
           Map<String, String> properties, InputStream in) throws ApiException {
      init(classes, mainClass, properties, new BufferedReader(new InputStreamReader(in)));
   }

   @Override
   public void init(ApiClasses classes, String mainClass,
           Map<String, String> properties, Reader in) throws ApiException {
      if (in instanceof BufferedReader) {
         br = (BufferedReader) in;
      } else {
         br = new BufferedReader(in);
      }

      _props = properties;
      _classes = classes;
      _mainClass = mainClass;

      try {
         CSVFormat format = CSVFormat.DEFAULT;

         if (_props != null) {
            if (_props.containsKey(CONSTANTS.escapeCharacter.name())) {
               format = format.withEscape(_props.get(CONSTANTS.escapeCharacter.name()).charAt(0));
            }

            if (_props.containsKey(CONSTANTS.Delimiter.name())) {
               if ("tab".equalsIgnoreCase(_props.get(CONSTANTS.Delimiter.name()))) {
                  format = format.withDelimiter("\t".charAt(0));
               } else {
                  format = format.withDelimiter(_props.get(CONSTANTS.Delimiter.name()).charAt(0));
               }
               format = format.withDelimiter(_props.get(CONSTANTS.Delimiter.name()).charAt(0));
            }
         }

         ApiClass cls = _classes.getClass(mainClass);

         List<String> fldNames = new ArrayList<>();

         for (ApiObject fld : cls.getList("fields")) {
            fldNames.add(fld.getString("name"));
         }

         format.withHeader(fldNames.toArray(new String[]{}));

         _parser = format.parse(in);
         _iterator = _parser.iterator();

         if ("true".equalsIgnoreCase(_props.get(CONSTANTS.IgnoreHeader.name()))) {
            // Skip over first row, due to it being headers.
            if (_iterator.hasNext()) {
               _iterator.next();
            }
         }
         this.parseErrors = new ArrayList<>();

      } catch (Exception io) {
         throw new ApiException("Error Setting up Parsing", io);
      }
   }

   @Override
   public boolean next() throws ApiException, ApiClassNotFoundException {

      this.parseErrors.clear();
      this.throwException = null;

      if (_iterator.hasNext()) {
         CSVRecord record = _iterator.next();

         _obj = _classes.getInstance(_mainClass);

         int iCurrField = 0;

         for (ApiObject fld : _obj.getApiClass().getList("fields")) {
            String fieldValue = record.get(iCurrField);

            try {
               switch (fld.getString("type").toLowerCase()) {
                  case "string":
                     _obj.getFields().put(fld.getString("name"),
                             TransformString.
                                     transformString(fieldValue, fld.
                                             getString("format")));

                     break;

                  case "decimal":
                     _obj.getFields().put(fld.getString("name"),
                             TransformDecimal.
                                     transformDecimal(fieldValue, fld.
                                             getString("format")));
                     break;

                  case "double":
                     _obj.getFields().put(fld.getString("name"),
                             TransformDecimal.
                                     transformDouble(fieldValue, fld.
                                             getString("format")));
                     break;

                  case "datetime":
                     _obj.getFields().put(fld.getString("name"),
                             TransformDate.
                                     transformDate(fieldValue, fld.getString(
                                             "format")));
                     break;

                  case "integer":
                     _obj.getFields().put(fld.getString("name"),
                             TransformNumber.
                                     transformInteger(fieldValue, fld.
                                             getString("format")));
                     break;

                  case "boolean":
                     if ("true".equalsIgnoreCase(fieldValue)) {
                        _obj.getFields().put(fld.getString("name"), true);
                     } else {
                        _obj.getFields().put(fld.getString("name"), false);
                     }

                     break;

                  case "long":
                     _obj.getFields().put(fld.getString("name"),
                             TransformNumber.
                                     transformLong(fieldValue, fld.getString(
                                             "format")));

                     break;
               }

               iCurrField++;
            } catch (Exception ex) {
               if (!this.parseErrors.contains("Field: " + fld.getString("name") + " " + ex.getMessage())) {
                  this.parseErrors.add("Field: " + fld.getString("name") + " " + ex.getMessage());
               }
               this.throwException = ex;
            }
         }
         return true;
      } else {
         return false;
      }

   }

   @Override
   public ApiObject getObject() {
      return _obj;
   }

   @Override
   public ObjectParser getParser() {
      return new CsvObjectParser();
   }

   @Override
   public ApiObject parseSingle(Reader in) throws ApiException, ApiClassNotFoundException {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
