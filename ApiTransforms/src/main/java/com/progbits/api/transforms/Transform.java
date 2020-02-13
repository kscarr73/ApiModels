package com.progbits.api.transforms;

import com.progbits.api.ObjectParser;
import com.progbits.api.ObjectWriter;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Generic Transform Functions
 *
 * @author scarr
 */
public class Transform {

	/**
	 * Convert ApiObject to String
	 *
	 * @param writer Should pass the type of Writer. From a WriterService, you
	 * can use _writer.getWriter("XML").getWriter()
	 * @param apiClasses Classes to match with the Transform
	 * @param apiObj ApiObject to Convert
	 * @return String representation of the Object
	 */
	public static String toString(ObjectWriter writer, ApiClasses apiClasses,
			  ApiObject apiObj) throws ApiException {
		StringWriter out = new StringWriter();

		writer.init(apiClasses, null, out);

		writer.write(apiObj);

		out.flush();

		return out.toString();
	}

	public static ApiObject toApiObject(ObjectParser parser,
			  ApiClasses apiClasses,
			  String initialClass, String subject) throws ApiException, ApiClassNotFoundException {
		StringReader reader = new StringReader(subject);

		parser.init(apiClasses, initialClass, null, reader);

		if (parser.next()) {
			return parser.getObject();
		} else {
			throw new ApiException("Object was not found", null);
		}
	}
}
