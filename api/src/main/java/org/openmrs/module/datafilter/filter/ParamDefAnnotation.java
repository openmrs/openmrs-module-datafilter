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

import org.hibernate.annotations.ParamDef;
import org.hibernate.type.StringType;
import org.openmrs.module.datafilter.DataFilterConstants;

/**
 * An instance of this class represents a {@link ParamDef} to be added to
 * {@link FilterDefAnnotation} instance
 */
public class ParamDefAnnotation implements ParamDef {
	
	/**
	 * The value of the patientIds param should be a comma separated list of patient ids to match
	 * 
	 * @see ParamDef#name()
	 */
	@Override
	public String name() {
		return DataFilterConstants.FILTER_PARAM_PATIENT_IDS;
	}
	
	/**
	 * @see ParamDef#type()
	 */
	@Override
	public String type() {
		return StringType.INSTANCE.getName();
	}
	
	/**
	 * @see ParamDef#annotationType()
	 */
	@Override
	public Class<? extends Annotation> annotationType() {
		return ParamDef.class;
	}
	
}
