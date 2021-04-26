package com.progbits.api.parser;

import com.fasterxml.aalto.stax.InputFactoryImpl;
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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author scarr
 */
@Component(name = "XmlObjectParser", immediate = true, property
        = {
           "type=XML", "name=XmlObjectParser"
        })
public class XmlObjectParser implements ObjectParser {

   private ApiObject _obj;
   private InputFactoryImpl _factory = new InputFactoryImpl();
   private XMLStreamReader _in;
   private String _mainClass;
   private Map<String, String> _props;
   private ApiClasses _classes;
   private List<String> parseErrors;
   private Throwable throwException;

   private Map<String, DateTimeFormatter> _dtFormats = new HashMap<>();

   @Override
   public ObjectParser getParser() {
      XmlObjectParser xml = new XmlObjectParser();

      return xml;
   }

   @Override
   public void initStream(ApiClasses classes, String mainClass,
           Map<String, String> properties, InputStream in) throws ApiException {
      init(classes, mainClass, properties, new BufferedReader(new InputStreamReader(in)));
   }

   @Override
   public void init(ApiClasses classes, String mainClass,
           Map<String, String> properties, Reader in) throws ApiException {
      try {
         _classes = classes;

         if (in != null) {
            //_factory.setProperty("isNamespaceAware", false);
            _in = _factory.createXMLStreamReader(in);
         }

         _mainClass = mainClass;
         _props = properties;
         parseErrors = new ArrayList<>();
      } catch (Exception ex) {
         throw new ApiException("init", ex);
      }
   }

