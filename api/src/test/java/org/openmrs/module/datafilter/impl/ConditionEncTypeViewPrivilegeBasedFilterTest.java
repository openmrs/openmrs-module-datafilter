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
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Condition;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class ConditionEncTypeViewPrivilegeBasedFilterTest extends BaseEncTypeViewPrivilegeBasedFilterTest {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "privilegedEncounters.xml");
	}
	
	protected List<Condition> getConditions() {
		return sessionFactory.getCurrentSession().createCriteria(Condition.class).list();
	}
	
	@Test
	public void getCondition_shouldIncludeConditionsLinkedToEncountersThatRequireAPrivilegeAndTheUserHasIt() {
		reloginAs("dyorke", "test");
		assertFalse(Context.getAuthenticatedUser().hasPrivilege(PRIV_MANAGE_CHEMO_PATIENTS));
		int expCount = 1;//Only return the encounter-less condition
		Collection<Condition> conditions = getConditions();
		assertEquals(expCount, conditions.size());
		Condition condition = conditions.iterator().next();
		assertEquals(1005, condition.getId().longValue());
		assertNull(condition.getEncounter());
		
		DataFilterTestUtils.addPrivilege(PRIV_MANAGE_CHEMO_PATIENTS);
		expCount = 2;
		conditions = getConditions();
		assertEquals(expCount, conditions.size());
		assertTrue(TestUtil.containsId(conditions, 1004));
		assertTrue(TestUtil.containsId(conditions, 1005));
	}
	
	@Test
	public void getCondition_shouldReturnAllConditionsIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		Collection<Condition> conditions = getConditions();
		assertEquals(2, conditions.size());
		assertTrue(TestUtil.containsId(conditions, 1004));
		assertTrue(TestUtil.containsId(conditions, 1005));
	}
	
	@Test
	public void getCondition_shouldReturnAllConditionsIfEncTypeViewPrivFilteringIsDisabled() {
		DataFilterTestUtils.disableEncTypeViewPrivilegeFiltering();
		Collection<Condition> conditions = getConditions();
		assertEquals(2, conditions.size());
		assertTrue(TestUtil.containsId(conditions, 1004));
		assertTrue(TestUtil.containsId(conditions, 1005));
	}
	
}
