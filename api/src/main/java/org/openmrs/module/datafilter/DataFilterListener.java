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

/**
 * Any spring bean that implements this interface will be notified before a supported filter is
 * enabled. A supported filter is one for which a call to {@link #supports(String)} returns true for
 * it. Implementations of this interface can do some useful logic to determine the filter logic a=nd
 * can also set parameter values in the condition for the filter about to be enabled. <pre>
 * Implementations are expected to ONLY listen for filters they have registered.
 * </pre>
 */
public interface DataFilterListener {
	
	/**
	 * Tests whether or not {@link #onEnableFilter(DataFilterContext)} should be invoked for a filter
	 * with the specified name.
	 *
	 * @param filterName The name of the filter to be tested
	 * @return true if and only if {@link #onEnableFilter(DataFilterContext)} should be invoked for a
	 *         filter with a matching name.
	 */
	boolean supports(String filterName);
	
	/**
	 * This method is called just before a listener is enabled, implementation logic is expected to be
	 * include in this method e.g to set parameter values include in the condition. The return value
	 * determines if the filter should be enabled or not.
	 * 
	 * @param filterContext the {@link DataFilterContext} instance associated with the filter getting
	 *            enabled.
	 * @return true if the filter should be enabled otherwise false
	 */
	boolean onEnableFilter(DataFilterContext filterContext);
	
}
