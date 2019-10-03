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

import java.util.List;

public class FilterRegistration {
	
	private String name;
	
	private Class<?> targetClass;
	
	private String condition;
	
	private List<FilterParameter> parameters;
	
	/**
	 * Gets the name
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name
	 *
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the targetClass
	 *
	 * @return the targetClass
	 */
	public Class<?> getTargetClass() {
		return targetClass;
	}
	
	/**
	 * Sets the targetClass
	 *
	 * @param targetClass the targetClass to set
	 */
	public void setTargetClass(Class<?> targetClass) {
		this.targetClass = targetClass;
	}
	
	/**
	 * Gets the condition
	 *
	 * @return the condition
	 */
	public String getCondition() {
		return condition;
	}
	
	/**
	 * Sets the condition
	 *
	 * @param condition the condition to set
	 */
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	/**
	 * Gets the parameters
	 *
	 * @return the parameters
	 */
	public List<FilterParameter> getParameters() {
		return parameters;
	}
	
	/**
	 * Sets the parameters
	 *
	 * @param parameters the parameters to set
	 */
	public void setParameters(List<FilterParameter> parameters) {
		this.parameters = parameters;
	}
	
}
