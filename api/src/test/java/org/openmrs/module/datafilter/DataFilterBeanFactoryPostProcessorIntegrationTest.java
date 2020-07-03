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

import java.util.Set;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.openmrs.module.datafilter.impl.BaseFilterTest;
import org.springframework.beans.factory.annotation.Autowired;

public class DataFilterBeanFactoryPostProcessorIntegrationTest extends BaseFilterTest {
	
	private static final String[] testXMlFilters = new String[] { "datafilter_locationFilter1", "datafilter_locationFilter2",
	        "datafilter_CareSettingFilter" };
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Test
	public void postProcessBeanFactory_shouldRegisterFiltersToHbmFiles() {
		assertEquals(15, Util.getHibernateFilterRegistrations().size());
		Set<String> registeredFilters = sessionFactory.getDefinedFilterNames();
		assertEquals(14, registeredFilters.size());
		for (String filterName : testXMlFilters) {
			registeredFilters.contains(filterName);
		}
	}
	
}
