/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.TestUtil;

public class EncounterFilterTest extends BaseModuleContextSensitiveTest {
	
	@BeforeClass
	public void beforeClass() throws Exception {
		//TODO Call activator.willStart() to add the annotations
	}
	
	@Before
	public void before() throws Exception {
		//TODO add test data
	}
	
	@Test
	public void getEncounters_shouldReturnEncountersBelongingToPatientsAccessibleToTheUser() throws Exception {
		TestUtil.printOutTableContents(getConnection(), DataFilterConstants.MODULE_ID + "_user_object_map");
	}
	
}
