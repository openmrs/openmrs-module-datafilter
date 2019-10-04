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

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/**
 * An instance of this class represents a {@link FilterDef} annotation to be added to a persistent
 * class mapped with annotations.
 */
public class FilterDefAnnotation implements FilterDef {
	
	private String name;
	
	private String defaultCondition;
	
	private ParamDef[] paramDefs;
	
	/**
	 * Constructor
	 *
	 * @param name the name of the filter def
	 * @param defaultCondition the default condition for the filter def
	 * @param paramDefs the array of @{@link ParamDef}s to add.
	 */
	public FilterDefAnnotation(String name, String defaultCondition, ParamDef[] paramDefs) {
		this.name = name;
		this.defaultCondition = StringUtils.isNotBlank(defaultCondition) ? defaultCondition : "";
		this.paramDefs = paramDefs != null ? paramDefs : new ParamDef[] {};
	}
	
	/**
	 * @see FilterDef#name()
	 */
	@Override
	public String name() {
		return name;
	}
	
	/**
	 * @see FilterDef#annotationType()
	 */
	@Override
	public Class<? extends Annotation> annotationType() {
		return FilterDef.class;
	}
	
	/**
	 * @see FilterDef#defaultCondition()
	 */
	@Override
	public String defaultCondition() {
		return defaultCondition;
	}
	
	/**
	 * @see FilterDef#parameters()
	 */
	@Override
	public ParamDef[] parameters() {
		return paramDefs;
	}
	
}
