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
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class AppointmentLocationBasedFilterTest extends BaseFilterTest {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private DataFilterService service;
	
	@Before
	public void before() throws Exception {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "appointments.xml");
	}
	
	private List<Appointment> getAppointments() {
		return sessionFactory.getCurrentSession().createCriteria(Appointment.class).list();
	}
	
	@Test
	public void getAppointments_shouldReturnNoAppointmentsIfTheUserIsNotGrantedAccessToAnyBasis() {
		reloginAs("dBeckham", "test");
		assertEquals(0, getAppointments().size());
	}
	
	@Test
	public void getAppointments_shouldReturnAppointmentsBelongingToPatientsAccessibleToTheUser() {
		reloginAs("dyorke", "test");
		int expCount = 2;
		Collection<Appointment> appointments = getAppointments();
		assertEquals(expCount, appointments.size());
		assertTrue(TestUtil.containsId(appointments, 101));
		assertTrue(TestUtil.containsId(appointments, 102));
		
		service.grantAccess(Context.getAuthenticatedUser(), new Location(4001));
		expCount = 3;
		appointments = getAppointments();
		assertEquals(expCount, appointments.size());
		assertTrue(TestUtil.containsId(appointments, 101));
		assertTrue(TestUtil.containsId(appointments, 102));
		assertTrue(TestUtil.containsId(appointments, 103));
	}
	
	@Test
	public void getAppointments_shouldReturnAllAppointmentsIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		final int expCount = 3;
		Collection<Appointment> appointments = getAppointments();
		assertEquals(expCount, appointments.size());
		assertTrue(TestUtil.containsId(appointments, 101));
		assertTrue(TestUtil.containsId(appointments, 102));
		assertTrue(TestUtil.containsId(appointments, 103));
	}
	
	@Test
	public void getAppointments_shouldReturnAllAppointmentsIfLocationFilteringIsDisabled() {
		DataFilterTestUtils.disableLocationFiltering();
		reloginAs("dyorke", "test");
		assertEquals(3, getAppointments().size());
	}
	
}
