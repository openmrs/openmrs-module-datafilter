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
import org.openmrs.Program;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.FilterTestUtils;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.openmrs.test.TestUtil;
import org.openmrs.util.DatabaseUtil;
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
	public void getUsers_shouldExcludeUsersWithProgramRolesForAUserThatHasNoRoles() {
		reloginAs("smulemba", "test");
		assertEquals(0, Context.getAuthenticatedUser().getAllRoles().size());
		Context.addProxyPrivilege(PrivilegeConstants.GET_USERS);
		Collection<User> users = getUsers();
		Context.removeProxyPrivilege(PrivilegeConstants.GET_USERS);
		assertEquals(2, users.size());
		assertTrue(TestUtil.containsId(users, 10005));
		assertTrue(TestUtil.containsId(users, 10006));
	}
	
	@Test
	public void getUsers_shouldExcludeUsersWithProgramRolesForAUserThatHasNoProgramRoles() {
		reloginAs("tmulemba", "test");
		Collection<Role> userRoles = Context.getAuthenticatedUser().getAllRoles();
		assertTrue(userRoles.size() > 0);
		Collection<String> programRoles = AccessUtil.getAllProgramRoles();
		assertEquals(0,
		    userRoles.stream().filter(role -> programRoles.contains(role.getName())).collect(Collectors.toList()).size());
		Collection<User> users = getUsers();
		assertEquals(2, users.size());
		assertTrue(TestUtil.containsId(users, 10005));
		assertTrue(TestUtil.containsId(users, 10006));
	}
	
	@Test
	public void getUsers_shouldReturnUsersWithAccessToTheSameProgramsAsTheAuthenticatedUser() {
		reloginAs("cmulemba", "test");
		int expCount = 6;
		Collection<User> users = getUsers();
		assertEquals(expCount, users.size());
		assertTrue(TestUtil.containsId(users, 10001));
		assertTrue(TestUtil.containsId(users, 10002));
		//Should include a user working at the same program but in a different uncommon role
		assertTrue(TestUtil.containsId(users, 10004));
		//Should include a user with no roles
		assertTrue(TestUtil.containsId(users, 10005));
		//Should include a user with some other role(s) but none is a program role
		assertTrue(TestUtil.containsId(users, 10006));
		//Should include a user with any of the roles the user has
		assertTrue(TestUtil.containsId(users, 10007));
		
		service.grantAccess(new Role(ROLE_COORDINATOR_PROG_1), new Program(10002));
		expCount = 7;
		users = getUsers();
		assertEquals(expCount, users.size());
		assertTrue(TestUtil.containsId(users, 10001));
		assertTrue(TestUtil.containsId(users, 10002));
		assertTrue(TestUtil.containsId(users, 10003));
		assertTrue(TestUtil.containsId(users, 10004));
		assertTrue(TestUtil.containsId(users, 10005));
		assertTrue(TestUtil.containsId(users, 10006));
		assertTrue(TestUtil.containsId(users, 10007));
	}
	
	@Test
	public void getUsers_shouldReturnAllUsersIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		assertEquals(7, getUsers().size());
	}
	
	@Test
	public void getUsers_shouldReturnAllUsersIfTheFilterIsDisabled() {
		FilterTestUtils.disableFilter(ImplConstants.PROGRAM_BASED_FILTER_NAME_USER);
		reloginAs("dyorke", "test");
		assertEquals(7, getUsers().size());
	}
	
	@Test
	public void getProvidersByPerson_shouldFailWithSQLSyntaxErrorException() throws Exception {
		// Setup
		String exception = "You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version for the right syntax to use near ')\n"
		        + "                            OR datafilter_ur.role NOT IN ()\n" + "                  ' at line 6";
		String sql = "DELETE FROM datafilter_entity_basis_map";
		FilterTestUtils.enableFilter(ImplConstants.PROGRAM_BASED_FILTER_NAME_USER);
		DatabaseUtil.executeSQL(getConnection(), sql, false);
		Context.logout();
		
		// Replay
		Context.addProxyPrivilege(PrivilegeConstants.GET_PERSONS);
		Context.addProxyPrivilege(PrivilegeConstants.GET_PROVIDERS);
		try {
			Context.getProviderService().getProvidersByPerson(Context.getPersonService().getPerson(1001));
		}
		catch (Exception e) {
			assertEquals(e.getCause().getMessage(), exception);
		}
	}
}
