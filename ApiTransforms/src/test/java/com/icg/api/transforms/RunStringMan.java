/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.icg.api.transforms;

import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class RunStringMan {

	@Test
	public void testStringMan() {
		String strWsdlServiceTemplate = "<wsdl:service name=\"%1$s\">\n"
				+ "<wsdl:port binding=\"tns:%1$sSoapBinding\" name=\"%2$sPort\">\n"
				+ "<soap:address location=\"%3$s\"/>\n"
				+ "</wsdl:port>\n"
				+ "</wsdl:service>\n"
				+ "</wsdl:definitions>";

		String strTemp = String.format(strWsdlServiceTemplate, "funcName",
				"Port", "http://mytestmp");
		System.out.println(strTemp);
	}
}
