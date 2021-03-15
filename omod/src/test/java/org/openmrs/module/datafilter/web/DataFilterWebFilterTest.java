/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.web;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.servlet.FilterChain;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.datafilter.DataFilterSessionContext;
import org.powermock.reflect.Whitebox;

public class DataFilterWebFilterTest {
	
	@Mock
	private FilterChain filterChain;
	
	@Test
	public void doFilter_shouldAlwaysResetTheFiltersOnTheCurrentThread() throws Exception {
		MockitoAnnotations.initMocks(this);
		ThreadLocal areFiltersSet = new ThreadLocal();
		areFiltersSet.set(true);
		Whitebox.setInternalState(DataFilterSessionContext.class, "areFiltersSet", areFiltersSet);
		// FIXE: No idea why the bellow assert is failing
		assertNotNull(((ThreadLocal) Whitebox.getInternalState(DataFilterSessionContext.class, "areFiltersSet")).get());
		
		new DataFilterWebFilter().doFilter(null, null, filterChain);
		
		assertNull(((ThreadLocal) Whitebox.getInternalState(DataFilterSessionContext.class, "areFiltersSet")).get());
	}
	
}
