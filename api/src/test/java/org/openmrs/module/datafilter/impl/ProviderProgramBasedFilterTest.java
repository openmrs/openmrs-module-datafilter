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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Privilege;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.FilterTestUtils;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.openmrs.test.TestUtil;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class ProviderProgramBasedFilterTest extends BaseProgramBasedFilterTest {
	
	@Autowired
	private ProviderService providerService;
	
	@Autowired
	private DataFilterService service;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "persons.xml");
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "users.xml");
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "providers.xml");
	}
	
	private Collection<Provider> getProviders() {
		return providerService.getProviders("Mulemba", null, null, null, true);
	}
	
	@Test
	public void getProviders_shouldExcludeProvidersWithProgramPrivilegeForAUserThatHasAtleastOneRoleButNoPrivileges() {
		reloginAs("jmulemba", "test");
		assertTrue(Context.getAuthenticatedUser().getAllRoles().size() > 0);
		assertEquals(0, Context.getAuthenticatedUser().getPrivileges().size());
		Context.addProxyPrivilege(PrivilegeConstants.GET_PROVIDERS);
		Collection<Provider> providers = getProviders();
		Context.removeProxyPrivilege(PrivilegeConstants.GET_PROVIDERS);
		assertEquals(5, providers.size());
		assertTrue(TestUtil.containsId(providers, 10004));
		assertTrue(TestUtil.containsId(providers, 10005));
		assertTrue(TestUtil.containsId(providers, 10006));
		//Should include providers with no person record with provider role(s) but none is linked to a program
		assertTrue(TestUtil.containsId(providers, 10011));
		//Should include providers with no person record and no provider role
		assertTrue(TestUtil.containsId(providers, 10012));
	}
	
	@Test
	public void getProviders_shouldExcludeProvidersWithProgramPrivilegeForAUserThatHasNoRoles() {
		reloginAs("smulemba", "test");
		assertEquals(0, Context.getAuthenticatedUser().getAllRoles().size());
		Context.addProxyPrivilege(PrivilegeConstants.GET_PROVIDERS);
		Collection<Provider> providers = getProviders();
		Collection<Provider> userProviderAccounts = providerService
		        .getProvidersByPerson(Context.getAuthenticatedUser().getPerson());
		Context.removeProxyPrivilege(PrivilegeConstants.GET_PROVIDERS);
		assertEquals(1, userProviderAccounts.size());
		assertNull(userProviderAccounts.iterator().next().getRole());
		assertEquals(5, providers.size());
		assertTrue(TestUtil.containsId(providers, 10004));
		assertTrue(TestUtil.containsId(providers, 10005));
		assertTrue(TestUtil.containsId(providers, 10006));
		//Should include providers with no person record with provider role(s) but none is linked to a program
		assertTrue(TestUtil.containsId(providers, 10011));
		//Should include providers with no person record and no provider role
		assertTrue(TestUtil.containsId(providers, 10012));
	}
	
	@Test
	public void getProviders_shouldExcludeProvidersWithProgramPrivilegeForAUserThatHasSeveralRolesAndOtherPrivilegesButNoProgramPrivileges() {
		reloginAs("tmulemba", "test");
		assertTrue(Context.getAuthenticatedUser().getAllRoles().size() > 1);
		Collection<Privilege> userPrivileges = Context.getAuthenticatedUser().getPrivileges();
		assertTrue(userPrivileges.size() > 0);
		Collection<String> programPrivileges = AccessUtil.getAllProgramPrivileges();
		assertEquals(0, userPrivileges.stream().filter(p -> programPrivileges.contains(p.getName()))
		        .collect(Collectors.toList()).size());
		Context.addProxyPrivilege(PrivilegeConstants.GET_PROVIDERS);
		Collection<Provider> providers = getProviders();
		Context.removeProxyPrivilege(PrivilegeConstants.GET_PROVIDERS);
		assertEquals(5, providers.size());
		assertTrue(TestUtil.containsId(providers, 10004));
		assertTrue(TestUtil.containsId(providers, 10005));
		assertTrue(TestUtil.containsId(providers, 10006));
		//Should include providers with no person record with provider role(s) but none is linked to a program
		assertTrue(TestUtil.containsId(providers, 10011));
		//Should include providers with no person record and no provider role
		assertTrue(TestUtil.containsId(providers, 10012));
	}
	
	@Test
	public void getProviders_shouldReturnProvidersWithAccessToTheSameProgramsAsTheAuthenticatedUser() {
		reloginAs("cmulemba", "test");
		int expCount = 10;
		Context.addProxyPrivilege(PrivilegeConstants.GET_PROVIDERS);
		Collection<Provider> providers = getProviders();
		assertEquals(expCount, providers.size());
		assertTrue(TestUtil.containsId(providers, 10001));
		assertTrue(TestUtil.containsId(providers, 10002));
		//Should include a provider that has at least one role but no privileges
		assertTrue(TestUtil.containsId(providers, 10004));
		//Should include a provider with no roles
		assertTrue(TestUtil.containsId(providers, 10005));
		//Should include a provider with some other role(s) but none has a program privilege
		assertTrue(TestUtil.containsId(providers, 10006));
		//Should include a provider with any of the roles the user has
		assertTrue(TestUtil.containsId(providers, 10007));
		//Should include providers with no person record but linked to the same program via a provider role
		assertTrue(TestUtil.containsId(providers, 10008));
		assertTrue(TestUtil.containsId(providers, 10009));
		//Should include providers with no person record with provider role(s) but none is linked to a program
		assertTrue(TestUtil.containsId(providers, 10011));
		//Should include providers with no person record and no provider role
		assertTrue(TestUtil.containsId(providers, 10012));
		
		DataFilterTestUtils.addPrivilege(BaseProgramBasedFilterTest.PRIV_VIEW_PROGRAM_2);
		expCount = 12;
		providers = getProviders();
		Context.removeProxyPrivilege(PrivilegeConstants.GET_PROVIDERS);
		assertEquals(expCount, providers.size());
		assertTrue(TestUtil.containsId(providers, 10001));
		assertTrue(TestUtil.containsId(providers, 10002));
		assertTrue(TestUtil.containsId(providers, 10003));
		assertTrue(TestUtil.containsId(providers, 10004));
		assertTrue(TestUtil.containsId(providers, 10005));
		assertTrue(TestUtil.containsId(providers, 10006));
		assertTrue(TestUtil.containsId(providers, 10007));
		assertTrue(TestUtil.containsId(providers, 10008));
		assertTrue(TestUtil.containsId(providers, 10009));
		assertTrue(TestUtil.containsId(providers, 10010));
		assertTrue(TestUtil.containsId(providers, 10011));
		assertTrue(TestUtil.containsId(providers, 10012));
	}
	
	@Test
	public void getProviders_shouldReturnAllProvidersIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		assertEquals(12, getProviders().size());
	}
	
	@Test
	public void getProviders_shouldReturnAllProvidersIfTheFilterIsDisabled() {
		FilterTestUtils.disableFilter(ImplConstants.PROGRAM_BASED_FILTER_NAME_PROVIDER);
		reloginAs("dyorke", "test");
		assertEquals(12, getProviders().size());
	}
	
}
