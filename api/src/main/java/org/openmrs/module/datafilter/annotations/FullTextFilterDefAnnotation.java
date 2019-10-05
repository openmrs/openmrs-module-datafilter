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

import org.hibernate.search.annotations.FilterCacheModeType;
import org.hibernate.search.annotations.FullTextFilterDef;

/**
 * An instance of this class represents a {@link FullTextFilterDef} annotation to be added to a
 * persistent class mapped with annotations and supports hibernate full text search.
 */
public class FullTextFilterDefAnnotation implements FullTextFilterDef {
	
	private String name;
	
	private Class<?> implClass;
	
	private FilterCacheModeType cacheModeType = FilterCacheModeType.INSTANCE_AND_DOCIDSETRESULTS;
	
	public FullTextFilterDefAnnotation(String name, Class<?> implClass, String cacheModeType) {
		this.name = name;
		this.implClass = implClass;
		if (cacheModeType != null) {
			this.cacheModeType = FilterCacheModeType.valueOf(cacheModeType);
		}
	}
	
	/**
	 * @see FullTextFilterDef#name()
	 */
	@Override
	public String name() {
		return name;
	}
	
	/**
	 * @see FullTextFilterDef#impl()
	 */
	@Override
	public Class<?> impl() {
		return implClass;
	}
	
	/**
	 * @see FullTextFilterDef#cache()
	 */
	@Override
	public FilterCacheModeType cache() {
		return cacheModeType;
	}
	
	/**
	 * @see FullTextFilterDef#annotationType()
	 */
	@Override
	public Class<? extends Annotation> annotationType() {
		return FullTextFilterDef.class;
	}
	
}
