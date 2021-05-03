package com.icg.api.writer;

import com.progbits.api.model.ApiObject;
import com.progbits.api.writer.YamlObjectWriter;
import java.util.List;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class TestYamlWriter {
	@Test
	public void testWriteSingle() throws Exception {
		YamlObjectWriter objectWriter = new YamlObjectWriter(true);
		
		ApiObject objTest = new ApiObject();
		
		objTest.setString("startField", "Testing");
		objTest.setInteger("intTest", 13);
		objTest.setDouble("doubleTest", 12.312);
		objTest.createList("listTest");
		objTest.getListAdd("listTest").setString("field1", "value1").setString("field2", "value2");
		
//		objTest.createList("listTest2");
//		objTest.getListAdd("listTest2").setString("field1", "value1").setString("field2", "value2");
//		
//		objTest.createObject("objectTest");
//		objTest.getObject("objectTest").setString("objectString", "This");
//		
		String strTest = objectWriter.writeSingle(objTest);
		
		assert strTest != null;
	}
}
