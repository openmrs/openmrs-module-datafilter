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

import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.PrivilegeConstants;

public abstract class BaseDataFilterTest extends BaseModuleContextSensitiveTest {
	
	@Override
	public void updateSearchIndex() {
		//Disable the interceptor so we can update the search index
		boolean resetToStrict = false;
		String originalValue = null;
		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		AdministrationService as = Context.getAdministrationService();
		GlobalProperty gp = as.getGlobalPropertyObject(LocationBasedAccessConstants.GP_RUN_IN_STRICT_MODE);
		if (gp == null) {
			gp = new GlobalProperty(LocationBasedAccessConstants.GP_RUN_IN_STRICT_MODE);
		} else {
			originalValue = gp.getPropertyValue();
			if ("true".equalsIgnoreCase(gp.getPropertyValue())) {
				resetToStrict = true;
			}
		}
		gp.setPropertyValue("false");
		as.saveGlobalProperty(gp);
		Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		Context.flushSession();
		
		try {
			super.updateSearchIndex();
		}
		finally {
			if (resetToStrict) {
				Context.addProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
				gp.setPropertyValue(originalValue);
				as.saveGlobalProperty(gp);
				Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
				Context.flushSession();
			}
		}
	}
}
