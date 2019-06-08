/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.filter;

import java.lang.annotation.Annotation;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.openmrs.module.datafilter.DataFilterConstants;

/**
 * An instance of this class represents a {@link FilterDef} to be added to an annotated persistent
 * class
 */
public class FilterDefAnnotation implements FilterDef {
	
	ParamDefAnnotation attribTypeParamDef = new ParamDefAnnotation(DataFilterConstants.PARAM_NAME_ATTRIB_TYPE_ID,
	        IntegerType.INSTANCE.getName());
	
	ParamDefAnnotation basisIdsParamDef = new ParamDefAnnotation(DataFilterConstants.PARAM_NAME_BASIS_IDS,
	        StringType.INSTANCE.getName());
	
	private String name;
	
	/**
	 * Constructor
	 *
	 * @param name the name of the filter def
	 */
	public FilterDefAnnotation(String name) {
		this.name = name;
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
		return null;
	}
	
	/**
	 * @see FilterDef#parameters()
	 */
	@Override
	public ParamDef[] parameters() {
		return new ParamDef[] { attribTypeParamDef, basisIdsParamDef };
	}
	
}
