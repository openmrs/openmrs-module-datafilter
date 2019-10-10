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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class ObsEncTypeViewPrivilegeBasedFilterTest extends BaseEncTypeViewPrivilegeBasedFilterTest {
	
	@Autowired
	private ObsService obsService;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "privilegedEncounters.xml");
	}
	
	private List<Obs> getObservations() {
		List<Concept> questions = Collections.singletonList(new Concept(5089));
		List<Encounter> encounters = new ArrayList<>();
		encounters.add(new Encounter(2001));
		return obsService.getObservations(null, encounters, questions, null, null, null, null, null, null, null, null,
		    false);
	}
	
	@Test
	public void getObs_shouldIncludeEncountersThatRequireAPrivilegeAndTheUserHasIt() {
		reloginAs("dyorke", "test");
		int expCount = 0;
		assertEquals(expCount, getObservations().size());
	}
	
	@Test
	public void getObs_shouldExcludeEncountersThatRequireAPrivilege() {
		reloginAs("dyorke", "test");
		int expCount = 0;
		Collection<Obs> observations = getObservations();
		assertEquals(expCount, observations.size());
		
		DataFilterTestUtils.addPrivilege(BaseEncTypeViewPrivilegeBasedFilterTest.PRIV_MANAGE_CHEMO_PATIENTS);
		expCount = 1;
		observations = getObservations();
		assertEquals(expCount, observations.size());
		assertTrue(TestUtil.containsId(observations, 1004));
	}
	
	@Test
	public void getObs_shouldReturnAllObsIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		int expCount = 1;
		Collection<Obs> observations = getObservations();
		assertEquals(expCount, observations.size());
		assertTrue(TestUtil.containsId(observations, 1004));
	}
	
}
