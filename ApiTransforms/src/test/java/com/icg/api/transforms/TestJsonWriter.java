package com.icg.api.transforms;

import com.progbits.api.model.ApiObject;
import com.progbits.api.parser.JsonObjectParser;
import com.progbits.api.writer.JsonObjectWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class TestJsonWriter {

	JsonObjectParser parser = null;
	JsonObjectWriter writer = null;

	@BeforeClass
	public void setup() throws Exception {
		parser = new JsonObjectParser();
		parser.init(null, null, null, null);

		writer = new JsonObjectWriter();
		writer.init(null, null, null);
	}

	@Test
	public void testStringArray() throws Exception {
		String strContents = new String(
				  Files.readAllBytes(
							 Paths.get(getClass().getResource("/JsonWriterTest.json").toURI())
				  )
		);

		ApiObject objTest = parser.parseSingle(new StringReader(strContents));

		assert objTest != null;

		String strOutput = writer.writeSingle(objTest);

		assert strOutput != null;
	}
}
