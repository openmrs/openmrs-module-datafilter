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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
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
		List<Concept> questions = Collections.singletonList(new Concept(5497));
		Person person = new Person();
		person.setPersonId(1001);
		List<Person> persons = Collections.singletonList(person);
		return obsService.getObservations(persons, null, questions, null, null, null, null, null, null, null, null, false);
	}
	
	@Test
	public void getEncounters_shouldIncludeObsLinkedToEncountersThatRequireAPrivilegeAndTheUserHasIt() {
		reloginAs("dyorke", "test");
		assertFalse(Context.getAuthenticatedUser().hasPrivilege(PRIV_MANAGE_CHEMO_PATIENTS));
		int expCount = 1;//Only return the encounter-less obs
		Collection<Obs> observations = getObservations();
		assertEquals(expCount, observations.size());
		Obs obs = observations.iterator().next();
		assertEquals(1005, obs.getId().longValue());
		assertNull(obs.getEncounter());
		
		DataFilterTestUtils.addPrivilege(PRIV_MANAGE_CHEMO_PATIENTS);
		expCount = 2;
		observations = getObservations();
		assertEquals(expCount, observations.size());
		assertTrue(TestUtil.containsId(observations, 1004));
		assertTrue(TestUtil.containsId(observations, 1005));
	}
	
	@Test
	public void getObs_shouldReturnAllObsIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		Collection<Obs> observations = getObservations();
		assertEquals(2, observations.size());
		assertTrue(TestUtil.containsId(observations, 1004));
		assertTrue(TestUtil.containsId(observations, 1005));
	}
	
	@Test
	public void getObs_shouldReturnAllObsIfEncTypeViewPrivFilteringIsDisabled() {
		DataFilterTestUtils.disableEncTypeViewPrivilegeFiltering();
		reloginAs("dyorke", "test");
		Collection<Obs> observations = getObservations();
		assertEquals(2, observations.size());
		assertTrue(TestUtil.containsId(observations, 1004));
		assertTrue(TestUtil.containsId(observations, 1005));
	}
	
}
