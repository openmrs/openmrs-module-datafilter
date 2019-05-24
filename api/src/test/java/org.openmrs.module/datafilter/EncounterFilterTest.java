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
import org.openmrs.api.EncounterService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class EncounterFilterTest extends BaseModuleContextSensitiveTest {
	
	private static DataFilterActivator activator = new DataFilterActivator();
	
	@Autowired
	private EncounterService encounterService;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		activator.willStart();
	}
	
	@Before
	public void before() throws Exception {
		executeDataSet(TestConstants.ROOT_DIR + "moduleTestData.xml");
	}
	
	@Test
	public void getEncounters_shouldReturnEncountersBelongingToPatientsAccessibleToTheUser() throws Exception {
		TestUtil.printOutTableContents(getConnection(), DataFilterConstants.MODULE_ID + "_role_object_map");
		//assertEquals(0, encounterService.getCountOfEncounters("Alex", false).intValue());
	}
	
}
