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

import static org.openmrs.module.datafilter.DataFilterConstants.FILTER_NAME_ENCOUNTER;

import java.util.List;

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
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.AccessUtil;
import org.openmrs.module.datafilter.DataFilterConstants;

/**
 * Contains advice to be applied to the {@link SessionFactory#getCurrentSession()} and
 * {@link FullTextSession#createFullTextQuery(Query, Class[])} methods that way the module can
 * always enable filters to the session and and full text session objects respectively.
 */
@Aspect
public class DataFilterAspect {
	
	private static final Log log = LogFactory.getLog(DataFilterAspect.class);
	
	private static final Object FLAG = new Object();
	
	private ThreadLocal<Object> skipAdvice = new ThreadLocal<>();
	
	/**
	 * Enables filters on the returned {@link Session} object
	 * 
	 * @param session the returned Session object
	 */
	@AfterReturning(value = "execution(* org.hibernate.SessionFactory.getCurrentSession())", returning = "session")
	public void afterGetCurrentSession(Session session) {
		
		if (skipAdvice.get() != null) {
			if (log.isDebugEnabled()) {
				log.debug("Already running advice, skipping for current invocation");
			}
			return;
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Enabling filter on the current session");
		}
		
		//User isn't granted access to patients at any location, so we set ids to -1,
		//because patient Ids are all > 0, so this in theory will match no records
		String patientIds = "-1";
		if (Context.getAuthenticatedUser() != null) {
			List<String> accessiblePersonIds;
			try {
				//Don't run the advice on the next line since we're already inside it
				skipAdvice.set(FLAG);
				accessiblePersonIds = AccessUtil.getAccessiblePersonIds(Location.class);
			}
			finally {
				skipAdvice.remove();
			}
			
			if (!accessiblePersonIds.isEmpty()) {
				patientIds = String.join(",", accessiblePersonIds);
			}
		}
		
		Filter filter = session.getEnabledFilter(FILTER_NAME_ENCOUNTER);
		if (filter == null) {
			filter = session.enableFilter(FILTER_NAME_ENCOUNTER);
		}
		filter.setParameter(DataFilterConstants.FILTER_PARAM_PATIENT_IDS, patientIds);
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
