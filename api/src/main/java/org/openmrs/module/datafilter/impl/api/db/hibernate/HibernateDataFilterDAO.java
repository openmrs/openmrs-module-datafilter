/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.impl.api.db.hibernate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.openmrs.module.datafilter.impl.EntityBasisMap;
import org.openmrs.module.datafilter.impl.api.db.DataFilterDAO;

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
	
	/**
	 * @see DataFilterDAO#getEntityBasisMaps(String, String, String)
	 */
	@Override
	public Collection<EntityBasisMap> getEntityBasisMaps(String entityIdentifier, String entityType, String basisType) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(EntityBasisMap.class);
		criteria.add(Restrictions.eq("entityIdentifier", entityIdentifier).ignoreCase());
		criteria.add(Restrictions.eq("entityType", entityType).ignoreCase());
		criteria.add(Restrictions.eq("basisType", basisType).ignoreCase());
		
		return (Collection<EntityBasisMap>) criteria.list();
	}
	
	@Override
	public List<EntityBasisMap> getEntityBasisMapsByBasis(String entityType, String basisType, String basisIdentifier) {
		
		Session session = sessionFactory.getCurrentSession();
		
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<EntityBasisMap> cq = cb.createQuery(EntityBasisMap.class);
		
		Root<EntityBasisMap> root = cq.from(EntityBasisMap.class);
		cq.select(root).where(cb.and(cb.equal(root.get("entityType"), entityType),
		    cb.equal(root.get("basisType"), basisType), cb.equal(root.get("basisIdentifier"), basisIdentifier)));
		
		Query<EntityBasisMap> query = session.createQuery(cq);
		return query.getResultList();
	}
}
