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
import org.openmrs.Diagnosis;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class DiagnosisLocationBasedFilterTest extends BaseFilterTest {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private DataFilterService service;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "encounters.xml");
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "diagnoses.xml");
	}
	
	protected List<Diagnosis> getDiagnoses() {
		return sessionFactory.getCurrentSession().createCriteria(Diagnosis.class).list();
	}
	
	@Test
	public void getDiagnosis_shouldReturnNoDiagnosisIfTheUserIsNotGrantedAccessToAnyBasis() {
		reloginAs("dBeckham", "test");
		assertEquals(0, getDiagnoses().size());
	}
	
	@Test
	public void getDiagnosis_shouldReturnDiagnosesBelongingToPatientsAccessibleToTheUser() {
		reloginAs("dyorke", "test");
		int expCount = 2;
		Collection<Diagnosis> diagnoses = getDiagnoses();
		assertEquals(expCount, diagnoses.size());
		assertTrue(TestUtil.containsId(diagnoses, 1001));
		assertTrue(TestUtil.containsId(diagnoses, 1002));
		
		service.grantAccess(Context.getAuthenticatedUser(), new Location(4001));
		expCount = 3;
		diagnoses = getDiagnoses();
		assertEquals(expCount, diagnoses.size());
		assertTrue(TestUtil.containsId(diagnoses, 1001));
		assertTrue(TestUtil.containsId(diagnoses, 1002));
		assertTrue(TestUtil.containsId(diagnoses, 1003));
	}
	
	@Test
	public void getDiagnosis_shouldReturnAllDiagnosesIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		int expCount = 3;
		Collection<Diagnosis> diagnoses = getDiagnoses();
		assertEquals(expCount, diagnoses.size());
		assertTrue(TestUtil.containsId(diagnoses, 1001));
		assertTrue(TestUtil.containsId(diagnoses, 1002));
		assertTrue(TestUtil.containsId(diagnoses, 1003));
	}
	
	@Test
	public void getDiagnosis_shouldReturnAllDiagnosesIfLocationFilteringIsDisabled() {
		DataFilterTestUtils.disableLocationFiltering();
		reloginAs("dyorke", "test");
		assertEquals(3, getDiagnoses().size());
	}
	
}
