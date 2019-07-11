/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.User;
import org.openmrs.module.datafilter.BaseDataFilterTest;
import org.openmrs.module.datafilter.TestConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class DataFilterServiceTest extends BaseDataFilterTest {
	
	@Autowired
	private DataFilterService service;
	
	@Before
	public void beforeTestMethod() {
		executeDataSet(TestConstants.MODULE_TEST_DATASET_XML);
	}
	
	@Test
	public void hasAccess_shouldReturnFalseIfTheUserHasNoAccessToTheSpecifiedBasis() throws Exception {
		assertFalse(service.hasAccess(new User(501), new Location(1)));
		assertFalse(service.hasAccess(new User(3000), new Program(2)));
	}
	
	@Test
	public void hasAccess_shouldReturnTrueIfTheUserHasAccessToTheSpecifiedBasis() {
		assertTrue(service.hasAccess(new User(3000), new Location(1)));
		assertTrue(service.hasAccess(new User(3000), new Location(4000)));
		assertTrue(service.hasAccess(new User(3000), new Program(1)));
		assertTrue(service.hasAccess(new User(501), new Location(4000)));
	}
	
	@Test
	public void grantAccess_shouldGrantTheUserAccessToRecordsAtTheSpecifiedBasis() {
		throw new APIException("Fail");
	}
	
}
