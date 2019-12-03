/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.impl.api.db.hibernate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openmrs.module.datafilter.DataFilterConstants.MODULE_ID;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.AssertionFailure;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.DatabaseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class PatientLocationLinkingInterceptorTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private LocationService locationService;
	
	@Rule
	public ExpectedException ee = ExpectedException.none();
	
	@Override
	public void updateSearchIndex() {
		//Do nothing
	}
	
	private Patient createTestPatient() {
		Patient patient = new Patient();
		patient.addIdentifier(new PatientIdentifier("123", new PatientIdentifierType(2), new Location(1)));
		
		PersonName pName = new PersonName();
		pName.setGivenName("Lazy");
		pName.setFamilyName("Developer");
		patient.addName(pName);
		
		patient.setBirthdate(new Date());
		patient.setBirthdateEstimated(true);
		patient.setGender("M");
		
		return patient;
	}
	
	private List<String> getPatientLocations(Patient p) {
		List<List<Object>> rows = DatabaseUtil.executeSQL(getConnection(),
		    "SELECT DISTINCT basis_identifier FROM " + MODULE_ID + "_entity_basis_map WHERE entity_identifier='" + p.getId()
		            + "' AND entity_type='" + Patient.class.getName() + "' AND basis_type='" + Location.class.getName()
		            + "'",
		    false);
		
		List<String> locations = new ArrayList();
		for (List<Object> columns : rows) {
			locations.add(columns.get(0).toString());
		}
		
		return locations;
	}
	
	private Long getCountOfPatientLocationLinks() {
		List<List<Object>> rows = DatabaseUtil.executeSQL(getConnection(),
		    "SELECT COUNT(*) FROM " + MODULE_ID + "_entity_basis_map WHERE entity_type='" + Patient.class.getName()
		            + "' AND basis_type='" + Location.class.getName() + "'",
		    false);
		
		return (Long) rows.get(0).get(0);
	}
	
	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void onSave_shouldCreateAnEntryInTheEntityBasisMapTableBetweenThePatientAndSessionLocation() throws Exception {
		Patient patient = createTestPatient();
		final Integer locationId = 1;
		Context.getUserContext().setLocation(new Location(locationId));
		long originalCount = getCountOfPatientLocationLinks();
		patientService.savePatient(patient);
		
		List<String> patientLocations = getPatientLocations(patient);
		assertEquals(++originalCount, getCountOfPatientLocationLinks().intValue());
		assertEquals(1, patientLocations.size());
		assertTrue(patientLocations.contains(locationId.toString()));
	}
	
	//@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void onSave_shouldFailIfThereIsNoSessionLocation() {
		Patient patient = createTestPatient();
		ee.expect(AssertionFailure.class);
		ee.expectMessage(containsString(("Unable to perform beforeTransactionCompletion callback")));
		ee.expectCause(isA(DAOException.class));
		patientService.savePatient(patient);
	}
	
}
