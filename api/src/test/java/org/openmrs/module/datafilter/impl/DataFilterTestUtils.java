/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.impl;

import static org.openmrs.module.datafilter.impl.ImplConstants.GP_ENC_TYPE_PRIV_BASED_FILTER_NAME_ENCOUNTER;
import static org.openmrs.module.datafilter.impl.ImplConstants.GP_ENC_TYPE_PRIV_BASED_FILTER_NAME_OBS;
import static org.openmrs.module.datafilter.impl.ImplConstants.GP_LOCATION_BASED_FILTER_NAME_ENCOUNTER;
import static org.openmrs.module.datafilter.impl.ImplConstants.GP_LOCATION_BASED_FILTER_NAME_OBS;
import static org.openmrs.module.datafilter.impl.ImplConstants.GP_LOCATION_BASED_FILTER_NAME_PATIENT;
import static org.openmrs.module.datafilter.impl.ImplConstants.GP_LOCATION_BASED_FILTER_NAME_PROVIDER;
import static org.openmrs.module.datafilter.impl.ImplConstants.GP_LOCATION_BASED_FILTER_NAME_USER;
import static org.openmrs.module.datafilter.impl.ImplConstants.GP_LOCATION_BASED_FILTER_NAME_VISIT;
import static org.openmrs.module.datafilter.impl.ImplConstants.GP_LOCATION_BASED_FULL_TEXT_FILTER_PATIENT;
import static org.openmrs.module.datafilter.impl.ImplConstants.GP_PROGRAM_BASED_FILTER_NAME_PROVIDER;
import static org.openmrs.module.datafilter.impl.ImplConstants.GP_PROGRAM_BASED_FILTER_NAME_USER;

import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;

public class DataFilterTestUtils {
	
	public static void disableLocationFiltering() {
		AdministrationService as = Context.getAdministrationService();
		as.setGlobalProperty(GP_LOCATION_BASED_FULL_TEXT_FILTER_PATIENT, "true");
		as.setGlobalProperty(GP_LOCATION_BASED_FILTER_NAME_PATIENT, "true");
		as.setGlobalProperty(GP_LOCATION_BASED_FILTER_NAME_VISIT, "true");
		as.setGlobalProperty(GP_LOCATION_BASED_FILTER_NAME_ENCOUNTER, "true");
		as.setGlobalProperty(GP_LOCATION_BASED_FILTER_NAME_OBS, "true");
		as.setGlobalProperty(GP_LOCATION_BASED_FILTER_NAME_USER, "true");
		as.setGlobalProperty(GP_LOCATION_BASED_FILTER_NAME_PROVIDER, "true");
		Context.flushSession();
	}
	
	public static void disableProgramBasedFiltering() {
		AdministrationService as = Context.getAdministrationService();
		as.setGlobalProperty(GP_PROGRAM_BASED_FILTER_NAME_USER, "true");
		as.setGlobalProperty(GP_PROGRAM_BASED_FILTER_NAME_PROVIDER, "true");
		Context.flushSession();
	}
	
	public static void disableEncTypeViewPrivilegeFiltering() {
		AdministrationService as = Context.getAdministrationService();
		as.setGlobalProperty(GP_ENC_TYPE_PRIV_BASED_FILTER_NAME_OBS, "true");
		as.setGlobalProperty(GP_ENC_TYPE_PRIV_BASED_FILTER_NAME_ENCOUNTER, "true");
		Context.flushSession();
	}
	
	public static void addPrivilege(String privilege) {
		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
		try {
			Privilege p = Context.getUserService().getPrivilege(privilege);
			Role role = Context.getAuthenticatedUser().getRoles().iterator().next();
			role.addPrivilege(p);
			Context.getUserService().saveRole(role);
			Context.flushSession();
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ROLES);
		}
	}
	
}
