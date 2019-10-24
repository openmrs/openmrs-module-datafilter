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

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.registration.FilterRegistration;

public class FilterTestUtils {
	
	public static void disableAllHibernateFilters() {
		for (FilterRegistration registration : Util.getHibernateFilterRegistrations()) {
			AdministrationService as = Context.getAdministrationService();
			as.setGlobalProperty(registration.getName() + ".disabled", "true");
			Context.flushSession();
		}
	}
	
	public static void enableFilter(String filterName) {
		AdministrationService as = Context.getAdministrationService();
		as.setGlobalProperty(filterName + ".disabled", null);
		Context.flushSession();
	}
	
	public static void disableFilter(String filterName) {
		AdministrationService as = Context.getAdministrationService();
		as.setGlobalProperty(filterName + ".disabled", "true");
		Context.flushSession();
	}
	
}
