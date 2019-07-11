/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.api.db.hibernate;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.datafilter.EntityBasisMap;
import org.openmrs.module.datafilter.api.db.DataFilterDAO;

public class HibernateDataFilterDAO implements DataFilterDAO {
	
	private SessionFactory sessionFactory;
	
	/**
	 * Sets the sessionFactory
	 *
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see DataFilterDAO#getEntityBasisMap(String, String, String, String)
	 */
	@Override
	public EntityBasisMap getEntityBasisMap(String entityIdentifier, String entityType, String basisIdentifier,
	                                        String basisType) {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(EntityBasisMap.class);
		criteria.add(Restrictions.eq("entityIdentifier", entityIdentifier).ignoreCase());
		criteria.add(Restrictions.eq("entityType", entityType).ignoreCase());
		criteria.add(Restrictions.eq("basisIdentifier", basisIdentifier).ignoreCase());
		criteria.add(Restrictions.eq("basisType", basisType).ignoreCase());
		
		return (EntityBasisMap) criteria.uniqueResult();
	}
	
	/**
	 * @see DataFilterDAO#saveEntityBasisMap(EntityBasisMap)
	 */
	@Override
	public EntityBasisMap saveEntityBasisMap(EntityBasisMap entityBasisMap) {
		sessionFactory.getCurrentSession().save(entityBasisMap);
		return entityBasisMap;
	}
	
	/**
	 * @see DataFilterDAO#deleteEntityBasisMap(EntityBasisMap)
	 */
	@Override
	public void deleteEntityBasisMap(EntityBasisMap entityBasisMap) {
		sessionFactory.getCurrentSession().delete(entityBasisMap);
	}
	
}
