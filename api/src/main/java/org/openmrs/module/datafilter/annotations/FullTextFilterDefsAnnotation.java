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

import org.hibernate.annotations.FilterDefs;
import org.hibernate.search.annotations.FullTextFilterDef;
import org.hibernate.search.annotations.FullTextFilterDefs;

/**
 * An instance of this class represents a {@link FilterDefs} annotation to be added to a persistent
 * class mapped with annotations.
 */
public class FullTextFilterDefsAnnotation extends BaseAggregateAnnotation<FullTextFilterDef> implements FullTextFilterDefs {
	
	/**
	 * @see FullTextFilterDefs#value()
	 */
	@Override
	public FullTextFilterDef[] value() {
		return group;
	}
	
	/**
	 * @see FullTextFilterDefs#annotationType()
	 */
	@Override
	public Class<? extends Annotation> annotationType() {
		return FullTextFilterDefs.class;
	}
	
	/**
	 * @see BaseAggregateAnnotation#getGroupedAnnotationType()
	 */
	@Override
	public Class<FullTextFilterDef> getGroupedAnnotationType() {
		return FullTextFilterDef.class;
	}
}
