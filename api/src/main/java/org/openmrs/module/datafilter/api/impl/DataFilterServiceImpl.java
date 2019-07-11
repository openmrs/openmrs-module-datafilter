/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.api.impl;

import java.util.List;

import org.openmrs.OpenmrsObject;
import org.openmrs.Role;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.datafilter.EntityBasisMap;
import org.openmrs.module.datafilter.api.DataFilterService;
import org.openmrs.module.datafilter.api.db.DataFilterDAO;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class DataFilterServiceImpl extends BaseOpenmrsService implements DataFilterService {
	
	private DataFilterDAO dao;
	
	/**
	 * Sets the dao
	 *
	 * @param dao the dao to set
	 */
	public void setDao(DataFilterDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see DataFilterService#grantAccess(OpenmrsObject, OpenmrsObject)
	 */
	@Transactional
	@Override
	public void grantAccess(OpenmrsObject openmrsObject, OpenmrsObject basis) {
		
	}
	
	/**
	 * @see DataFilterService#grantAccess(OpenmrsObject, List)
	 */
	@Transactional
	@Override
	public void grantAccess(OpenmrsObject openmrsObject, List<OpenmrsObject> basis) {
		
	}
	
	/**
	 * @see DataFilterService#revokeAccess(OpenmrsObject, OpenmrsObject)
	 */
	@Transactional
	@Override
	public void revokeAccess(OpenmrsObject openmrsObject, OpenmrsObject bases) {
		
	}
	
	/**
	 * @see DataFilterService#revokeAccess(OpenmrsObject, List)
	 */
	@Transactional
	@Override
	public void revokeAccess(OpenmrsObject openmrsObject, List<OpenmrsObject> bases) {
		
	}
	
	/**
	 * @see DataFilterService#hasAccess(OpenmrsObject, OpenmrsObject)
	 */
	@Override
	public boolean hasAccess(OpenmrsObject entity, OpenmrsObject basis) {
		String entityId = null;
		String basisId = null;
		try {
			entityId = entity.getId().toString();
		}
		catch (UnsupportedOperationException e) {
			if (entity instanceof Role) {
				entityId = entity.getId().toString();
			}
		}
		
		try {
			basisId = basis.getId().toString();
		}
		catch (UnsupportedOperationException e) {
			if (basis instanceof Role) {
				basisId = basis.getId().toString();
			}
		}
		
		if (entityId == null) {
			throw new APIException("Failed to determine entityId");
		}
		
		if (basisId == null) {
			throw new APIException("Failed to determine basisId");
		}
		
		EntityBasisMap map = dao.getEntityBasisMap(entityId, entity.getClass().getName(), basisId,
		    basis.getClass().getName());
		
		if (map != null) {
			return true;
		}
		
		return false;
	}
}
