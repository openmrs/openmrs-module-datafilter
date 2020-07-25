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
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Condition;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class ConditionLocationBasedFilterTest extends BaseFilterTest {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private DataFilterService service;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "encounters.xml");
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "conditions.xml");
	}
	
	protected List<Condition> getConditions() {
		return sessionFactory.getCurrentSession().createCriteria(Condition.class).list();
	}
	
	@Test
	public void getCondition_shouldReturnNoConditionIfTheUserIsNotGrantedAccessToAnyBasis() {
		reloginAs("dBeckham", "test");
		assertEquals(0, getConditions().size());
	}
	
	@Test
	public void getCondition_shouldReturnConditionsBelongingToPatientsAccessibleToTheUser() {
		reloginAs("dyorke", "test");
		int expCount = 2;
		Collection<Condition> conditions = getConditions();
		assertEquals(expCount, conditions.size());
		assertTrue(TestUtil.containsId(conditions, 1001));
		assertTrue(TestUtil.containsId(conditions, 1002));
		
		service.grantAccess(Context.getAuthenticatedUser(), new Location(4001));
		expCount = 3;
		conditions = getConditions();
		assertEquals(expCount, conditions.size());
		assertTrue(TestUtil.containsId(conditions, 1001));
		assertTrue(TestUtil.containsId(conditions, 1002));
		assertTrue(TestUtil.containsId(conditions, 1003));
	}
	
	@Test
	public void getCondition_shouldReturnAllConditionsIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		int expCount = 3;
		Collection<Condition> conditions = getConditions();
		assertEquals(expCount, conditions.size());
		assertTrue(TestUtil.containsId(conditions, 1001));
		assertTrue(TestUtil.containsId(conditions, 1002));
		assertTrue(TestUtil.containsId(conditions, 1003));
	}
	
	@Test
	public void getCondition_shouldReturnAllConditionsIfLocationFilteringIsDisabled() {
		DataFilterTestUtils.disableLocationFiltering();
		reloginAs("dyorke", "test");
		assertEquals(3, getConditions().size());
	}
	
}
