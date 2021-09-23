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

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.TestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import static org.junit.Assert.*;

public class EncTypePrivilegeBasedFilterTest extends BaseEncTypeViewPrivilegeBasedFilterTest {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "privilegedEncounters.xml");
	}
	
	@Test
	public void getEncounterType_shouldIncludeEncountersThatRequireAPrivilegeAndTheUserHasIt() {
		reloginAs("dyorke", "test");
		assertFalse(Context.getAuthenticatedUser().hasPrivilege(PRIV_MANAGE_CHEMO_PATIENTS));
		
		int expCount = 3;
		List ecounterTypelist = sessionFactory.getCurrentSession().createCriteria(EncounterType.class).list();
		assertEquals(expCount, ecounterTypelist.size());
		
		DataFilterTestUtils.addPrivilege(PRIV_MANAGE_CHEMO_PATIENTS);
		expCount = 4;
		ecounterTypelist = sessionFactory.getCurrentSession().createCriteria(EncounterType.class).list();
		assertEquals(expCount, ecounterTypelist.size());
	}
	
	@Test
	public void getCondition_shouldReturnAllEncounterTypesIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		List encounterTypes = sessionFactory.getCurrentSession().createCriteria(EncounterType.class).list();
		System.out.println(encounterTypes);
		assertEquals(4, encounterTypes.size());
	}
	
	@Test
	public void getCondition_shouldReturnAllEncounterTypesIfEncTypeViewPrivFilteringIsDisabled() {
		DataFilterTestUtils.disableEncTypeViewPrivilegeFiltering();
		List encounterTypes = sessionFactory.getCurrentSession().createCriteria(EncounterType.class).list();
		assertEquals(4, encounterTypes.size());
	}
	
}
