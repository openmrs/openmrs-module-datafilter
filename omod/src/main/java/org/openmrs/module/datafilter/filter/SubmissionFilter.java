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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.LocationService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.web.UserFormSubmissionHandler;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.openmrs.web.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubmissionFilter implements Filter {
	
	private static final Logger log = LoggerFactory.getLogger(SubmissionFilter.class);
	
	/**
	 * @see Filter#init(FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) {
		if (log.isDebugEnabled()) {
			log.debug("Initializing datafilter web filter....");
		}
	}
	
	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
		
		try {
			Context.getRegisteredComponent("userFormSubmissionHandler", UserFormSubmissionHandler.class).handle(request,
			    response, chain);
		}
		catch (Exception e) {
			log.error("Error while processing user form", e);
			((HttpServletRequest) request).getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
			    "Error while processing user form");
		}
	}
	
	/**
	 * @see Filter#destroy()
	 */
	@Override
	public void destroy() {
		if (log.isDebugEnabled()) {
			log.debug("Destroying datafilter web filter....");
		}
	}
	
}
