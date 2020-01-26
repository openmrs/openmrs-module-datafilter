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

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.search.FullTextQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.FullTextQueryAndEntityClass;
import org.openmrs.api.db.FullTextQueryCreatedEvent;
import org.openmrs.module.datafilter.registration.FullTextFilterRegistration;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Util.class, Context.class })
public class FullTextQueryCreatedEventListenerTest {
	
	@Mock
	private FullTextQuery fullTextQuery;
	
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void onApplicationEvent_shouldNotEnableFiltersForWhichTheAuthenticatedUserHasASpecificByPassPrivilege() {
		mockStatic(Util.class);
		mockStatic(Context.class);
		final String filter1 = "filter1";
		final String filter2 = "filter2";
		final Class clazz = PersonName.class;
		FullTextFilterRegistration filterReg1 = new FullTextFilterRegistration();
		filterReg1.setName(filter1);
		filterReg1.setTargetClasses(Collections.singletonList(clazz));
		FullTextFilterRegistration filterReg2 = new FullTextFilterRegistration();
		filterReg2.setName(filter2);
		filterReg2.setTargetClasses(Collections.singletonList(clazz));
		List<FullTextFilterRegistration> filters = Stream.of(filterReg1, filterReg2).collect(Collectors.toList());
		when(Util.getFullTextFilterRegistrations()).thenReturn(filters);
		when(Util.skipFilter(anyString())).thenCallRealMethod();
		when(Context.isAuthenticated()).thenReturn(true);
		when(Context.hasPrivilege(eq(filter1 + DataFilterConstants.BYPASS_PRIV_SUFFIX))).thenReturn(true);
		
		new FullTextQueryCreatedEventListener()
		        .onApplicationEvent(new FullTextQueryCreatedEvent(new FullTextQueryAndEntityClass(fullTextQuery, clazz)));
		
		verify(fullTextQuery, never()).enableFullTextFilter(eq(filter1));
		verify(fullTextQuery, times(1)).enableFullTextFilter(eq(filter2));
	}
	
}
