/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.web.UserFormSubmissionHandler;
import org.openmrs.web.WebConstants;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;

import java.io.IOException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class SubmissionFilterTest {
	
	@Mock
	private FilterChain filterChain;
	
	private MockHttpServletRequest httpServletRequest;
	
	private MockHttpServletResponse httpServletResponse;
	
	@Mock
	private UserFormSubmissionHandler handler;
	
	private SubmissionFilter submissionFilter;
	
	@Before
	public void setUp() throws Exception {
		initMocks(this);
		httpServletRequest = new MockHttpServletRequest();
		httpServletResponse = new MockHttpServletResponse();
		
		PowerMockito.mockStatic(Context.class);
		submissionFilter = new SubmissionFilter();
	}
	
	@Test
	public void doFilterToThrowExceptionWhenErrorOnHandleRequest() throws Exception {
		when(Context.getRegisteredComponent("userFormSubmissionHandler", UserFormSubmissionHandler.class))
		        .thenReturn(handler);
		
		doThrow(new IOException()).when(handler).handle(httpServletRequest, httpServletResponse, filterChain);
		
		submissionFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
		Assert.assertEquals("Error while processing user form",
		    httpServletRequest.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR));
	}
	
	@Test
	public void doFilterToThrowExceptionWhenRegisteredComponentIsNotPresent() {
		submissionFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
		Assert.assertEquals("Error while processing user form",
		    httpServletRequest.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR));
	}
}
