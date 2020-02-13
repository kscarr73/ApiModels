package com.progbits.api.writer;

import com.progbits.api.ObjectWriter;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author scarr
 */
@Component(name = "JsonObjectWriter",
        immediate = true,
        property = {
           "type=JSON", "name=JsonObjectWriter"
        }
)
public class JsonObjectWriter implements ObjectWriter {

   private JsonGeneratorFactory _jf;
   private ApiClasses _classes;
   private JsonGenerator _out = null;
   private Map<String, String> _props = null;
   private List<String> writeErrors = new ArrayList<>();
   private Throwable throwException = null;

   private String mainClassName = null;

   private Map<String, DateTimeFormatter> _dtFormats = new HashMap<>();

   @Override
   public ObjectWriter getWriter() {
      return new JsonObjectWriter();
   }

   @Override
   public void init(ApiClasses classes, Map<String, String> properties,
           Writer out) throws ApiException {
      Map<String, Object> props = new HashMap<>(1);

      if (properties != null && properties.size() > 0) {
         if ("true".equalsIgnoreCase(properties.get("PrettyPrint"))) {
            props.put(JsonGenerator.PRETTY_PRINTING, true);
         }
      }

      _jf = Json.createGeneratorFactory(props);

      if (out != null) {
         _out = _jf.createGenerator(out);
      }

      _props = properties;
      _classes = classes;
   }

   @Override
   public void init(ApiClasses classes, String mainClassName, Map<String, String> properties,
           Writer out) throws ApiException {
      this.mainClassName = mainClassName;

      init(classes, properties, out);
   }

   @Override
   public void initStream(ApiClasses classes, String mainClassName, Map<String, String> properties,
           OutputStream out) throws ApiException {
      this.mainClassName = mainClassName;

      Map<String, Object> props = new HashMap<>(1);

      if (properties != null && properties.size() > 0) {
         if ("true".equalsIgnoreCase(properties.get("PrettyPrint"))) {
            props.put(JsonGenerator.PRETTY_PRINTING, true);
         }
      }

      BufferedOutputStream bout = null;

      if (out instanceof BufferedOutputStream) {
         bout = (BufferedOutputStream) out;
      } else {
         bout = new BufferedOutputStream(out);
      }

      _jf = Json.createGeneratorFactory(props);

      if (out != null) {
         _out = _jf.createGenerator(bout);
      }

      _props = properties;
      _classes = classes;
   }

   @Override
   public void write(ApiObject obj) throws ApiException {
      convertObjectToJson(_out, obj, null);

      _out.flush();
   }

   public void convertObjectToJson(JsonGenerator writeOut, ApiObject apiObj,
           String name) throws ApiException {
      if (name != null) {
         writeOut.writeStartObject(name);
      } else {
         writeOut.writeStartObject();
      }
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
               writeOut.write(fldKey, (String) fldValue);
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

                     writeOut.writeStartArray(fldKey);

                     for (String objs : arrStrList) {
                        writeOut.write(objs);
                     }

                     writeOut.writeEnd();
                     break;

                  case "integerarray":
                     List<Integer> arrIntList = (List<Integer>) fldValue;

                     writeOut.writeStartArray(fldKey);

                     for (Integer objs : arrIntList) {
                        writeOut.write(objs);
                     }

                     writeOut.writeEnd();
                     break;
                     
                  case "doublearray":
                     List<Double> arrDblList = (List<Double>) fldValue;

                     writeOut.writeStartArray(fldKey);

                     for (Double objs : arrDblList) {
                        writeOut.write(objs);
                     }

                     writeOut.writeEnd();
                     break;

                  default:
                     List<ApiObject> arrList = (List<ApiObject>) fldValue;

                     writeOut.writeStartArray(fldKey);

                     arrList.forEach((objs) -> {
                        try {
                           convertObjectToJson(writeOut, objs, null);
                        } catch (ApiException app) {

                        }
                     });

                     writeOut.writeEnd();
                     break;
               }
            } else if (fldValue instanceof ApiObject) {
               ApiObject obj = (ApiObject) fldValue;
               try {
                  convertObjectToJson(writeOut, obj, fldKey);
               } catch (ApiException app) {

               }
            } else if (fldValue instanceof Double) {
               writeOut.write(fldKey, (Double) fldValue);
            } else if (fldValue instanceof BigDecimal) {
               writeOut.write(fldKey, (BigDecimal) fldValue);
            } else if (fldValue instanceof Integer) {
               writeOut.write(fldKey, (Integer) fldValue);
            } else if (fldValue instanceof Boolean) {
               writeOut.write(fldKey, (Boolean) fldValue);
            } else if (fldValue instanceof Long) {
               writeOut.write(fldKey, (Long) fldValue);
            } else if (fldValue instanceof DateTime) {
               if (!_dtFormats.containsKey(fldKey)) {
                  if (format != null && !format.isEmpty()) {
                     DateTimeFormatter dtFormat = DateTimeFormat.forPattern(
                             format);

                     _dtFormats.put(fldKey, dtFormat);
                  } else {
                     _dtFormats.put(fldKey, ISODateTimeFormat.
                             dateTime());
                  }
               }

               writeOut.write(fldKey, ((DateTime) fldValue).
                       toString(
                               _dtFormats.get(fldKey)));
            } else if (fldValue instanceof Boolean) {
               writeOut.write(fldKey, String.valueOf((Boolean) fldValue));
            }
         } catch (Exception ex) {
            if (!this.writeErrors.contains(ex.getMessage())) {
               this.writeErrors.add(ex.getMessage());
            }
            this.throwException = ex;
         }
      });

      writeOut.writeEnd();
   }

   @Override
   public String writeSingle(ApiObject obj) throws ApiException {
      StringWriter retStr = new StringWriter(10000);
      JsonGenerator jsonWrite = _jf.createGenerator(retStr);

      convertObjectToJson(jsonWrite, obj, null);

      jsonWrite.flush();
      return retStr.toString();
   }

   @Override
   public void writeHeader() throws ApiException {
      _out.writeStartArray();
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
