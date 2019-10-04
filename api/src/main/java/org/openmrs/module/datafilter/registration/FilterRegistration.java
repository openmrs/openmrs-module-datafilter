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

public class FilterRegistration {
	
	private String name;
	
	private Class targetClass;
	
	private String property;
	
	private String defaultCondition = "";
	
	private String condition = defaultCondition;
	
	private List<FilterParameter> parameters = new ArrayList();
	
	private boolean deduceAliasInjectionPoints = true;
	
	private List<FilterAlias> aliases = new ArrayList();
	
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
	public Class getTargetClass() {
		return targetClass;
	}
	
	/**
	 * Gets the property
	 *
	 * @return the property
	 */
	public String getProperty() {
		return property;
	}
	
	/**
	 * Sets the property
	 *
	 * @param property the property to set
	 */
	public void setProperty(String property) {
		this.property = property;
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
	 * Gets the defaultCondition
	 *
	 * @return the defaultCondition
	 */
	public String getDefaultCondition() {
		return defaultCondition;
	}
	
	/**
	 * Sets the defaultCondition
	 *
	 * @param defaultCondition the defaultCondition to set
	 */
	public void setDefaultCondition(String defaultCondition) {
		this.defaultCondition = defaultCondition;
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
	
	/**
	 * Gets the deduceAliasInjectionPoints
	 *
	 * @return the deduceAliasInjectionPoints
	 */
	public boolean isDeduceAliasInjectionPoints() {
		return deduceAliasInjectionPoints;
	}
	
	/**
	 * Sets the deduceAliasInjectionPoints
	 *
	 * @param deduceAliasInjectionPoints the deduceAliasInjectionPoints to set
	 */
	public void setDeduceAliasInjectionPoints(boolean deduceAliasInjectionPoints) {
		this.deduceAliasInjectionPoints = deduceAliasInjectionPoints;
	}
	
	/**
	 * Gets the aliases
	 *
	 * @return the aliases
	 */
	public List<FilterAlias> getAliases() {
		return aliases;
	}
	
	/**
	 * Sets the aliases
	 *
	 * @param aliases the aliases to set
	 */
	public void setAliases(List<FilterAlias> aliases) {
		this.aliases = aliases;
	}
	
}
