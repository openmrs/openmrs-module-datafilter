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

import java.util.List;

public abstract class FilterRegistration<T extends FilterParameter> {
	
	private String name;
	
	private List<Class> targetClasses;
	
	private List<T> parameters;
	
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
	 * Gets the targetClasses
	 *
	 * @return the targetClasses
	 */
	public List<Class> getTargetClasses() {
		return targetClasses;
	}
	
	/**
	 * Sets the targetClasses
	 *
	 * @param targetClasses the targetClasses to set
	 */
	public void setTargetClasses(List<Class> targetClasses) {
		this.targetClasses = targetClasses;
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
