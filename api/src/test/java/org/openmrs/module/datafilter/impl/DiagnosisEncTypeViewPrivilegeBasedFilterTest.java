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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Diagnosis;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class DiagnosisEncTypeViewPrivilegeBasedFilterTest extends BaseEncTypeViewPrivilegeBasedFilterTest {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Before
	public void before() {
		executeDataSet(TestConstants.ROOT_PACKAGE_DIR + "privilegedEncounters.xml");
	}
	
	protected List<Diagnosis> getDiagnoses() {
		return sessionFactory.getCurrentSession().createCriteria(Diagnosis.class).list();
	}
	
	@Test
	public void getDiagnosis_shouldIncludeDiagnosesLinkedToEncountersThatRequireAPrivilegeAndTheUserHasIt() {
		reloginAs("dyorke", "test");
		assertFalse(Context.getAuthenticatedUser().hasPrivilege(PRIV_MANAGE_CHEMO_PATIENTS));
		int expCount = 0;
		Collection<Diagnosis> diagnoses = getDiagnoses();
		assertEquals(expCount, diagnoses.size());
		
		DataFilterTestUtils.addPrivilege(PRIV_MANAGE_CHEMO_PATIENTS);
		expCount = 1;
		diagnoses = getDiagnoses();
		assertEquals(expCount, diagnoses.size());
		assertTrue(TestUtil.containsId(diagnoses, 1004));
	}
	
	@Test
	public void getDiagnosis_shouldReturnAllDiagnosesIfTheAuthenticatedUserIsASuperUser() {
		assertTrue(Context.getAuthenticatedUser().isSuperUser());
		Collection<Diagnosis> diagnoses = getDiagnoses();
		assertEquals(1, diagnoses.size());
		assertTrue(TestUtil.containsId(diagnoses, 1004));
	}
	
	@Test
	public void getDiagnosis_shouldReturnAllDiagnosesIfEncTypeViewPrivFilteringIsDisabled() {
		DataFilterTestUtils.disableEncTypeViewPrivilegeFiltering();
		Collection<Diagnosis> diagnoses = getDiagnoses();
		assertEquals(1, diagnoses.size());
		assertTrue(TestUtil.containsId(diagnoses, 1004));
	}
	
}
