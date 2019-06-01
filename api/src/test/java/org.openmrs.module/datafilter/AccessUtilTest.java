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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class AccessUtilTest extends BaseModuleContextSensitiveTest {
	
	@Before
	public void beforeTestMethod() {
		executeDataSet(TestConstants.MODULE_TEST_DATASET_XML);
	}
	
	private void reloginAs(String username, String password) {
		Context.logout();
		Context.authenticate(new UsernamePasswordCredentials(username, password));
	}
	
	@Test
	public void getAssignedBasisIds_shouldReturnAnEmptyListIfNoneConfiguredForTheAuthenticatedUser() {
		assertEquals(0, AccessUtil.getAssignedBasisIds(Location.class).size());
	}
	
	@Test
	public void getAssignedBasisIds_shouldReturnAListOfAccessibleBasisIdsForTheAuthenticatedUser() {
		reloginAs("dyorke", "test");
		List<String> basisIds = AccessUtil.getAssignedBasisIds(Location.class);
		assertEquals(2, basisIds.size());
		assertTrue(basisIds.contains("'4000'"));
		assertTrue(basisIds.contains("'1'"));
		
		basisIds = AccessUtil.getAssignedBasisIds(Program.class);
		assertEquals(1, basisIds.size());
		assertTrue(basisIds.contains("'1'"));
	}
	
	@Test
	public void getAssignedBasisIds_shouldReturnAnEmptyListIfNoBasisAssignedToTheAuthenticatedUser() {
		assertEquals(0, AccessUtil.getAccessiblePersonIds(Location.class).size());
	}
	
	@Test
	public void getAccessiblePersonIds_shouldReturnAListOfAccessiblePersonIdsForTheAuthenticatedUser() {
		reloginAs("dyorke", "test");
		List<String> patientIds = AccessUtil.getAccessiblePersonIds(Location.class);
		assertEquals(1, patientIds.size());
		assertTrue(patientIds.contains("1001"));
	}
	
}
