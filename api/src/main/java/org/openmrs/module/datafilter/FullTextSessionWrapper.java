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

/**
 * Custom implementation of the {@link FullTextSession} interface that acts a wrapper around a
 * target FullTextSession instance, it actually delegates all the method calls directly to the
 * target except for the {@link FullTextSession#createFullTextQuery(Query, Class[])} method where is
 * first enables the filters before returning the created {@link FullTextQuery} object.
 */
final class FullTextSessionWrapper extends SessionDelegatorBaseImpl implements FullTextSession {
	
	private static final Log log = LogFactory.getLog(FullTextSessionWrapper.class);
	
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
		if (log.isDebugEnabled()) {
			log.debug("Enabling filter on the full text session");
			//TODO enable wrappers
		}

		return fullTextSession.createFullTextQuery(luceneQuery, entities);
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
