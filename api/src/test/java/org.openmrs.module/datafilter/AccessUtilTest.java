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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class AccessUtilTest extends BaseModuleContextSensitiveTest {
	
	@Before
	public void beforeTestMethod() {
		executeDataSet(TestConstants.MODULE_TEST_DATASET_XML);
	}
	
	@Test
	public void getAccessiblePatientIds_shouldReturnAListOfAccessiblePatientIdsForTheAuthenticatedUser() {
		List<Integer> patientIds = AccessUtil.getAccessiblePatientIds(Location.class);
		assertEquals(2, patientIds.size());
		Assert.assertTrue(patientIds.contains(4000));
		Assert.assertTrue(patientIds.contains(1));
	}
	
}
