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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.Session;
import org.hibernate.context.spi.CurrentSessionContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.impl.BaseFilterTest;
import org.openmrs.module.datafilter.registration.HibernateFilterRegistration;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;

public class DataFilterSessionContextSensitiveTest extends BaseFilterTest {
	
	@Autowired
	private SessionFactoryImplementor sessionFactory;
	
	private CurrentSessionContext currentSessionContext;
	
	@Before
	public void setup() {
		if (currentSessionContext == null) {
			currentSessionContext = Whitebox.getInternalState(sessionFactory, CurrentSessionContext.class);
		}
	}
	
	@Test
	public void currentSession_shouldNotEnableFiltersForWhichTheAuthenticatedUserHasASpecificByPassPrivilege() {
		reloginAs("dyorke", "test");
		List<String> disabledFilters = Stream
		        .of("datafilter_locationBasedVisitFilter", "datafilter_locationBasedEncounterFilter")
		        .collect(Collectors.toList());
		disabledFilters
		        .forEach(filterName -> Context.addProxyPrivilege(filterName + DataFilterConstants.BYPASS_PRIV_SUFFIX));
		
		Session session = currentSessionContext.currentSession();
		
		List<HibernateFilterRegistration> filters = Util.getHibernateFilterRegistrations();
		List<String> enabledFilterNames = filters.stream().filter(f -> !disabledFilters.contains(f.getName()))
		        .map(f -> f.getName()).collect(Collectors.toList());
		enabledFilterNames.forEach(f -> assertNotNull(session.getEnabledFilter(f)));
		disabledFilters.forEach(f -> assertNull(session.getEnabledFilter(f)));
	}
	
}
