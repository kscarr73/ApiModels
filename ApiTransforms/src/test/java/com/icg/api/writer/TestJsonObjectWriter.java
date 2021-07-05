package com.icg.api.writer;

import com.progbits.api.model.ApiObject;
import com.progbits.api.writer.JsonObjectWriter;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class TestJsonObjectWriter {
	@Test
	public void testJsonWriter() throws Exception {
		JsonObjectWriter writer = new JsonObjectWriter(true);
		
		ApiObject objTest = new ApiObject();
		
		objTest.setString("startField", "Testing");
		objTest.setInteger("intTest", 13);
		objTest.setDouble("doubleTest", 12.312);
		objTest.createList("listTest");
		objTest.getListAdd("listTest").setString("field1", "value1").setString("field2", "value2");
		
		objTest.createList("listTest2");
		objTest.getListAdd("listTest2").setString("field1", "value1").setString("field2", "value2");
		
		objTest.createObject("objectTest");
		objTest.getObject("objectTest").setString("objectString", "This");
		
		objTest.createStringArray("myStringArray");
		objTest.getStringArray("myStringArray").add("This");
		objTest.getStringArray("myStringArray").add("That");
		objTest.getStringArray("myStringArray").add("Other");
		
		objTest.createIntegerArray("myIntegerArray");
		objTest.getIntegerArray("myIntegerArray").add(42);
		objTest.getIntegerArray("myIntegerArray").add(13);
		objTest.getIntegerArray("myIntegerArray").add(7);
		
		String strJson = writer.writeSingle(objTest);
		
		assert strJson != null;
	}
}
