/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.impl.api.db;

import org.openmrs.module.datafilter.impl.EntityBasisMap;

import java.util.Collection;

public interface DataFilterDAO {
	
	/**
	 * Gets the EntityBasisMap that matches the specified argument values
	 *
	 * @param entityIdentifier the entity identifier to match
	 * @param entityType entity type to match
	 * @param basisIdentifier the basis identifier to match
	 * @param basisType basis type to match
	 * @return the matching EntityBasisMap otherwise null
	 */
	EntityBasisMap getEntityBasisMap(String entityIdentifier, String entityType, String basisIdentifier, String basisType);
	
	/**
	 * Saves the specified EntityBasisMap instance to the database
	 * 
	 * @param entityBasisMap
	 */
	EntityBasisMap saveEntityBasisMap(EntityBasisMap entityBasisMap);
	
	/**
	 * Deletes the specified EntityBasisMap instance from the database
	 * 
	 * @param entityBasisMap
	 */
	void deleteEntityBasisMap(EntityBasisMap entityBasisMap);

	/**
	 * Get all the EntityBasisMap for the specified entity and type from the database
	 *
	 * @param entityIdentifier
	 * @param entityType
	 * @param basisType
	 */
	Collection<EntityBasisMap> get(String entityIdentifier, String entityType, String basisType);
}
