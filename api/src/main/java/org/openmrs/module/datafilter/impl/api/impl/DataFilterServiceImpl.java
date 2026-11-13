/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.impl.api.impl;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.OpenmrsObject;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.datafilter.DataFilterSessionContext;
import org.openmrs.module.datafilter.impl.EntityBasisMap;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.openmrs.module.datafilter.impl.api.db.DataFilterDAO;
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
	public void grantAccess(OpenmrsObject entity, OpenmrsObject basis) {
		Context.getService(DataFilterService.class).grantAccess(entity, Collections.singleton(basis));
	}
	
	/**
	 * @see DataFilterService#grantAccess(OpenmrsObject, Collection)
	 */
	@Transactional
	@Override
	public void grantAccess(OpenmrsObject entity, Collection<OpenmrsObject> bases) {
		for (OpenmrsObject basis : bases) {
			if (!hasAccess(entity, basis)) {
				EntityBasisMap map = new EntityBasisMap();
				map.setEntityIdentifier(getIdentifier(entity));
				map.setEntityType(Hibernate.getClass(entity).getName());
				map.setBasisIdentifier(getIdentifier(basis));
				map.setBasisType(Hibernate.getClass(basis).getName());
				
				dao.saveEntityBasisMap(map);
			}
		}
		
		DataFilterSessionContext.reset();
	}
	
	/**
	 * @see DataFilterService#revokeAccess(OpenmrsObject, OpenmrsObject)
	 */
	@Transactional
	@Override
	public void revokeAccess(OpenmrsObject entity, OpenmrsObject basis) {
		Context.getService(DataFilterService.class).revokeAccess(entity, Collections.singleton(basis));
	}
	
	/**
	 * @see DataFilterService#revokeAccess(OpenmrsObject, Collection)
	 */
	@Transactional
	@Override
	public void revokeAccess(OpenmrsObject entity, Collection<OpenmrsObject> bases) {
		for (OpenmrsObject basis : bases) {
			EntityBasisMap map = dao.getEntityBasisMap(getIdentifier(entity), Hibernate.getClass(entity).getName(),
			    getIdentifier(basis), Hibernate.getClass(basis).getName());
			if (map != null) {
				dao.deleteEntityBasisMap(map);
			}
		}
		
		DataFilterSessionContext.reset();
	}
	
	/**
	 * @see DataFilterService#hasAccess(OpenmrsObject, OpenmrsObject)
	 */
	@Override
	public boolean hasAccess(OpenmrsObject entity, OpenmrsObject basis) {
		EntityBasisMap map = dao.getEntityBasisMap(getIdentifier(entity), Hibernate.getClass(entity).getName(),
		    getIdentifier(basis), Hibernate.getClass(basis).getName());
		
		if (map != null) {
			return true;
		}
		
		return false;
	}
	
	private String getIdentifier(OpenmrsObject openmrsObject) {
		String entityId = null;
		try {
			entityId = openmrsObject.getId().toString();
		}
		catch (UnsupportedOperationException e) {
			if (openmrsObject instanceof Role || openmrsObject instanceof Privilege) {
				entityId = ((OpenmrsMetadata) openmrsObject).getName();
			}
		}
		
		if (StringUtils.isBlank(entityId)) {
			throw new APIException("Failed to determine id for Object: " + openmrsObject);
		}
		
		return entityId;
	}
	
	/**
	 * @see DataFilterService#getEntityBasisMaps(OpenmrsObject, String)
	 */
	@Override
	public Collection<EntityBasisMap> getEntityBasisMaps(OpenmrsObject entity, String basisClassName) {
		return dao.getEntityBasisMaps(entity.getId().toString(), Hibernate.getClass(entity).getName(), basisClassName);
	}
	
	@Override
	public Collection<EntityBasisMap> getEntityBasisMapsByBasis(Class<? extends OpenmrsObject> entityClass,
	        OpenmrsObject basis) {
		return getEntityBasisMapsByBasis(entityClass.getName(), basis);
	}
	
	@Override
	public Collection<EntityBasisMap> getEntityBasisMapsByBasis(String entityClassName, OpenmrsObject basis) {
		return dao.getEntityBasisMapsByBasis(entityClassName, Hibernate.getClass(basis).getName(), basis.getId().toString());
	}
}
