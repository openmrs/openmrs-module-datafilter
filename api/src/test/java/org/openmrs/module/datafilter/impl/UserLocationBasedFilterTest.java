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
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class UserLocationBasedFilterTest extends BaseFilterTest {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private DataFilterService service;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "persons.xml");
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "users.xml");
		DataFilterTestUtils.disableProgramBasedFiltering();
	}
	
	private Collection<User> getUsers() {
		return userService.getUsers("Mulemba", null, true, null, null);
	}
	
	@Test
	public void getUsers_shouldReturnNoUsersIfTheUserIsNotGrantedAccessToAnyBasis() {
		reloginAs("dBeckham", "test");
		assertEquals(0, getUsers().size());
	}
	
	@Test
	public void getUsers_shouldReturnUsersAccessibleToTheUser() {
		reloginAs("dyorke", "test");
		int expCount = 2;
		Collection<User> users = getUsers();
		assertEquals(expCount, users.size());
		assertTrue(TestUtil.containsId(users, 10001));
		assertTrue(TestUtil.containsId(users, 10002));
		
		service.grantAccess(Context.getAuthenticatedUser(), new Location(4001));
		expCount = 3;
		users = getUsers();
		assertEquals(expCount, users.size());
		assertTrue(TestUtil.containsId(users, 10001));
		assertTrue(TestUtil.containsId(users, 10002));
		assertTrue(TestUtil.containsId(users, 10003));
	}
	
	@Test
	public void getUsers_shouldReturnAllUsersIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		assertEquals(7, getUsers().size());
	}
	
	@Test
	public void getUsers_shouldReturnAllUsersIfLocationFilteringIsDisabled() {
		DataFilterTestUtils.disableLocationFiltering();
		reloginAs("dyorke", "test");
		assertEquals(7, getUsers().size());
	}
	
}
