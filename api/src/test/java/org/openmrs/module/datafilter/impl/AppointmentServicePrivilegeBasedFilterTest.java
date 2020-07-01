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

import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class AppointmentServicePrivilegeBasedFilterTest extends BaseFilterTest {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "appointments.xml");
		DataFilterTestUtils.disableLocationFiltering();
	}
	
	private List<AppointmentServiceDefinition> getServices() {
		return sessionFactory.getCurrentSession().createCriteria(AppointmentServiceDefinition.class).list();
	}
	
	@Test
	public void getServices_shouldExcludeServicesThatRequireAPrivilegeTheUserDoesNotHave() {
		reloginAs("dBeckham", "test");
		assertEquals(0, getServices().size());
	}
	
	@Test
	public void getServices_shouldIncludeServicesAssociatedToServicesThatRequireAPrivilegeAndTheUserHasIt() {
		reloginAs("dyorke", "test");
		int expCount = 2;
		List<AppointmentServiceDefinition> services = getServices();
		assertEquals(expCount, services.size());
		assertTrue(TestUtil.containsId(services, 301));
		assertTrue(TestUtil.containsId(services, 302));
		
		DataFilterTestUtils.addPrivilege(TestConstants.PRIV_MANAGE_DENTAL_APPOINTMENTS);
		expCount = 3;
		services = getServices();
		assertEquals(expCount, services.size());
		assertTrue(TestUtil.containsId(services, 301));
		assertTrue(TestUtil.containsId(services, 302));
		assertTrue(TestUtil.containsId(services, 303));
	}
	
	@Test
	public void getServices_shouldReturnAllServicesIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		List<AppointmentServiceDefinition> services = getServices();
		assertEquals(4, services.size());
		assertTrue(TestUtil.containsId(services, 301));
		assertTrue(TestUtil.containsId(services, 302));
		assertTrue(TestUtil.containsId(services, 303));
		assertTrue(TestUtil.containsId(services, 304));
	}
	
	@Test
	public void getServices_shouldReturnAllServicesIfPrivFilteringIsDisabled() {
		DataFilterTestUtils.disableAppointmentPrivilegeFiltering();
		reloginAs("dyorke", "test");
		List<AppointmentServiceDefinition> services = getServices();
		assertEquals(4, services.size());
		assertTrue(TestUtil.containsId(services, 301));
		assertTrue(TestUtil.containsId(services, 302));
		assertTrue(TestUtil.containsId(services, 303));
		assertTrue(TestUtil.containsId(services, 304));
	}
	
}
