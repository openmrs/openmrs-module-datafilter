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

import org.junit.Before;
import org.junit.BeforeClass;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.Util;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.PrivilegeConstants;

public abstract class BaseFilterTest extends BaseModuleContextSensitiveTest {
	
	@BeforeClass
	public static void beforeBaseFilterClass() throws ReflectiveOperationException {
		Util.initializeFilters();
	}
	
	@Before
	public void beforeTestMethod() {
		executeDataSet(TestConstants.MODULE_TEST_DATASET_XML);
	}
	
	protected void reloginAs(String username, String password) {
		Context.logout();
		Context.authenticate(new UsernamePasswordCredentials(username, password));
	}
	
	@Override
	public void updateSearchIndex() {
		//Disable the interceptor so we can update the search index
		String originalValue = null;
		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		AdministrationService as = Context.getAdministrationService();
		GlobalProperty gp = as.getGlobalPropertyObject(ImplConstants.GP_RUN_IN_STRICT_MODE);
		if (gp == null) {
			gp = new GlobalProperty(ImplConstants.GP_RUN_IN_STRICT_MODE);
		} else {
			originalValue = gp.getPropertyValue();
		}
		
		gp.setPropertyValue("false");
		as.saveGlobalProperty(gp);
		Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		Context.flushSession();
		
		try {
			super.updateSearchIndex();
		}
		finally {
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
			gp.setPropertyValue(originalValue == null ? "" : originalValue);
			as.saveGlobalProperty(gp);
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
			Context.flushSession();
		}
	}
	
}
