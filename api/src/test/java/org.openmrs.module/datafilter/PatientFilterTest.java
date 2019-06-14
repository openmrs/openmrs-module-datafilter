/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class PatientFilterTest extends BaseFilterTest {
	
	@Autowired
	private PatientService patientService;
	
	private static final String PATIENT_NAME = "Magidu";
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "patients.xml");
	}
	
	@Test
	public void getAllPatients_shouldReturnPatientsAccessibleToTheUser() {
		reloginAs("dyorke", "test");
		int expCount = 3;
		Collection<Patient> patients = patientService.getAllPatients();
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1001));
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1503));
		
		AccessUtilTest.grantLocationAccessToUser(Context.getAuthenticatedUser().getUserId(), 4001, getConnection());
		expCount = 5;
		assertEquals(expCount, patientService.getAllPatients().size());
		patients = patientService.getAllPatients();
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1001));
		assertTrue(TestUtil.containsId(patients, 1002));
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1502));
		assertTrue(TestUtil.containsId(patients, 1503));
	}
	
	@Test
	public void getAllPatients_shouldReturnNoPatientsIfTheUserIsNotGrantedAccessToAnyBasis() {
		reloginAs("dBeckham", "test");
		Collection<Patient> encounters = patientService.getAllPatients();
		assertEquals(0, encounters.size());
	}
	
	@Test
	public void getPatients_shouldReturnPatientsAccessibleToTheUser() {
		reloginAs("dyorke", "test");
		updateSearchIndex();
		int expCount = 2;
		assertEquals(expCount, patientService.getCountOfPatients(PATIENT_NAME).intValue());
		Collection<Patient> patients = patientService.getPatients(PATIENT_NAME);
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1503));
		
		AccessUtilTest.grantLocationAccessToUser(Context.getAuthenticatedUser().getUserId(), 4001, getConnection());
		expCount = 3;
		assertEquals(expCount, patientService.getCountOfPatients(PATIENT_NAME).intValue());
		patients = patientService.getPatients(PATIENT_NAME);
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1502));
		assertTrue(TestUtil.containsId(patients, 1503));
	}
	
	@Test
	public void getPatients_shouldReturnNoPatientsIfTheUserIsNotGrantedAccessToAnyBasis() {
		reloginAs("dBeckham", "test");
		updateSearchIndex();
		final int expCount = 0;
		assertEquals(expCount, patientService.getCountOfPatients(PATIENT_NAME).intValue());
		Collection<Patient> encounters = patientService.getPatients(PATIENT_NAME);
		assertEquals(expCount, encounters.size());
	}
	
}