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
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Privilege;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.FilterTestUtils;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.openmrs.test.TestUtil;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class UserProgramBasedFilterTest extends BaseProgramBasedFilterTest {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private DataFilterService service;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "persons.xml");
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "users.xml");
	}
	
	private Collection<User> getUsers() {
		return userService.getUsers("Mulemba", null, true, null, null);
	}
	
	@Test
	public void getUsers_shouldExcludeUsersWithProgramPrivilegeForAUserThatHasAtleastOneRoleButNoPrivileges() {
		reloginAs("jmulemba", "test");
		assertTrue(Context.getAuthenticatedUser().getAllRoles().size() > 0);
		assertEquals(0, Context.getAuthenticatedUser().getPrivileges().size());
		Context.addProxyPrivilege(PrivilegeConstants.GET_USERS);
		Collection<User> users = getUsers();
		Context.removeProxyPrivilege(PrivilegeConstants.GET_USERS);
		assertEquals(3, users.size());
		assertTrue(TestUtil.containsId(users, 10004));
		assertTrue(TestUtil.containsId(users, 10005));
		assertTrue(TestUtil.containsId(users, 10006));
	}
	
	@Test
	public void getUsers_shouldExcludeUsersWithProgramPrivilegeForAUserThatHasNoRoles() {
		reloginAs("smulemba", "test");
		assertEquals(0, Context.getAuthenticatedUser().getAllRoles().size());
		Context.addProxyPrivilege(PrivilegeConstants.GET_USERS);
		Collection<User> users = getUsers();
		Context.removeProxyPrivilege(PrivilegeConstants.GET_USERS);
		assertEquals(3, users.size());
		assertTrue(TestUtil.containsId(users, 10004));
		assertTrue(TestUtil.containsId(users, 10005));
		assertTrue(TestUtil.containsId(users, 10006));
	}
	
	@Test
	public void getUsers_shouldExcludeUsersWithProgramPrivilegeForAUserThatHasSeveralRolesAndOtherPrivilegesButNoProgramPrivileges() {
		reloginAs("tmulemba", "test");
		assertTrue(Context.getAuthenticatedUser().getAllRoles().size() > 1);
		Collection<Privilege> userPrivileges = Context.getAuthenticatedUser().getPrivileges();
		assertTrue(userPrivileges.size() > 0);
		Collection<String> programPrivileges = AccessUtil.getAllProgramPrivileges();
		assertEquals(0, userPrivileges.stream().filter(p -> programPrivileges.contains(p.getName()))
		        .collect(Collectors.toList()).size());
		Collection<User> users = getUsers();
		assertEquals(3, users.size());
		assertTrue(TestUtil.containsId(users, 10004));
		assertTrue(TestUtil.containsId(users, 10005));
		assertTrue(TestUtil.containsId(users, 10006));
	}
	
	@Test
	public void getUsers_shouldReturnUsersWithAccessToTheSameProgramsAsTheAuthenticatedUser() {
		reloginAs("cmulemba", "test");
		int expCount = 5;
		Collection<User> users = getUsers();
		assertEquals(expCount, users.size());
		assertTrue(TestUtil.containsId(users, 10001));
		assertTrue(TestUtil.containsId(users, 10002));
		//Should include a user that has at least one role but no privileges
		assertTrue(TestUtil.containsId(users, 10004));
		//Should include a user with no roles
		assertTrue(TestUtil.containsId(users, 10005));
		//Should include a user with some other role(s) but none has a program privilege
		assertTrue(TestUtil.containsId(users, 10006));
		
		DataFilterTestUtils.addPrivilege(BaseProgramBasedFilterTest.PRIV_VIEW_PROGRAM_2);
		expCount = 6;
		users = getUsers();
		assertEquals(expCount, users.size());
		assertTrue(TestUtil.containsId(users, 10001));
		assertTrue(TestUtil.containsId(users, 10002));
		assertTrue(TestUtil.containsId(users, 10003));
		assertTrue(TestUtil.containsId(users, 10004));
		assertTrue(TestUtil.containsId(users, 10005));
		assertTrue(TestUtil.containsId(users, 10006));
	}
	
	@Test
	public void getUsers_shouldReturnAllUsersIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		assertEquals(6, getUsers().size());
	}
	
	@Test
	public void getUsers_shouldReturnAllUsersIfTheFilterIsDisabled() {
		FilterTestUtils.disableFilter(ImplConstants.PROGRAM_BASED_FILTER_NAME_USER);
		reloginAs("dyorke", "test");
		assertEquals(6, getUsers().size());
	}
	
}
