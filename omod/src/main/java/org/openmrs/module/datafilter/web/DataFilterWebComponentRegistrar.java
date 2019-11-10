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

import static javax.servlet.DispatcherType.ERROR;
import static javax.servlet.DispatcherType.FORWARD;
import static javax.servlet.DispatcherType.REQUEST;

import java.util.EnumSet;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.stereotype.Component;

/**
 * Registers web components that typically are registered via web.xml e.g. filters, servlets and
 * listeners.
 */
@Component
public class DataFilterWebComponentRegistrar implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ServletContext servletContext = servletContextEvent.getServletContext();
		FilterRegistration filterReg = servletContext.addFilter("datafilter-Filter", new DataFilterWebFilter());
		//TODO Should ignore static content requests
		filterReg.addMappingForUrlPatterns(EnumSet.of(ERROR, FORWARD, REQUEST), true, "/*");
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {

	}
}
