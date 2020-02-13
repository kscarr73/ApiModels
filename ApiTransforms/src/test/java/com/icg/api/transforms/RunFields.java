/*
 * Copyright: 2009-2017 Ingram Content Group. All rights reserved
 *
 */
package com.icg.api.transforms;

import com.progbits.api.formaters.TransformDate;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class RunFields {

	@Test
	public void testDate() {
		String strDate = TransformDate.formatDate(null, "yyyyMMdd|00000000");

	}
}
