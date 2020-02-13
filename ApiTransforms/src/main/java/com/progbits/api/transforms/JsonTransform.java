package com.progbits.api.transforms;

import com.progbits.api.ObjectParser;
import com.progbits.api.ObjectWriter;
import com.progbits.api.ParserService;
import com.progbits.api.WriterService;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Convert an API Object to and from JSON
 *
 * @deprecated Please use Transform instead
 * @author scarr
 */
public class JsonTransform {

	/**
	 *
	 * @param service Writer object from WriterService
	 * @param apiClasses
	 * @param apiObj
	 * @return
	 */
	public static String convertToJson(WriterService service,
			  ApiClasses apiClasses, ApiObject apiObj) throws ApiException {
		StringWriter out = new StringWriter();

		ObjectWriter writer = service.getWriter("JSON").getWriter();

		writer.init(apiClasses, null, out);

		writer.write(apiObj);

		out.flush();

		return out.toString();
	}

	/**
	 *
	 * @param service Parser object from ParserService
	 * @param apiClasses
	 * @param initialClass
	 * @param jsonString
	 * @return
	 */
	public static ApiObject jsonToApiObject(ParserService service,
			  ApiClasses apiClasses, String initialClass, String jsonString) throws ApiException, ApiClassNotFoundException {
		StringReader reader = new StringReader(jsonString);

		ObjectParser parser = service.getParser("JSON").getParser();

		parser.init(apiClasses, initialClass, null, reader);

		if (parser.next()) {
			return parser.getObject();
		} else {
			throw new ApiException("Object was not found", null);
		}
	}

}
