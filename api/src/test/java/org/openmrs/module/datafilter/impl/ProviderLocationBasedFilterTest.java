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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class ProviderLocationBasedFilterTest extends BaseFilterTest {
	
	@Autowired
	private ProviderService providerService;
	
	@Autowired
	private DataFilterService service;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "persons.xml");
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "users.xml");
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "providers.xml");
		DataFilterTestUtils.disableProgramBasedFiltering();
	}
	
	private Collection<Provider> getProviders() {
		return providerService.getProviders("Mulemba", null, null, null, true);
	}
	
	@Test
	public void getProviders_shouldReturnNoProvidersIfTheProviderIsNotGrantedAccessToAnyBasis() {
		reloginAs("dBeckham", "test");
		assertEquals(0, getProviders().size());
	}
	
	@Test
	public void getProviders_shouldReturnProvidersAccessibleToTheProvider() {
		reloginAs("dyorke", "test");
		int expCount = 2;
		Collection<Provider> providers = getProviders();
		assertEquals(expCount, providers.size());
		assertTrue(TestUtil.containsId(providers, 10001));
		assertTrue(TestUtil.containsId(providers, 10002));
		
		service.grantAccess(Context.getAuthenticatedUser(), new Location(4001));
		expCount = 3;
		providers = getProviders();
		assertEquals(expCount, providers.size());
		assertTrue(TestUtil.containsId(providers, 10001));
		assertTrue(TestUtil.containsId(providers, 10002));
		assertTrue(TestUtil.containsId(providers, 10003));
	}
	
	@Test
	public void getProviders_shouldReturnAllProvidersIfTheAuthenticatedProviderIsASuperProvider() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		assertEquals(7, getProviders().size());
	}
	
	@Test
	public void getProviders_shouldReturnAllProvidersIfLocationFilteringIsDisabled() {
		DataFilterTestUtils.disableLocationFiltering();
		reloginAs("dyorke", "test");
		assertEquals(7, getProviders().size());
	}
	
	@Test
	public void getProviderByUuid_shouldReturnTheProviderThatMatchesTheSpecifiedUuid() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "users.xml");
		reloginAs("cmulemba", "test");
		
		Provider provider = providerService.getProviderByUuid("b1e3868a-6b90-11e0-93c3-18a905e044dc");
		Assert.assertNotNull(provider);
		assertEquals(10002, provider.getProviderId().intValue());
	}
	
}
