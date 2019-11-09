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
import org.openmrs.module.datafilter.FilterTestUtils;

public abstract class BaseProgramBasedFilterTest extends BaseFilterTest {
	
	protected final static String ROLE_COORDINATOR_PROG_1 = "Program 1 Coordinator";
	
	@Before
	public void beforeProgramBasedFilterMethod() {
		FilterTestUtils.disableAllHibernateFilters();
		FilterTestUtils.enableFilter(ImplConstants.PROGRAM_BASED_FILTER_NAME_USER);
		FilterTestUtils.enableFilter(ImplConstants.PROGRAM_BASED_FILTER_NAME_PROVIDER);
	}
	
}
