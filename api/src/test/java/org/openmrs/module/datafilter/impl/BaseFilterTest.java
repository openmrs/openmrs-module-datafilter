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

import org.hibernate.cfg.Environment;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.module.datafilter.DataFilterSessionContext;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.Util;
import org.openmrs.module.datafilter.annotations.FilterDefsAnnotation;
import org.openmrs.module.datafilter.annotations.FiltersAnnotation;
import org.openmrs.module.datafilter.annotations.FullTextFilterDefsAnnotation;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.PrivilegeConstants;

public abstract class BaseFilterTest extends BaseModuleContextSensitiveTest {
	
	@BeforeClass
	public static void beforeBaseFilterClass() throws ReflectiveOperationException {
		Util.addAnnotationToClass(Patient.class, new FilterDefsAnnotation());
		Util.addAnnotationToClass(Patient.class, new FiltersAnnotation());
		Util.addAnnotationToClass(PatientIdentifier.class, new FullTextFilterDefsAnnotation());
		Util.addAnnotationToClass(Visit.class, new FilterDefsAnnotation());
		Util.addAnnotationToClass(Visit.class, new FiltersAnnotation());
		Util.addAnnotationToClass(Encounter.class, new FilterDefsAnnotation());
		Util.addAnnotationToClass(Encounter.class, new FiltersAnnotation());
		Util.addAnnotationToClass(Obs.class, new FilterDefsAnnotation());
		Util.addAnnotationToClass(Obs.class, new FiltersAnnotation());
		Util.setupFilters();
		Context.addConfigProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, DataFilterSessionContext.class.getName());
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
		boolean resetToStrict = false;
		String originalValue = null;
		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		AdministrationService as = Context.getAdministrationService();
		GlobalProperty gp = as.getGlobalPropertyObject(ImplConstants.GP_RUN_IN_STRICT_MODE);
		if (gp == null) {
			gp = new GlobalProperty(ImplConstants.GP_RUN_IN_STRICT_MODE);
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
