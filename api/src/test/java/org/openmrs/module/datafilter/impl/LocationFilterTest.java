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

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.openmrs.test.TestUtil;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class LocationFilterTest extends BaseFilterTest {
	
	@Autowired
	private LocationService locationService;
	
	@Autowired
	private DataFilterService service;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "persons.xml");
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "users.xml");
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "locations.xml");
	}
	
	@Test
	public void getLocations_shouldReturnNoLocationsIfTheUserIsNotGrantedAccessToAny() {
		reloginAs("dBeckham", "test");
		assertEquals(0, locationService.getLocations("Kampala").size());
	}
	
	@Test
	public void getLocations_shouldReturnNoLocationsIfThereIsNoAuthenticatedUser() {
		Context.logout();
		Context.addProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
		try {
			assertEquals(0, locationService.getLocations("Kampala").size());
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
		}
	}
	
	@Test
	public void getLocations_shouldReturnLocationsBelongingToPatientsAccessibleToTheUser() throws Exception {
		reloginAs("dyorke", "test");
		int expCount = 7;
		Collection<Location> locations = locationService.getLocations("Kampala");
		assertEquals(expCount, locations.size());
		assertTrue(TestUtil.containsId(locations, 40000));
		assertTrue(TestUtil.containsId(locations, 40001));
		assertTrue(TestUtil.containsId(locations, 40003));
		assertTrue(TestUtil.containsId(locations, 40004));
		assertTrue(TestUtil.containsId(locations, 40006));
		assertTrue(TestUtil.containsId(locations, 40007));
		assertTrue(TestUtil.containsId(locations, 40008));
		
		service.grantAccess(Context.getAuthenticatedUser(), new Location(40002));
		expCount = 8;
		locations = locationService.getLocations("Kampala");
		assertEquals(expCount, locations.size());
		assertTrue(TestUtil.containsId(locations, 40000));
		assertTrue(TestUtil.containsId(locations, 40001));
		assertTrue(TestUtil.containsId(locations, 40002));
		assertTrue(TestUtil.containsId(locations, 40003));
		assertTrue(TestUtil.containsId(locations, 40004));
		assertTrue(TestUtil.containsId(locations, 40006));
		assertTrue(TestUtil.containsId(locations, 40007));
		assertTrue(TestUtil.containsId(locations, 40008));
	}
	
	@Test
	public void getLocations_shouldReturnAllLocationsIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		final int expCount = 8;
		Collection<Location> locations = locationService.getLocations("Kampala");
		assertEquals(expCount, locations.size());
		assertTrue(TestUtil.containsId(locations, 40000));
		assertTrue(TestUtil.containsId(locations, 40001));
		assertTrue(TestUtil.containsId(locations, 40002));
	}
	
	@Test
	public void getLocations_shouldReturnAllLocationsIfFilteringIsDisabled() {
		DataFilterTestUtils.disableLocationFiltering();
		reloginAs("dyorke", "test");
		assertEquals(8, locationService.getLocations("Kampala").size());
	}
	
}
