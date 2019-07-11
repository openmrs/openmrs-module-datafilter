/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.api;

import java.util.List;

import org.openmrs.OpenmrsObject;
import org.openmrs.api.OpenmrsService;

public interface DataFilterService extends OpenmrsService {
	
	/**
	 * Grants access to records associated to the specified basis to the specified entity
	 * 
	 * @param entity the entity to grant access
	 * @param basis the basis associated to the records to be accessed
	 */
	void grantAccess(OpenmrsObject entity, OpenmrsObject basis);
	
	/**
	 * @param entity the entity to grant access
	 * @param bases the bases associated to the records to be accessed
	 */
	void grantAccess(OpenmrsObject entity, List<OpenmrsObject> bases);
	
	/**
	 * @param entity the entity from which to revoke access
	 * @param basis the basis associated to the records from which to revoke access
	 */
	void revokeAccess(OpenmrsObject entity, OpenmrsObject basis);
	
	/**
	 * @param entity the entity from which to revoke access
	 * @param bases the bases associated to the records from which to revoke access
	 */
	void revokeAccess(OpenmrsObject entity, List<OpenmrsObject> bases);
	
	/**
	 * Checks whether the specified entity has access to the specified basis
	 * 
	 * @param entity the entity to check whether they have access to the basis
	 * @param basis the basis to against
	 * @return true if the entity has access otherwise false
	 */
	boolean hasAccess(OpenmrsObject entity, OpenmrsObject basis);
	
}
