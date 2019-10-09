/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.location;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.appframework.LoginLocationFilter;
import org.openmrs.module.datafilter.DataFilterConstants;
import org.springframework.stereotype.Component;

@Component(DataFilterConstants.MODULE_ID + "LoginLocationFilter")
public class DataFilterLoginLocationFilter implements LoginLocationFilter {
	
	public static final String GP_LOGIN_LOCATION_USER_PROPERTY = "referenceapplication.locationUserPropertyName";
	
	/**
	 * @see LoginLocationFilter#accept(Location)
	 */
	@Override
	public boolean accept(Location location) {
		if (Daemon.isDaemonThread()) {
			return true;
		}
		
		if (StringUtils.isBlank(Context.getAdministrationService().getGlobalProperty(GP_LOGIN_LOCATION_USER_PROPERTY))) {
			return true;
		}
		
		if (!Context.isAuthenticated() || Context.getAuthenticatedUser() == null) {
			return false;
		}
		
		if (Context.getAuthenticatedUser().isSuperUser()) {
			return true;
		}
		
		return AccessUtil.getAssignedBasisIds(Location.class).contains(location.getId().toString());
	}
	
}
