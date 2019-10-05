/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.registration;

import java.util.ArrayList;
import java.util.List;

public abstract class FilterRegistration<T extends FilterParameter> {
	
	private String name;
	
	private Class targetClass;
	
	private List<T> parameters = new ArrayList();
	
	/**
	 * Gets the name
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the names
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
	public Class getTargetClass() {
		return targetClass;
	}
	
	/**
	 * Sets the targetClass
	 *
	 * @param targetClass the targetClass to set
	 */
	public void setTargetClass(Class targetClass) {
		this.targetClass = targetClass;
	}
	
	/**
	 * Gets the parameters
	 *
	 * @return the parameters
	 */
	public List<T> getParameters() {
		return parameters;
	}
	
	/**
	 * Sets the parameters
	 *
	 * @param parameters the parameters to set
	 */
	public void setParameters(List<T> parameters) {
		this.parameters = parameters;
	}
	
}
