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

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

/**
 * An instance of this class represents a {@link Filters} annotation to be added to a persistent
 * class mapped with annotations.
 */
public class FiltersAnnotation extends BaseAggregateAnnotation<Filter> implements Filters {
	
	/**
	 * @see Filters#value()
	 */
	@Override
	public Filter[] value() {
		return group;
	}
	
	/**
	 * @see Filters#annotationType()
	 */
	@Override
	public Class<? extends Annotation> annotationType() {
		return Filters.class;
	}
	
	/**
	 * @see BaseAggregateAnnotation#getGroupedAnnotationType()
	 */
	@Override
	public Class<Filter> getGroupedAnnotationType() {
		return Filter.class;
	}
}
