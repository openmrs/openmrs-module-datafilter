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

import org.junit.Before;

public abstract class BaseEncTypeViewPrivilegeBasedFilterTest extends BaseFilterTest {
	
	protected final static String PRIV_MANAGE_CHEMO_PATIENTS = "Manage Chemo Patients";
	
	@Before
	public void beforePrivilegeBasedFilterMethod() {
		DataFilterTestUtils.disableLocationFiltering();
	}
	
}
