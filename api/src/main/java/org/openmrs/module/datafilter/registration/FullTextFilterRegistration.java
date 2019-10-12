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

public class FullTextFilterRegistration extends FilterRegistration<FilterParameter> {
	
	private Class implClass;
	
	private String cacheMode;
	
	/**
	 * Gets the implClass
	 *
	 * @return the implClass
	 */
	public Class getImplClass() {
		return implClass;
	}
	
	/**
	 * Sets the implClass
	 *
	 * @param implClass the implClass to set
	 */
	public void setImplClass(Class implClass) {
		this.implClass = implClass;
	}
	
	/**
	 * Gets the cacheMode
	 *
	 * @return the cacheMode
	 */
	public String getCacheMode() {
		return cacheMode;
	}
	
	/**
	 * Sets the cacheMode
	 *
	 * @param cacheMode the cacheMode to set
	 */
	public void setCacheMode(String cacheMode) {
		this.cacheMode = cacheMode;
	}
	
}
