/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.Collection;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.DataFilterConstants;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.util.DatabaseUtil;

public class AccessUtilTest extends BaseFilterTest {
	
	@Before
	public void beforeTestMethod() {
		executeDataSet(TestConstants.MODULE_TEST_DATASET_XML);
	}
	
	public static void grantLocationAccessToUser(Integer userId, Integer locationId, Connection conn) {
		//TODO replace the hard coded id
		String classname = Location.class.getName();
		String columns = "entity_basis_map_id, entity_identifier, entity_type, basis_identifier, basis_type, "
		        + "creator, date_created, uuid";
		String query = "INSERT INTO " + DataFilterConstants.MODULE_ID + "_entity_basis_map (" + columns + ") "
		        + "VALUES (100, '" + userId + "', 'org.openmrs.User', '" + locationId + "', '" + classname
		        + "', 1, '2019-05-01 00:00:00.0', '" + UUID.randomUUID().toString() + "')";
		DatabaseUtil.executeSQL(conn, query, false);
	}
	
	@Test
	public void getPersonAttributeTypeId_shouldReturnNullIfNoUuidIsConfiguredForTheBasisType() {
		assertNull(AccessUtil.getPersonAttributeTypeId(Program.class));
	}
	
	@Test
	public void getPersonAttributeTypeId_shouldReturnTheIdForPersonAttributeTypeForTheBasisType() {
		assertEquals(6000, AccessUtil.getPersonAttributeTypeId(Location.class).intValue());
	}
	
	@Test
	public void getAssignedBasisIds_shouldReturnAnEmptyListIfNoneConfiguredForTheAuthenticatedUser() {
		assertEquals(0, AccessUtil.getAssignedBasisIds(Location.class).size());
	}
	
	@Test
	public void getAssignedBasisIds_shouldReturnAListOfAccessibleBasisIdsForTheAuthenticatedUser() {
		reloginAs("dyorke", "test");
		final Integer englandLocationId = 4000;
		final Integer unknownLocationId = 1;
		Collection<String> basisIds = AccessUtil.getAssignedBasisIds(Location.class);
		assertEquals(2, basisIds.size());
		assertTrue(basisIds.contains(englandLocationId.toString()));
		assertTrue(basisIds.contains(unknownLocationId.toString()));
		
		//Should include child locations
		final Integer ugandaLocationId = 4001;
		grantLocationAccessToUser(Context.getAuthenticatedUser().getUserId(), ugandaLocationId, getConnection());
		basisIds = AccessUtil.getAssignedBasisIds(Location.class);
		assertEquals(4, basisIds.size());
		assertTrue(basisIds.contains(englandLocationId.toString()));
		assertTrue(basisIds.contains(unknownLocationId.toString()));
		assertTrue(basisIds.contains(ugandaLocationId.toString()));
		assertTrue(basisIds.contains("4002"));
		
		basisIds = AccessUtil.getAssignedBasisIds(Program.class);
		assertEquals(1, basisIds.size());
		assertTrue(basisIds.contains("1"));
	}
	
	@Test
	public void getAssignedBasisIds_shouldReturnAnEmptyListIfNoBasisAssignedToTheAuthenticatedUser() {
		assertEquals(0, AccessUtil.getAccessiblePersonIds(Location.class).size());
	}
	
	@Test
	public void getAccessiblePersonIds_shouldReturnAListOfAccessiblePersonIdsForTheAuthenticatedUser() {
		reloginAs("dyorke", "test");
		Collection<String> patientIds = AccessUtil.getAccessiblePersonIds(Location.class);
		assertEquals(1, patientIds.size());
		assertTrue(patientIds.contains("1001"));
		
		grantLocationAccessToUser(Context.getAuthenticatedUser().getUserId(), 4001, getConnection());
		patientIds = AccessUtil.getAccessiblePersonIds(Location.class);
		//Should include the child location too
		assertEquals(3, patientIds.size());
		assertTrue(patientIds.contains("1001"));
		assertTrue(patientIds.contains("1002"));
		assertTrue(patientIds.contains("1003"));
	}
	
	@Test
	public void getViewPrivilege_shouldReturnTheTheEncounterViewPrivilege() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "privilegedEncounters.xml");
		assertEquals("Manage Chemo Patients", AccessUtil.getViewPrivilege(5000));
	}
	
}
