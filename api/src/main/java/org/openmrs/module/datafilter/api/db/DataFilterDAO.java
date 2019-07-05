/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.api.db;

import org.openmrs.module.datafilter.AuthorizedEntityBasisMap;

public interface DataFilterDAO {
	
	/**
	 * Saves the specified AuthorizedEntityBasisMap instance to the database
	 * 
	 * @param authorizedEntityBasisMap
	 */
	AuthorizedEntityBasisMap saveAuthorizedBasisMapById(AuthorizedEntityBasisMap authorizedEntityBasisMap);
	
	/**
	 * Deletes the specified AuthorizedEntityBasisMap instance from the database
	 * 
	 * @param authorizedEntityBasisMap
	 */
	void deleteAuthorizedBasisMap(AuthorizedEntityBasisMap authorizedEntityBasisMap);
	
}
