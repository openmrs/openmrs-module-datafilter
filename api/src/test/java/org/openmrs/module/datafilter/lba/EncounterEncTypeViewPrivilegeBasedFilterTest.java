/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.lba;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.api.DataFilterService;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class EncounterEncTypeViewPrivilegeBasedFilterTest extends BaseEncTypeViewPrivilegeBasedFilterTest {
	
	@Autowired
	private EncounterService encounterService;
	
	@Autowired
	private VisitService visitService;
	
	@Autowired
	private DataFilterService service;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "encounters.xml");
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "privilegedEncounters.xml");
	}
	
	@Test
	public void getEncounters_shouldExcludeEncountersThatRequireAPrivilege() {
		reloginAs("dBeckham", "test");
		final String name = "Navuga";
		final int expCount = 3;
		assertEquals(expCount, encounterService.getCountOfEncounters(name, false).intValue());
		assertEquals(expCount, encounterService.getEncounters(name, 0, Integer.MAX_VALUE, false).size());
	}
	
	@Test
	public void getEncounters_shouldIncludeEncountersThatRequireAPrivilegeAndTheUserHasIt() {
		reloginAs("dyorke", "test");
		final String name = "Navuga";
		int expCount = 3;
		assertEquals(expCount, encounterService.getCountOfEncounters(name, false).intValue());
		Collection<Encounter> encounters = encounterService.getEncounters(name, 0, Integer.MAX_VALUE, false);
		assertEquals(expCount, encounters.size());
		assertTrue(TestUtil.containsId(encounters, 1000));
		assertTrue(TestUtil.containsId(encounters, 1001));
		assertTrue(TestUtil.containsId(encounters, 1002));
		
		DataFilterTestUtils.addPrivilege(PRIV_MANAGE_CHEMO_PATIENTS);
		expCount = 4;
		assertEquals(expCount, encounterService.getCountOfEncounters(name, false).intValue());
		encounters = encounterService.getEncounters(name, 0, Integer.MAX_VALUE, false);
		assertEquals(expCount, encounters.size());
		assertTrue(TestUtil.containsId(encounters, 1000));
		assertTrue(TestUtil.containsId(encounters, 1001));
		assertTrue(TestUtil.containsId(encounters, 1002));
		assertTrue(TestUtil.containsId(encounters, 2001));
	}
	
	@Test
	public void getEncounters_shouldReturnAllEncountersIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		final String name = "Navuga";
		final int expCount = 4;
		assertEquals(expCount, encounterService.getCountOfEncounters(name, false).intValue());
		Collection<Encounter> encounters = encounterService.getEncounters(name, 0, Integer.MAX_VALUE, false);
		assertEquals(expCount, encounters.size());
		assertTrue(TestUtil.containsId(encounters, 1000));
		assertTrue(TestUtil.containsId(encounters, 1001));
		assertTrue(TestUtil.containsId(encounters, 1002));
		assertTrue(TestUtil.containsId(encounters, 2001));
	}
	
	@Test
	public void getEncounters_shouldReturnAllEncountersIfEncTypeViewPrivFilteringIsDisabled() {
		DataFilterTestUtils.disableEncTypeViewPrivilegeFiltering();
		reloginAs("dyorke", "test");
		final String name = "Navuga";
		assertEquals(4, encounterService.getCountOfEncounters(name, false).intValue());
		Collection<Encounter> encounters = encounterService.getEncounters(name, 0, Integer.MAX_VALUE, false);
		//In core this method already filters encounters by privilege
		assertEquals(3, encounters.size());
		assertTrue(TestUtil.containsId(encounters, 1000));
		assertTrue(TestUtil.containsId(encounters, 1001));
		assertTrue(TestUtil.containsId(encounters, 1002));
	}
	
	@Test
	public void getVisits_shouldExcludePrivilegedEncountersOfTheVisitIfTheUserHasNoRequiredPrivilege() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "visitWithEncounters.xml");
		reloginAs("dBeckham", "test");
		Set<Encounter> encounters = visitService.getVisit(10001).getEncounters();
		assertEquals(2, encounters.size());
		assertTrue(TestUtil.containsId(encounters, 20001));
		assertTrue(TestUtil.containsId(encounters, 20002));
	}
	
	@Test
	public void getVisits_shouldIncludePrivilegedEncountersOfTheVisitIfTheUserHasTheRequiredPrivilege() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "visitWithEncounters.xml");
		reloginAs("dBeckham", "test");
		DataFilterTestUtils.addPrivilege(PRIV_MANAGE_CHEMO_PATIENTS);
		assertEquals(4, visitService.getVisit(10001).getEncounters().size());
	}
	
}
