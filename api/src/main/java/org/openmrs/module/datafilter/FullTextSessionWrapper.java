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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.Query;
import org.hibernate.engine.spi.SessionDelegatorBaseImpl;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.FullTextSharedSessionBuilder;
import org.hibernate.search.MassIndexer;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.filter.FullTextFilter;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.api.APIException;

/**
 * Custom implementation of the {@link FullTextSession} interface that acts a wrapper around a
 * target FullTextSession instance, it actually delegates all the method calls directly to the
 * target except for the {@link FullTextSession#createFullTextQuery(Query, Class[])} method where is
 * first enables the filters before returning the created {@link FullTextQuery} object.
 */
final class FullTextSessionWrapper extends SessionDelegatorBaseImpl implements FullTextSession {
	
	private static final Log log = LogFactory.getLog(FullTextSessionWrapper.class);
	
	private static final HashMap<Class<?>, String> CLASS_FIELD_MAP;
	
	static {
		CLASS_FIELD_MAP = new HashMap(3);
		CLASS_FIELD_MAP.put(PersonName.class, "person.personId");
		CLASS_FIELD_MAP.put(PersonAttribute.class, "person.personId");
		CLASS_FIELD_MAP.put(PatientIdentifier.class, "patient.personId");
	}
	
	private FullTextSession fullTextSession;
	
	public FullTextSessionWrapper(FullTextSession fullTextSession) {
		super((SessionImplementor) fullTextSession, fullTextSession);
		this.fullTextSession = fullTextSession;
	}
	
	/**
	 * @see FullTextSession#createFullTextQuery(Query, Class[])
	 */
	@Override
	public FullTextQuery createFullTextQuery(Query luceneQuery, Class<?>... entities) {
		if (entities.length > 1) {
			throw new APIException("Can't apply full text filters to a query with multiple persistent classes");
		}
		
		Class<?> entityClass = entities[0];
		if (!CLASS_FIELD_MAP.containsKey(entityClass)) {
			if (log.isDebugEnabled()) {
				log.debug("Skipping enabling of filters on the full text query for " + entityClass.getName());
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Enabling filters on the full text query for " + entityClass.getName());
		}
		
		FullTextQuery query = fullTextSession.createFullTextQuery(luceneQuery, entityClass);
		FullTextFilter filter = query.enableFullTextFilter(DataFilterConstants.FULL_TEXT_FILTER_NAME_PATIENT);
		filter.setParameter("field", CLASS_FIELD_MAP.get(entityClass));
		List<String> personIds = AccessUtil.getAccessiblePersonIds(Location.class);
		if (personIds.isEmpty()) {
			//If the user isn't granted access to patients at any basis, we add -1 because ids are all > 0,
			//in theory the query will match no records if the user isn't granted access to any basis
			personIds = Collections.singletonList("-1");
		}
		filter.setParameter("patientIds", personIds);
		
		return query;
	}
	
	/**
	 * @see FullTextSession#index(Object)
	 */
	@Override
	public <T> void index(T entity) {
		fullTextSession.index(entity);
	}
	
	/**
	 * @see FullTextSession#getSearchFactory()
	 */
	@Override
	public SearchFactory getSearchFactory() {
		return fullTextSession.getSearchFactory();
	}
	
	/**
	 * @see FullTextSession#purge(Class, Serializable)
	 */
	@Override
	public <T> void purge(Class<T> entityType, Serializable id) {
		fullTextSession.purge(entityType, id);
	}
	
	/**
	 * @see FullTextSession#purgeAll(Class)
	 */
	@Override
	public <T> void purgeAll(Class<T> entityType) {
		fullTextSession.purgeAll(entityType);
	}
	
	/**
	 * @see FullTextSession#flushToIndexes()
	 */
	@Override
	public void flushToIndexes() {
		fullTextSession.flushToIndexes();
	}
	
	/**
	 * @see FullTextSession#createIndexer(Class[])
	 */
	@Override
	public MassIndexer createIndexer(Class<?>... types) {
		return fullTextSession.createIndexer(types);
	}
	
	/**
	 * @see FullTextSession#sessionWithOptions()
	 */
	@Override
	public FullTextSharedSessionBuilder sessionWithOptions() {
		return fullTextSession.sessionWithOptions();
	}
	
}
