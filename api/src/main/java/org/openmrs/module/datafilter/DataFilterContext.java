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

import java.util.Map;

/**
 * An object used to pass information between the data filter framework and a
 * {@link DataFilterListener} before enabling the filter.
 */
public class DataFilterContext {
	
	private String filterName;
	
	private Map<String, Map<String, Object>> filterAndParamValueMap;
	
	public DataFilterContext(String filterName, Map<String, Map<String, Object>> filterAndParamValueMap) {
		this.filterName = filterName;
		this.filterAndParamValueMap = filterAndParamValueMap;
	}
	
	/**
	 * Gets the filterName
	 *
	 * @return the filterName
	 */
	public String getFilterName() {
		return filterName;
	}
	
	/**
	 * Sets the value od the specified filter parameter name
	 * 
	 * @param parameterName the name of the filter to set
	 * @param value the value to set
	 */
	public void setParameter(String parameterName, Object value) {
		filterAndParamValueMap.get(filterName).put(parameterName, value);
	}
	
}
