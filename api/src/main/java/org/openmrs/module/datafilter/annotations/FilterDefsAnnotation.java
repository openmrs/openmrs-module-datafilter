/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.annotations;

import java.lang.annotation.Annotation;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;

/**
 * An instance of this class represents a {@link FilterDefs} annotation to be added to a persistent
 * class mapped with annotations.
 */
public class FilterDefsAnnotation implements FilterDefs {
	
	private FilterDef[] filterDefs = new FilterDef[1];
	
	/**
	 * @see FilterDefs#value()
	 */
	@Override
	public FilterDef[] value() {
		return filterDefs;
	}
	
	/**
	 * @see FilterDefs#annotationType()
	 */
	@Override
	public Class<? extends Annotation> annotationType() {
		return FilterDefs.class;
	}
	
}
