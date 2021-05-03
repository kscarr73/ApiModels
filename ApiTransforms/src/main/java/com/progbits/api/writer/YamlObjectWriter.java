package com.progbits.api.writer;

import com.progbits.api.ObjectWriter;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
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
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.emitter.EmitterException;
import org.yaml.snakeyaml.events.CollectionStartEvent;
import org.yaml.snakeyaml.events.DocumentEndEvent;
import org.yaml.snakeyaml.events.DocumentStartEvent;
import org.yaml.snakeyaml.events.ImplicitTuple;
import org.yaml.snakeyaml.events.MappingEndEvent;
import org.yaml.snakeyaml.events.MappingStartEvent;
import org.yaml.snakeyaml.events.NodeEvent;
import org.yaml.snakeyaml.events.ScalarEvent;
import org.yaml.snakeyaml.events.SequenceEndEvent;
import org.yaml.snakeyaml.events.SequenceStartEvent;
import org.yaml.snakeyaml.events.StreamEndEvent;
import org.yaml.snakeyaml.events.StreamStartEvent;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.serializer.Serializer;

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

	private Yaml _factory = new Yaml();
	private ApiClasses _classes;
	private Emitter _writer = null;
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
		_writer = new Emitter(out, new DumperOptions());
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

		_writer = new Emitter(new OutputStreamWriter(bout), new DumperOptions());
		_props = properties;
		_classes = classes;
	}

	@Override
	public void write(ApiObject obj) throws ApiException {
		convertObjectToYaml(_writer, obj, null);
	}

	public void convertObjectToYaml(Emitter writeOut, ApiObject apiObj,
			String name) throws ApiException {
		try {
			if (name != null) {
				writeScalar(writeOut, "str", name);
				writeStartMapping(writeOut);
			} else {
				writeStartMapping(writeOut);
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
						writeScalar(writeOut, "str", (String) fldValue);
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

								writeStartArray(writeOut, false);

								for (String objs : arrStrList) {
									writeScalar(writeOut, "str", objs);
								}

								writeEndArray(writeOut);
								break;

							case "integerarray":
								List<Integer> arrIntList = (List<Integer>) fldValue;

								writeScalar(writeOut, "int", fldKey);
								writeStartArray(writeOut, false);

								for (Integer objs : arrIntList) {
									writeScalar(writeOut, "int", String.valueOf(objs));
								}

								writeEndArray(writeOut);
								break;

							case "doublearray":
								List<Double> arrDblList = (List<Double>) fldValue;

								writeScalar(writeOut, "int", fldKey);
								writeStartArray(writeOut, false);

								for (Double objs : arrDblList) {
									writeScalar(writeOut, "int", String.valueOf(objs));
								}

								writeEndArray(writeOut);
								break;

							default:
								List<ApiObject> arrList = (List<ApiObject>) fldValue;

								writeArrayList(writeOut, fldKey, arrList);

								break;
						}
					} else if (fldValue instanceof ApiObject) {
						ApiObject obj = (ApiObject) fldValue;
						try {
							convertObjectToYaml(writeOut, obj, fldKey);
						} catch (ApiException app) {
							log.error("Internal Error", app);
						}
					} else if (fldValue instanceof Double) {
						writeScalar(writeOut, "str", String.valueOf(fldValue));
					} else if (fldValue instanceof BigDecimal) {
						writeScalar(writeOut, "int", String.valueOf(fldValue));
					} else if (fldValue instanceof Integer) {
						writeScalar(writeOut, "int", String.valueOf(fldValue));
					} else if (fldValue instanceof Boolean) {
						writeScalar(writeOut, "bool", String.valueOf(fldValue));
					} else if (fldValue instanceof Long) {
						writeScalar(writeOut, "int", String.valueOf(fldValue));
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

						writeScalar(writeOut, "timestamp", dtValue.format(_dtFormats.get(fldKey)));
					} else if (fldValue instanceof Boolean) {
						writeScalar(writeOut, "timestamp", String.valueOf(fldValue));
					}
				} catch (Exception ex) {
					if (!this.writeErrors.contains(ex.getMessage())) {
						this.writeErrors.add(ex.getMessage());
					}
					this.throwException = ex;
				}
			});

			if (name != null) {
				writeEndMapping(writeOut);
			} else {
				writeEndMapping(writeOut);
			}
		} catch (EmitterException | IOException io) {
			throw new ApiException(io.getMessage(), io);
		}
	}

	private void writeScalar(Emitter writeOut, String type, String subject) throws IOException {
		writeOut.emit(new ScalarEvent(null, type, new ImplicitTuple(true, false), subject, null, null, DumperOptions.ScalarStyle.PLAIN));
	}

	private void writeStartDocument(Emitter writeOut) throws IOException {
		writeOut.emit(new DocumentStartEvent(null, null, true, DumperOptions.Version.V1_1, null));
	}
	
	private void writeEndDocument(Emitter writeOut) throws IOException {
		writeOut.emit(new DocumentEndEvent(null, null, true));
	}
	
	private void writeStartMapping(Emitter writeOut) throws IOException {
		writeOut.emit(new MappingStartEvent(null, null, true, null, null, DumperOptions.FlowStyle.FLOW));
	}
	
	private void writeEndMapping(Emitter writeOut) throws IOException {
		writeOut.emit(new MappingEndEvent(null, null));
	}
	
	private void writeStartArray(Emitter writeOut, Boolean blockStyle) throws IOException {
		writeOut.emit(new SequenceStartEvent(null, null, true, null, null, blockStyle ? DumperOptions.FlowStyle.BLOCK : DumperOptions.FlowStyle.FLOW));
	}
	
	private void writeEndArray(Emitter writeOut) throws IOException {
		writeOut.emit(new SequenceEndEvent(null, null));
	}

	private void writeStreamStart(Emitter writeOut) throws IOException {
		writeOut.emit(new StreamStartEvent(null, null));
	}

	private void writeStreamEnd(Emitter writeOut) throws IOException {
		writeOut.emit(new StreamEndEvent(null, null));
	}

	private void writeArrayList(Emitter writeOut, String fldKey, List<ApiObject> arrList) {
		try {
			if (null != fldKey) {
				writeScalar(writeOut, "str", fldKey);
				writeStartArray(writeOut, true);
			} else {
				writeStartArray(writeOut, true);
			}

			for (ApiObject objs : arrList) {
				try {
					convertObjectToYaml(writeOut, objs, null);
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
		StringWriter retStr = new StringWriter(10000);

		Emitter writeOut = new Emitter(retStr, new DumperOptions());

		try {
			writeStreamStart(writeOut);
			writeStartDocument(writeOut);

			if (obj.size() == 1 && obj.containsKey("root")) {
				writeArrayList(writeOut, null, obj.getList("root"));
			} else {
				convertObjectToYaml(writeOut, obj, null);
			}

			writeEndDocument(writeOut);
			writeStreamEnd(writeOut);
			retStr.flush();
		} catch (IOException io) {
			return retStr.toString();
			//throw new ApiException(io.getMessage());
		}

		return retStr.toString();
	}

	@Override
	public void writeHeader() throws ApiException {
		try {
			writeStartArray(_writer, true);
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
