/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.impl.api;

import java.util.Collection;

import org.openmrs.OpenmrsObject;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.datafilter.impl.EntityBasisMap;

public interface DataFilterService extends OpenmrsService {
	
	/**
	 * Grants access to records associated to the specified basis to the specified entity
	 * 
	 * @param entity the entity to grant access
	 * @param basis the basis associated to the records to be accessed
	 */
	void grantAccess(OpenmrsObject entity, OpenmrsObject basis);
	
	/**
	 * Grants access to records associated to the specified list of bases to the specified entity
	 * 
	 * @param entity the entity to grant access
	 * @param bases the list of bases associated to the records to be accessed
	 */
	void grantAccess(OpenmrsObject entity, Collection<OpenmrsObject> bases);
	
	/**
	 * Revokes access to records associated to the specified basis from the specified entity
	 * 
	 * @param entity the entity from which to revoke access
	 * @param basis the basis associated to the records from which to revoke access
	 */
	void revokeAccess(OpenmrsObject entity, OpenmrsObject basis);
	
	/**
	 * Revokes access to records associated to the specified list of bases from the specified entity
	 * 
	 * @param entity the entity from which to revoke access
	 * @param bases the list of bases associated to the records from which to revoke access
	 */
	void revokeAccess(OpenmrsObject entity, Collection<OpenmrsObject> bases);
	
	/**
	 * Checks whether the specified entity has access to the specified basis
	 * 
	 * @param entity the entity to check whether they have access to the basis
	 * @param basis the basis to against
	 * @return true if the entity has access otherwise false
	 */
	boolean hasAccess(OpenmrsObject entity, OpenmrsObject basis);
	
	/**
	 * Gets a collection of mappings for the specified instance for the basis matching the specified
	 * basis class name.
	 * 
	 * @param entity the instance to match
	 * @param basisClassName fully qualified java class name of the basis type
	 * @return a collection of {@link EntityBasisMap} instances
	 */
	Collection<EntityBasisMap> getEntityBasisMaps(OpenmrsObject entity, String basisClassName);
	
	/**
	 * Gets a collection of mappings from a specific basis to allowed entities of the specified type
	 *
	 * @param entityClass the class of the entity to match
	 * @param basis the specific basis entity to match
	 * @return a collection of {@link EntityBasisMap} instances
	 */
	Collection<EntityBasisMap> getEntityBasisMapsByBasis(Class<? extends OpenmrsObject> entityClass, OpenmrsObject basis);
	
	/**
	 * Gets a collection of mappings from a specific basis to allowed entities of the specified type
	 *
	 * @param entityClassName the class name of the entity to match
	 * @param basis the specific basis entity to match
	 * @return a collection of {@link EntityBasisMap} instances
	 */
	Collection<EntityBasisMap> getEntityBasisMapsByBasis(String entityClassName, OpenmrsObject basis);
	
}
