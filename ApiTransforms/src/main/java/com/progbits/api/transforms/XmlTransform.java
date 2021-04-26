package com.progbits.api.transforms;

import com.progbits.api.ObjectParser;
import com.progbits.api.ObjectWriter;
import com.progbits.api.ParserService;
import com.progbits.api.WriterService;
import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiClasses;
import com.progbits.api.model.ApiObject;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated Please use Transform instead
 * @author scarr
 */
public class XmlTransform {

	public static String convertToXml(WriterService service,
			  ApiClasses apiClasses,
			  ApiObject apiObj) throws ApiException {
		StringWriter out = new StringWriter();

		ObjectWriter writer = service.getWriter("XML").getWriter();

		writer.init(apiClasses, null, out);

		writer.write(apiObj);

		return out.toString();
	}

	public static String convertToXml(WriterService service,
			  ApiClasses apiClasses,
			  ApiObject apiObj, String nameSpace) throws ApiException, ApiClassNotFoundException {
		StringWriter out = new StringWriter();

		ObjectWriter writer = service.getWriter("XML").getWriter();

		Map<String, String> props = new HashMap<>();

		props.put("NameSpace", nameSpace);

		writer.init(apiClasses, props, out);

		writer.write(apiObj);

		return out.toString();
	}

	public static ApiObject xmlToApiObject(ParserService service,
			  ApiClasses apiClasses, String initialClass, String xmlString) throws ApiException, ApiClassNotFoundException {
		StringReader reader = new StringReader(xmlString);

		ObjectParser parser = service.getParser("XML").getParser();

		parser.init(apiClasses, initialClass, null, reader);

		if (parser.next()) {
			return parser.getObject();
		} else {
			throw new ApiException("Object was not found", null);
		}
	}

	/**
	 * Locate Body of Soap message and skip ahead to that location.
	 *
	 * @param reader Reader to Skip ahead
	 * @return First Tag After Body
	 * @throws IOException
	 */
	public static String skipToSoapFunction(Reader reader) throws IOException {
		String strFunc = null;
		StringBuilder curStream = new StringBuilder();
		StringBuilder curTag = new StringBuilder();
		char[] cb = new char[1];
		boolean bSoapBody = false;
		int iCnt = 0;
		boolean bStart = false;

		while (reader.read(cb) > -1) {
			if (!bSoapBody) {
				iCnt++;
			}

			curStream.append(cb);

			if (cb[0] == '>') {
				if (bSoapBody) {
					strFunc = curTag.toString();

					int iColonLoc = strFunc.indexOf(":");

					if (iColonLoc > -1) {
						strFunc = strFunc.substring(iColonLoc + 1);
					}

					break;
				} else if (curTag.toString().contains("Body")) {
					bSoapBody = true;
				}

				curTag = new StringBuilder();
			} else if (cb[0] == '<') {
				bStart = true;
			} else if (cb[0] == ' ') {
				if (bStart) {
					bStart = false;
				}
			} else {
				if (bStart) {
					curTag.append(cb);
				}
			}
		}

//        reader.reset();
//        reader.skip(iCnt);
//        System.out.println(strSoap);
//        System.out.println("Found: " + iCnt);
		return strFunc;
	}
}
