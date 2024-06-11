package com.progbits.api.writer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.progbits.api.ObjectWriter;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.BufferedOutputStream;
import java.io.IOException;
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
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger log = LoggerFactory.getLogger(JsonObjectWriter.class);

	private JsonFactory _jf;
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

	public JsonObjectWriter() {
	}

	public JsonObjectWriter(boolean genericProcessor) {
		if (genericProcessor) {
			internalInit(null, null, null);
		}
	}

	private void internalInit(ApiClasses classes, Map<String, String> properties, Writer out) {
		_jf = new JsonFactory();

		if (out != null) {
			try {
				_out = _jf.createGenerator(out);
			} catch (IOException io) {
				log.error("internalInit", io);
			}
		}

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

		_jf = new JsonFactory();

		if (out != null) {
			try {
				_out = _jf.createGenerator(bout);
			} catch (IOException io) {
				throw new ApiException(io.getMessage(), io);
			}
		}

		_props = properties;
		_classes = classes;
	}

	@Override
	public void write(ApiObject obj) throws ApiException {
		convertObjectToJson(_out, obj, null);

		try {
			_out.flush();
		} catch (IOException io) {
			throw new ApiException(io.getMessage(), io);
		}

	}

	public void convertObjectToJson(JsonGenerator writeOut, ApiObject apiObj,
			String name) throws ApiException {
		try {
			if (name != null) {
				writeOut.writeFieldName(name);
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
						writeOut.writeStringField(fldKey, (String) fldValue);
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

								writeOut.writeFieldName(fldKey);
								writeOut.writeStartArray(fldKey);

								for (String objs : arrStrList) {
									writeOut.writeString(objs);
								}

								writeOut.writeEndArray();
								break;

							case "integerarray":
								List<Integer> arrIntList = (List<Integer>) fldValue;

								writeOut.writeFieldName(fldKey);
								writeOut.writeStartArray(fldKey);

								for (Integer objs : arrIntList) {
									writeOut.writeNumber(objs);
								}

								writeOut.writeEndArray();
								break;

							case "doublearray":
								List<Double> arrDblList = (List<Double>) fldValue;

								writeOut.writeFieldName(fldKey);
								writeOut.writeStartArray(fldKey);

								for (Double objs : arrDblList) {
									writeOut.writeNumber(objs);
								}

								writeOut.writeEndArray();
								break;

							default:
								List<ApiObject> arrList = (List<ApiObject>) fldValue;

								writeArrayList(writeOut, fldKey, arrList);

								break;
						}
					} else if (fldValue instanceof ApiObject) {
						ApiObject obj = (ApiObject) fldValue;
						try {
							convertObjectToJson(writeOut, obj, fldKey);
						} catch (ApiException app) {
							log.error("Internal Error", app);
						}
					} else if (fldValue instanceof Double) {
						writeOut.writeNumberField(fldKey, (Double) fldValue);
					} else if (fldValue instanceof BigDecimal) {
						writeOut.writeNumberField(fldKey, (BigDecimal) fldValue);
					} else if (fldValue instanceof Integer) {
						writeOut.writeNumberField(fldKey, (Integer) fldValue);
					} else if (fldValue instanceof Boolean) {
						writeOut.writeBooleanField(fldKey, (Boolean) fldValue);
					} else if (fldValue instanceof Long) {
						writeOut.writeNumberField(fldKey, (Long) fldValue);
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

						writeOut.writeStringField(fldKey, dtValue.format(_dtFormats.get(fldKey)));
					} else if (fldValue instanceof Boolean) {
						writeOut.writeBooleanField(fldKey, ((Boolean) fldValue));
					}
				} catch (Exception ex) {
					if (!this.writeErrors.contains(ex.getMessage())) {
						this.writeErrors.add(ex.getMessage());
					}
					this.throwException = ex;
				}
			});

			writeOut.writeEndObject();
		} catch (IOException io) {
			throw new ApiException(io.getMessage(), io);
		}
	}

	private void writeArrayList(JsonGenerator writeOut, String fldKey, List<ApiObject> arrList) {
		try {
			if (null != fldKey) {
				writeOut.writeFieldName(fldKey);
				writeOut.writeStartArray(fldKey);
			} else {
				writeOut.writeStartArray();
			}

			for (ApiObject objs : arrList) {
				try {
					convertObjectToJson(writeOut, objs, null);
				} catch (ApiException app) {
					log.error("writeArrayList", app);
				}
			}

			writeOut.writeEndArray();
		} catch (IOException io) {
			log.error("writeArrayList", io);
		}
	}

	@Override
	public String writeSingle(ApiObject obj) throws ApiException {
		StringWriter retStr = new StringWriter(10000);

		try {
			JsonGenerator jsonWrite = _jf.createGenerator(retStr);

			if (obj.size() == 1 && obj.containsKey("root")) {
				writeArrayList(jsonWrite, null, obj.getList("root"));
			} else {
				convertObjectToJson(jsonWrite, obj, null);
			}

			jsonWrite.flush();
		} catch (IOException io) {
			throw new ApiException(io.getMessage(), io);
		}
		
		return retStr.toString();
	}

	@Override
	public void writeHeader() throws ApiException {
		try {
			_out.writeStartArray();
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
