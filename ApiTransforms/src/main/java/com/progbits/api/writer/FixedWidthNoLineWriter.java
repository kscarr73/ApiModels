package com.progbits.api.writer;

import com.progbits.api.ObjectWriter;
import com.progbits.api.exception.ApiException;
import com.progbits.api.formaters.TransformDate;
import com.progbits.api.formaters.TransformDecimal;
import com.progbits.api.formaters.TransformNumber;
import com.progbits.api.formaters.TransformString;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.format.DateTimeFormatter;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author scarr
 */
@Component(name = "FixedWidthNoLineObjectWriter",
        immediate = true,
        property = {
           "type=FIXEDNOLINE", "name=FixedWidthNoLineObjectWriter"
        }
)
public class FixedWidthNoLineWriter implements ObjectWriter {

   private String mainClassName = null;
   private BufferedWriter _write;
   private Map<String, String> _props = null;
   private ApiClasses _classes = null;
   private List<String> writeErrors = new ArrayList<>();
   private Throwable throwException = null;
   private Map<String, DateTimeFormatter> _dtFormats = new HashMap<>();

   @Override
   public void init(ApiClasses ac, Map<String, String> props, Writer writer) throws ApiException {
      _classes = ac;
      if (writer instanceof BufferedWriter) {
         this._write = (BufferedWriter) writer;
      } else {
         _write = new BufferedWriter(writer);
      }

      _props = props;
   }

   @Override
   public void init(ApiClasses classes, String _mainClassName, Map<String, String> properties,
           Writer out) throws ApiException {
      this._props = properties;
      this._classes = classes;

      if (out instanceof BufferedWriter) {
         this._write = (BufferedWriter) out;
      } else {
         _write = new BufferedWriter(out);
      }

      this.mainClassName = _mainClassName;
   }

   @Override
   public void initStream(ApiClasses classes, String _mainClassName, Map<String, String> properties,
           OutputStream out) throws ApiException {
      this._props = properties;
      this._classes = classes;
      this._write = new BufferedWriter(new OutputStreamWriter(out));
      this.mainClassName = _mainClassName;
   }

   @Override
   public void write(ApiObject ao) throws ApiException {
      this.getWriteErrors().clear();
      this.throwException = null;
      writeObject(ao);

      try {
         _write.flush();
      } catch (IOException ex) {
         throw new ApiException(ex.getMessage(), ex);
      }
   }

   public void writeObject(ApiObject ao) throws ApiException {
      for (ApiObject fld : ao.getApiClass().getList("fields")) {
         Object oFldLength = fld.getCoreObject("length");
         int iFieldLength = 0;

         if (oFldLength instanceof Integer) {
            iFieldLength = (Integer) oFldLength;
         } else if (oFldLength instanceof Long) {
            iFieldLength = ((Long) oFldLength).intValue();
         }

         Object fieldValue = ao.getCoreObject(fld.getString("name"));
         String fieldFormat = ao.getString("format");

         try {
            switch (fld.getString("type")) {
               case "String":
                  _write.append(TransformString.forceRightSize(
                          TransformString.
                                  formatString(fieldValue, fieldFormat),
                          " ", iFieldLength));
                  break;
               case "Decimal":
                  _write.append(TransformDecimal.
                          formatDecimal(fieldValue, fieldFormat));
                  break;
               case "Double":
                  _write.append(TransformDecimal.
                          formatDecimal(fieldValue, fieldFormat));
                  break;
               case "DateTime":
                  _write.append(TransformDate.formatDate(fieldValue,
                          fieldFormat));
                  break;
               case "Integer":
                  _write.
                          append(TransformNumber.
                                  formatInteger(fieldValue, fieldFormat));
                  break;
               case "Boolean":
                  if (ao.getBoolean(fld.getString("name"))) {
                     _write.append("1");
                  } else {
                     _write.append("0");
                  }
                  break;
               case "Long":
                  _write.append(TransformNumber.formatLong(fieldValue,
                          fieldFormat));
                  break;
               case "Object":
                  writeObject(ao.getObject(fld.getString("name")));
                  break;

               case "ArrayList":
                  for (ApiObject rowObj : ao.
                          getList(fld.getString("name"))) {
                     writeObject(rowObj);
                  }
                  break;
            }
         } catch (Exception ex) {
            String errMsg = "Field: " + fld.getString("name") + " " + ex.getMessage();
            if (!this.writeErrors.contains(errMsg)) {
               this.writeErrors.add(errMsg);
            }
            this.throwException = ex;
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

   @Override
   public ObjectWriter getWriter() {
      FixedWidthNoLineWriter fww = new FixedWidthNoLineWriter();

      return fww;
   }

   @Override
   public void writeHeader() throws ApiException {
   }

   @Override
   public String writeSingle(ApiObject obj) throws ApiException {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

}
