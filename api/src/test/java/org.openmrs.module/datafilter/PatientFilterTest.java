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
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class PatientFilterTest extends BaseFilterTest {
	
	@Autowired
	private PatientService patientService;
	
	private static final String PATIENT_NAME = "Magidu";
	
	private static final String IDENTIFIER_PREFIX = "M15";
	
	private static final String TELEPHONE_AREA_CODE = "317";
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "patients.xml");
		updateSearchIndex();
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
		expCount = 6;
		assertEquals(expCount, patientService.getAllPatients().size());
		patients = patientService.getAllPatients();
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1001));
		assertTrue(TestUtil.containsId(patients, 1002));
        assertTrue(TestUtil.containsId(patients, 1003));
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1502));
		assertTrue(TestUtil.containsId(patients, 1503));
	}
	
	@Test
	public void getAllPatients_shouldReturnNoPatientsIfTheUserIsNotGrantedAccessToAnyBasis() {
		reloginAs("dBeckham", "test");
		assertEquals(0, patientService.getAllPatients().size());
	}
	
	@Test
	public void getPatients_shouldReturnNoPatientsIfTheUserIsNotGrantedAccessToAnyBasis() {
		Context.getAdministrationService().setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		reloginAs("dBeckham", "test");
		final int expCount = 0;
		assertEquals(expCount, patientService.getCountOfPatients(PATIENT_NAME).intValue());
		assertEquals(expCount, patientService.getPatients(PATIENT_NAME).size());
		assertEquals(expCount, patientService.getCountOfPatients(IDENTIFIER_PREFIX).intValue());
		assertEquals(expCount, patientService.getPatients(IDENTIFIER_PREFIX).size());
		assertEquals(expCount, patientService.getCountOfPatients(TELEPHONE_AREA_CODE).intValue());
		assertEquals(expCount, patientService.getPatients(TELEPHONE_AREA_CODE).size());
	}
	
	@Test
	public void getPatients_shouldReturnPatientsByNameAccessibleToTheUser() {
		reloginAs("dyorke", "test");
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
	public void getPatients_shouldReturnPatientsByIdentifierAccessibleToTheUser() {
		reloginAs("dyorke", "test");
		int expCount = 2;
		assertEquals(expCount, patientService.getCountOfPatients(IDENTIFIER_PREFIX).intValue());
		Collection<Patient> patients = patientService.getPatients(IDENTIFIER_PREFIX);
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1503));
		
		AccessUtilTest.grantLocationAccessToUser(Context.getAuthenticatedUser().getUserId(), 4001, getConnection());
		expCount = 3;
		assertEquals(expCount, patientService.getCountOfPatients(IDENTIFIER_PREFIX).intValue());
		patients = patientService.getPatients(IDENTIFIER_PREFIX);
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1502));
		assertTrue(TestUtil.containsId(patients, 1503));
	}
	
	@Test
	public void getPatients_shouldReturnPatientsByPersonAttributeAccessibleToTheUser() {
		Context.getAdministrationService().setGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_MODE,
		    OpenmrsConstants.GLOBAL_PROPERTY_PERSON_ATTRIBUTE_SEARCH_MATCH_ANYWHERE);
		reloginAs("dyorke", "test");
		int expCount = 2;
		assertEquals(expCount, patientService.getCountOfPatients(TELEPHONE_AREA_CODE).intValue());
		Collection<Patient> patients = patientService.getPatients(TELEPHONE_AREA_CODE);
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1503));
		
		AccessUtilTest.grantLocationAccessToUser(Context.getAuthenticatedUser().getUserId(), 4001, getConnection());
		expCount = 3;
		assertEquals(expCount, patientService.getCountOfPatients(TELEPHONE_AREA_CODE).intValue());
		patients = patientService.getPatients(TELEPHONE_AREA_CODE);
		assertEquals(expCount, patients.size());
		assertTrue(TestUtil.containsId(patients, 1501));
		assertTrue(TestUtil.containsId(patients, 1502));
		assertTrue(TestUtil.containsId(patients, 1503));
	}
	
}
