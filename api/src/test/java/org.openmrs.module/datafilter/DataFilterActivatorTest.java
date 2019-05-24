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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.util.OpenmrsClassScanner;

public class DataFilterActivatorTest {
	
	private DataFilterActivator activator = new DataFilterActivator();
	
	private Class<?>[] filteredEntityClasses = new Class[] { Encounter.class };
	
	private OpenmrsClassScanner classScanner = OpenmrsClassScanner.getInstance();
	
	@Test
	public void willStart_shouldAddFilterAnnotationsToTheFilteredEntityClasses() {
		Class<? extends Annotation> annotationClass = FilterDef.class;
		for (Class<?> clazz : filteredEntityClasses) {
			assertFalse(clazz.isAnnotationPresent(annotationClass));
		}
		
		activator.willStart();
		
		annotationClass = Filter.class;
		for (Class<?> clazz : filteredEntityClasses) {
			assertTrue(clazz.isAnnotationPresent(annotationClass));
		}
	}
	
}
