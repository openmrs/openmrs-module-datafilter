/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.aspects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.Query;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.openmrs.module.datafilter.DataFilterConstants;

/**
 * Contains advice to be applied to the {@link SessionFactory#getCurrentSession()} and
 * {@link FullTextSession#createFullTextQuery(Query, Class[])} methods that way the module can
 * always enable filters to the session and and full text session objects respectively.
 */
@Aspect
public class DataFilterAspect {
	
	private static final Log log = LogFactory.getLog(DataFilterAspect.class);
	
	/**
	 * Enables filters on the returned {@link Session} object
	 * 
	 * @param session the returned Session object
	 */
	@AfterReturning(value = "execution(* org.hibernate.SessionFactory.getCurrentSession())", returning = "session")
	public void afterGetCurrentSession(Session session) {
		if (log.isDebugEnabled()) {
			log.debug("Enabling filter on the current session");
		}
		
		Filter filter = session.getEnabledFilter(DataFilterConstants.FILTER_NAME_ENCOUNTER);
		if (filter == null) {
			session.enableFilter(DataFilterConstants.FILTER_NAME_ENCOUNTER);
		}
	}
	
	/**
	 * Enables filters on the returned {@link FullTextQuery} object
	 *
	 * @param fullTextQuery the returned FullTextQuery object
	 */
	@AfterReturning(value = "execution(* org.hibernate.search.FullTextSession.createFullTextQuery(..))", returning = "fullTextQuery")
	public void afterCreateFullTextQuery(FullTextQuery fullTextQuery) {
		if (log.isDebugEnabled()) {
			log.debug("Enabling filter on the full text session");
		}
		//TODO apply filters to the  FulltextQuery
	}
	
}
