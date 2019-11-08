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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.openmrs.module.datafilter.DataFilterSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The job of this filter is to clear the thread local variable in {@link DataFilterSessionContext}
 * class so that we don't have stale session filter settings shared between different http requests
 * in a servlet container environment where threads are reused from a thread pool.
 */
public class DataFilterWebFilter implements Filter {
	
	private static final Logger log = LoggerFactory.getLogger(DataFilterWebFilter.class);
	
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
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	    throws IOException, ServletException {
		
		if (log.isTraceEnabled()) {
			log.trace("In datafilter web filter....");
		}
		
		try {
			chain.doFilter(request, response);
		}
		finally {
			if (log.isTraceEnabled()) {
				log.trace("Http request processing ended....");
			}
			
			DataFilterSessionContext.reset();
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
