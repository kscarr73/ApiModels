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
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author scarr
 */
@Component(name = "FixedWidthParser",
        immediate = true,
        property = {
           "type=FIXED", "name=FixedWidthParser"
        }
)
public class FixedWidthParser implements ObjectParser {

   private ApiObject _obj = null;

   private String _mainClass;
   private ApiClasses _classes;
   private BufferedReader br = null;
   private Map<String, String> _props;

   private boolean m_segmentedFormat = false;
   private String m_lastObject = null;
   private int m_headerLength = 0;
   private Map<String, ApiObject> m_headerFields = new HashMap<>();

   private int m_lineCount = 0;
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
      if (in instanceof BufferedReader) {
         br = (BufferedReader) in;
      } else {
         br = new BufferedReader(in);
      }

      _props = properties;
      _classes = classes;
      _mainClass = mainClass;
      this.parseErrors = new ArrayList<>();

      if (_classes != null && _mainClass != null) {
         ApiClass tstClass = _classes.getClass(mainClass);

         for (ApiObject flds : tstClass.getList("fields")) {
            switch (flds.getString("type")) {
               case "Object":
               case "ArrayList":
                  m_segmentedFormat = true;
                  m_lastObject = flds.getString("name");

                  m_headerFields.put(m_lastObject, flds);

                  if (m_lastObject.length() > m_headerLength) {
                     m_headerLength = m_lastObject.length();
                  }

                  break;
            }
         }
      }
   }

   @Override
   public boolean next() throws ApiException {
      boolean bNotEnd = false;
      this.parseErrors.clear();
      this.throwException = null;

      try {
         String strLine = br.readLine();
         m_lineCount++;

         if (strLine != null) {
            if (m_segmentedFormat) {
               ApiObject retObj;

               if (_obj != null) {
                  //retObj = _obj.cloneApiObject();
                  retObj = _obj;
               } else {
                  retObj = _classes.getInstance(_mainClass);
               }

               boolean bLineContinue = true;

               while (bLineContinue) {
                  if (strLine != null) {
                     String headerCode = strLine.substring(0, m_headerLength).
                             trim();

                     if (headerCode.equals(m_lastObject)) {
                        bLineContinue = false;
                     }

                     ApiObject lineFields = m_headerFields.get(headerCode);

                     if (lineFields != null) {
                        String fieldName = lineFields.getString("name");
                        String subType = lineFields.getString("subType");

                        retObj.put(fieldName, parseLine(m_headerLength,
                                strLine, subType));
                     }

                     if (bLineContinue) {
                        strLine = br.readLine();
                        m_lineCount++;
                     }

                     bNotEnd = true;
                  } else {
                     bLineContinue = false;
                     bNotEnd = false;
                  }
               }
               _obj = retObj;
            } else {
               _obj = parseLine(0, strLine, _mainClass);
               bNotEnd = true;
            }
         }
      } catch (Exception iex) {
         throw new ApiException(iex.getMessage(), iex);
      }

      return bNotEnd;
   }

   private ApiObject parseLine(int startPos, String strLine,
           String className) throws ApiException, ApiClassNotFoundException {
      int iCurLoc = startPos;

      ApiObject obj = _classes.getInstance(className);

      for (ApiObject fld : obj.getApiClass().getList("fields")) {
         Object oFldLength = fld.getCoreObject("length");

         int iFieldLength = 0;

         if (oFldLength instanceof Integer) {
            iFieldLength = (Integer) oFldLength;
         } else if (oFldLength instanceof Long) {
            iFieldLength = ((Long) oFldLength).intValue();
         }

         String fieldValue;
         boolean fieldFound = false;

         try {
            fieldValue = strLine.substring(iCurLoc,
                    iCurLoc + iFieldLength);
            fieldFound = true;
         } catch (StringIndexOutOfBoundsException ex) {
            fieldFound = false;
            fieldValue = null;
         }

         iCurLoc += iFieldLength;

         try {
            if (fieldFound) {
               switch (fld.getString("type")) {
                  case "String":
                     obj.getFields().put(fld.getString("name"),
                             TransformString.
                                     transformString(fieldValue, fld.
                                             getString("format")));

                     break;

                  case "Decimal":
                     fieldValue = fieldValue.trim();
                     if (fieldValue != null && !fieldValue.isEmpty()) {
                        obj.getFields().put(fld.getString("name"),
                                TransformDecimal.
                                        transformDecimal(fieldValue,
                                                fld.getString("format")));
                     } else {
                        // Check to see if field is Required
                        if (fld.isSet("min") && fld.getLong("min") > 0) {
                           throw new ApiException("Field: " + fld.getString(
                                   "name") + " Is Required", 
                                   null);
                        }
                     }

                     break;

                  case "Double":
                     fieldValue = fieldValue.trim();
                     if (fieldValue != null && !fieldValue.isEmpty()) {
                        obj.getFields().put(fld.getString("name"),
                                TransformDecimal.
                                        transformDouble(fieldValue, fld.
                                                getString("format")));
                     } else {
                        // Check to see if field is Required
                        if (fld.isSet("min") && fld.getLong("min") > 0) {
                           throw new ApiException("Field: " + fld.getString(
                                   "name") + " Is Required", 
                                   null);
                        }
                     }
                     break;

                  case "DateTime":
                     obj.getFields().put(fld.getString("name"),
                             TransformDate.
                                     transformDate(fieldValue, fld.
                                             getString("format")));
                     break;

                  case "Integer":
                     fieldValue = fieldValue.trim();
                     if (fieldValue != null && !fieldValue.isEmpty()) {
                        obj.getFields().put(fld.getString("name"),
                                TransformNumber.
                                        transformInteger(fieldValue,
                                                fld.getString("format")));
                     } else {
                        // Check to see if field is Required
                        if (fld.isSet("min") && fld.getLong("min") > 0) {
                           throw new ApiException("Field: " + fld.getString(
                                   "name") + " Is Required",
                                   null);
                        }
                     }
                     break;

                  case "Boolean":
                     fieldValue = fieldValue.trim();
                     if (fieldValue != null && !fieldValue.isEmpty()) {
                        if ("true".equalsIgnoreCase(fieldValue)) {
                           obj.getFields().put(fld.getString("name"),
                                   true);
                        } else {
                           obj.getFields().put(fld.getString("name"),
                                   false);
                        }
                     } else {
                        // Check to see if field is Required
                        if (fld.isSet("min") && fld.getLong("min") > 0) {
                           throw new ApiException("Field: " + fld.getString(
                                   "name") + " Is Required", 
                                   null);
                        }
                     }

                     break;

                  case "Long":
                     fieldValue = fieldValue.trim();
                     if (fieldValue != null && !fieldValue.isEmpty()) {
                        obj.getFields().put(fld.getString("name"),
                                TransformNumber.
                                        transformLong(fieldValue, fld.
                                                getString("format")));
                     } else {
                        // Check to see if field is Required
                        if (fld.isSet("min") && fld.getLong("min") > 0) {
                           throw new ApiException("Field: " + fld.getString(
                                   "name") + " Is Required",
                                   null);
                        }
                     }

                     break;
               }
            }
         } catch (Exception ex) {

            if (!this.parseErrors.contains("Line: " + m_lineCount + " Field: " + fld.getString("name") + " " + ex.
                    getMessage())) {
               this.parseErrors.add("Line: " + m_lineCount + " Field: " + fld.getString("name") + " " + ex.
                       getMessage());
            }
            this.throwException = ex;
         }
      }

      return obj;
   }

   @Override
   public ApiObject getObject() {
      return _obj;
   }

   @Override
   public ObjectParser getParser() {
      return new FixedWidthParser();
   }

   @Override
   public ApiObject parseSingle(Reader in) throws ApiException {
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
