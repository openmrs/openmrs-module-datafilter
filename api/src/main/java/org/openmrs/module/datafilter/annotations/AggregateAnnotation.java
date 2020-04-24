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

/**
 * Super interface for all aggregate annotations i.e a container annotation that aggregates several
 * other annotations e.g. {@link org.hibernate.annotations.FilterDefs},
 * {@link org.hibernate.annotations.Filters} etc.
 */
public interface AggregateAnnotation<A> {
	
	/**
	 * Adds an annotation to this annotation's array of the grouped annotations.
	 * 
	 * @param toAdd the annotation to add
	 */
	void add(A toAdd);
	
	/**
	 * Subclasses need to implement this method to return the type of the annotation they group.
	 */
	Class<A> getGroupedAnnotationType();
	
}
