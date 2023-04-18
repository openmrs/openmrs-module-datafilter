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

import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import javax.servlet.FilterChain;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.datafilter.DataFilterSessionContext;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*" })
@PrepareForTest(DataFilterSessionContext.class)
public class DataFilterWebFilterTest {
	
	@Mock
	private FilterChain filterChain;
	
	@Test
	public void doFilter_shouldAlwaysResetTheFiltersOnTheCurrentThread() throws Exception {
		mockStatic(DataFilterSessionContext.class);
		try (AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
			AtomicBoolean wasCalled = new AtomicBoolean(false);
			doAnswer((invocation) -> {
				wasCalled.set(true);
				return null;
			}).when(DataFilterSessionContext.class, "reset");
			
			new DataFilterWebFilter().doFilter(null, null, filterChain);
			
			assertTrue(wasCalled.get());
		}
	}
	
}