   @Override
   public boolean next() throws ApiException, ApiClassNotFoundException {
      _obj = _classes.getInstance(_mainClass);
      this.parseErrors.clear();
      this.throwException = null;

      try {
         if (_obj != null) {
            parseXmlToObject(_classes, _obj.getString("name"), _mainClass, _obj,
                    true, _in);
         } else {
            throw new ApiException(
                    "Main Class is Not Resolving from Classes: " + _mainClass, null);
         }
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

   private void parseXmlToObject(ApiClasses apiClasses, String keyName,
           String curClass, ApiObject obj, boolean bFirst,
           XMLStreamReader xmlRead) throws ApiException {
      boolean iFirstObj = bFirst;

      ApiClass thisClass = obj.getApiClass();

      if (thisClass == null) {
         throw new ApiException("Class Not Setup on Object", null);
      }

      ApiObject nObj = null;
      String anyField = null;

      try {
         OUTER:
         while (xmlRead.hasNext()) {
            int iCurEvent = xmlRead.next();

            switch (iCurEvent) {
               case XMLStreamReader.START_ELEMENT:
                  if (iFirstObj) {
                     iFirstObj = false;
                  } else if (anyField != null) {
                     String key = xmlRead.getLocalName().toString();

                     if (_classes != null) {
                        ApiClass clsSub = null;

                        try {
                           clsSub = _classes.getClassByName(key);
                        } catch (ApiClassNotFoundException aex) {
                           // We don't care if it doesn't exist
                        }

                        if (clsSub != null) {
                           ApiObject objNew = clsSub.createInstance();

                           parseXmlToObject(apiClasses, clsSub.getName(),
                                   clsSub.getString(
                                           "className"), objNew, false,
                                   xmlRead);

                           obj.setObject(anyField, objNew);
                        } else {
                           parseToEndTag(key, xmlRead);
                        }
                     }
                  } else {
                     String key = xmlRead.getLocalName().toString();

                     if (key.equals(keyName)) {
                        // We are currently in the element for the called object
                        // Pull attributes if any exist
                        for (ApiObject fld : thisClass.getList("fields")) {
                           Boolean bAttr = fld.getBoolean("attribute");

                           if (bAttr != null && bAttr) {
                              String attrValue = xmlRead.
                                      getAttributeValue(
                                              null, fld.
                                                      getString("name"));

                              if (attrValue != null) {
                                 populateObject(fld.getString("type"),
                                         fld, obj, fld.
                                                 getString("name"),
                                         attrValue);
                              }
                           }
                        }
                     } else {
                        ApiObject fld = thisClass.getListSearch("fields",
                                "name", key);

                        if (fld != null) {
                           try {
                              switch (fld.getString("type").toLowerCase()) {
                                 case "arraylist":
                                    // Need to handle ArrayList better
                                    List<ApiObject> arrList = new ArrayList<>();
                                    if (obj.getList(key) == null) {
                                       obj.createList(key);
                                    }

                                    ApiObject nArrObj = apiClasses.
                                            getInstance(fld.getString(
                                                    "subType"));

                                    if (nArrObj != null) {
                                       parseXmlToObject(apiClasses, key,
                                               curClass, nArrObj,
                                               false, xmlRead);

                                       obj.getList(key).add(nArrObj);
                                    }

                                    break;
                                 case "object":
                                    ApiObject apiObject = apiClasses.
                                            getInstance(fld.
                                                    getString("subType"));
                                    parseXmlToObject(apiClasses, key,
                                            fld.getString("subType"),
                                            apiObject, false, xmlRead);
                                    obj.setObject(key, apiObject);
                                    break;
                                 case "any":
                                    anyField = key;
                                    break;
                                 default:
                                    // Pull Standard Types
                                    populateObject(fld.getString("type"),
                                            fld, obj, key, xmlRead.
                                                    getElementText());
                                    break;
                              }
                           } catch (Exception ex) {
                              throw new ApiException(
                                      "Key: " + key + " Message: " + ex.
                                              getMessage(), null);
                           }
                        }
                     }
                  }
                  break;
               case XMLStreamReader.CHARACTERS:
                  break;
               case XMLStreamReader.END_ELEMENT:
                  String key = xmlRead.getLocalName().toString();
                  if (obj != null && key.equals(keyName)) {
                     break OUTER;
                  }
                  if (anyField != null && key.equals(anyField)) {
                     anyField = null;
                  }
                  break;
               default:
                  break;
            }
         }
      } catch (Exception ex) {
         if (!this.parseErrors.contains(ex.getMessage())) {
            this.parseErrors.add(ex.getMessage());
         }
         this.throwException = ex;
         throw new ApiException("Parsing XML Error", ex);
      }
   }

   private void parseToEndTag(String endTag, XMLStreamReader xmlRead) throws ApiException {
      try {
         while (xmlRead.hasNext()) {
            int iCurEvent = xmlRead.next();

            if (iCurEvent == XMLStreamReader.END_ELEMENT) {
               String key = xmlRead.getLocalName().toString();

               if (key.equals(endTag)) {
                  break;
               }
            }
         }
      } catch (Exception ex) {
         throw new ApiException("Parsing XML Error", ex);
      }
   }

   private void populateObject(String type, ApiObject fld, ApiObject obj,
           String name, String value) throws ApiException {
      if (type != null) {
         switch (type.toLowerCase()) {
            case "string":
               obj.setString(name, value);
               break;
            case "stringarray":
               if (obj.isNull(name)) {
                  obj.createStringArray(name);
               }

               obj.getStringArray(name).add(value);
               break;

            case "long":
               if (value.length() > 0) {
                  obj.setLong(name, Long.parseLong(value));
               }
               break;

            case "integer":
               if (value.length() > 0) {
                  obj.setInteger(name, Integer.parseInt(value));
               }
               break;

            case "integerarray":
               if (obj.isNull(name)) {
                  obj.createIntegerArray(name);
               }

               obj.getIntegerArray(name).add(Integer.valueOf(value));
               break;
            case "double":
               if (value.length() > 0) {
                  obj.setDouble(name, Double.parseDouble(value));
               }
               break;
            case "date":
            case "datetime":
               if (!_dtFormats.containsKey(name)) {
                  String format = fld.getString("format");

                  if (format != null && !format.isEmpty()) {
                     DateTimeFormatter dtFormat = DateTimeFormatter.
                             ofPattern(format);

                     _dtFormats.put(name, dtFormat);
                  } else {
                     _dtFormats.put(name, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                  }
               }

               if (value.length() > 0) {
                  obj.setDateTime(name, OffsetDateTime.parse(value, _dtFormats.get(name)));
               }

               break;
            case "boolean":
               if (value.length() == 0) {
                  obj.setBoolean(name, false);
               } else {
                  obj.setBoolean(name, Boolean.parseBoolean(value));
               }

               break;
         }
      } else {
         obj.setString(name, String.valueOf(value));
      }
   }

   @Override
   public ApiObject parseSingle(Reader in) throws ApiException, ApiClassNotFoundException {
      XMLStreamReader xmlRead = null;
      ApiObject retObj = _classes.getInstance(_mainClass);

      try {
         xmlRead = _factory.createXMLStreamReader(in);
      } catch (XMLStreamException xml) {
         throw new ApiException("Parse Single Exception", xml);
      }

      if (xmlRead != null) {
         if (retObj != null) {
            parseXmlToObject(_classes, retObj.getString("name"), _mainClass,
                    retObj,
                    true, xmlRead);
         } else {
            throw new ApiException(
                    "Main Class is Not Resolving from Classes: " + _mainClass, null);
         }
      }
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
