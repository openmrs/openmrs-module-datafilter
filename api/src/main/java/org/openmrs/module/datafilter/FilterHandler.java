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

import org.openmrs.api.context.Context;

public interface FilterHandler {
	
	/**
	 * Checks if the specified filter is disabled for the authenticated user.
	 * 
	 * @param filterName the name of the filter to check
	 * @return true if the filter is disabled for the user otherwise false
	 */
	default boolean isFilterDisabled(String filterName) {
		boolean hasByPassPriv = false;
		if (Context.isAuthenticated()) {
			hasByPassPriv = Context.hasPrivilege(filterName + DataFilterConstants.BYPASS_PRIV_SUFFIX);
		}
		
		return Util.isFilterDisabled(filterName) || hasByPassPriv;
	}
	
}
