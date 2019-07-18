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

import org.hibernate.cfg.Environment;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.module.datafilter.annotations.FilterDefsAnnotation;
import org.openmrs.module.datafilter.annotations.FiltersAnnotation;

public abstract class BaseFilterTest extends BaseDataFilterTest {
	
	@BeforeClass
	public static void beforeClass() throws ReflectiveOperationException {
		Util.addAnnotationToClass(Encounter.class, new FilterDefsAnnotation());
		Util.addAnnotationToClass(Encounter.class, new FiltersAnnotation());
		Util.configureLocationBasedFiltering();
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
	
}
