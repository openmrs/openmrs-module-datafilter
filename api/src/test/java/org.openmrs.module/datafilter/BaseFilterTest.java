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

import java.util.List;

import org.hibernate.cfg.Environment;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.DatabaseUtil;

public abstract class BaseFilterTest extends BaseModuleContextSensitiveTest {
	
	@BeforeClass
	public static void beforeClass() {
		Context.addConfigProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, DataFilterSessionContext.class.getName());
		Util.addFilterAnnotations();
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
		List<List<Object>> rows = DatabaseUtil.executeSQL(getConnection(),
		    "SELECT property_value FROM global_property WHERE property = '" + DataFilterConstants.GP_RUN_IN_STRICT_MODE
		            + "'",
		    true);
		if (rows.isEmpty()) {
			DatabaseUtil.executeSQL(getConnection(), "INSERT INTO global_property (property, property_value) VALUES ('"
			        + DataFilterConstants.GP_RUN_IN_STRICT_MODE + "', 'false')",
			    false);
		}
		
		try {
			super.updateSearchIndex();
		}
		finally {
			//DatabaseUtil.executeSQL(getConnection(), "UPDATE global_property SET property_value = '" + v
			//        + "' WHERE property= '" + DataFilterConstants.GP_RUN_IN_STRICT_MODE + "'",
			//   false);
		}
	}
}
