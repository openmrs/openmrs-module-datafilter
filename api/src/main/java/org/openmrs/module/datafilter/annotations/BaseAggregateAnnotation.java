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

import java.lang.reflect.Array;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Super interface for all aggregate annotations.
 * 
 * @param <A>
 * @see AggregateAnnotation
 */
public abstract class BaseAggregateAnnotation<A> implements AggregateAnnotation<A> {
	
	protected A[] group = (A[]) Array.newInstance(getGroupedAnnotationType(), 0);
	
	/**
	 * @see AggregateAnnotation#add(Object)
	 */
	@Override
	public void add(A annotation) {
		group = ArrayUtils.add(group, annotation);
	}
	
}
