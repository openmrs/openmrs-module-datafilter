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
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class VisitLocationBasedFilterTest extends BaseFilterTest {
	
	@Autowired
	private VisitService visitService;
	
	@Autowired
	private DataFilterService service;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "visits.xml");
	}
	
	private Collection<Visit> getVisits() {
		return visitService.getVisits(Collections.singleton(new VisitType(2000)), null, null, null, null, null, null, null,
		    null, true, true);
	}
	
	@Test
	public void getVisits_shouldReturnNoVisitsIfTheUserIsNotGrantedAccessToAnyBasis() {
		reloginAs("dBeckham", "test");
		assertEquals(0, getVisits().size());
	}
	
	@Test
	public void getVisits_shouldReturnVisitsBelongingToPatientsAccessibleToTheUser() {
		reloginAs("dyorke", "test");
		int expCount = 2;
		Collection<Visit> visits = getVisits();
		assertEquals(expCount, visits.size());
		assertTrue(TestUtil.containsId(visits, 1000));
		assertTrue(TestUtil.containsId(visits, 1001));
		
		service.grantAccess(Context.getAuthenticatedUser(), new Location(4001));
		expCount = 3;
		visits = getVisits();
		assertEquals(expCount, visits.size());
		assertTrue(TestUtil.containsId(visits, 1000));
		assertTrue(TestUtil.containsId(visits, 1001));
		assertTrue(TestUtil.containsId(visits, 1002));
	}
	
	@Test
	public void getVisits_shouldReturnAllVisitsIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		final int expCount = 3;
		Collection<Visit> visits = getVisits();
		assertEquals(expCount, visits.size());
		assertTrue(TestUtil.containsId(visits, 1000));
		assertTrue(TestUtil.containsId(visits, 1001));
		assertTrue(TestUtil.containsId(visits, 1002));
	}
	
	@Test
	public void getVisits_shouldReturnAllVisitsIfLocationFilteringIsDisabled() {
		DataFilterTestUtils.disableLocationFiltering();
		reloginAs("dyorke", "test");
		assertEquals(3, getVisits().size());
	}
	
}
