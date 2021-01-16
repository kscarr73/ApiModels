package com.progbits.api.export.mapping.python;

import com.progbits.api.model.ApiObject;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class ApiExportMappingPythonNGTest {
	
	@Test
	public void testSource() {
		ApiObject objTest = new ApiObject();
		objTest.setString("sourceClass", "com.progbits.api.Test");
		objTest.setString("targetClass", "com.progbits.com.api.Target");
		
		objTest.setString("mapScript", "target.MyName = source.OtherName\n"
			+ "target.OrderNumber = source.ID");
		
		ApiExportMappingPython mainObj = new ApiExportMappingPython();
		
		String strOutput = mainObj.performExport(objTest.getString("sourceClass"), objTest);
		
		System.out.println(strOutput);
	}
	
}
