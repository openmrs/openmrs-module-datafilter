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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Filter;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.datafilter.registration.FilterParameter;
import org.openmrs.module.datafilter.registration.FilterRegistration;
import org.openmrs.module.datafilter.registration.HibernateFilterRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.SpringSessionContext;

;

/**
 * Custom hibernate CurrentSessionContext that enables filters on the current Session object from
 * this single centralized place before it is returned.
 *
 * <pre>
 *  TODO:
 *  May be this is not the best implementation since this gets called multiple times,
 *  Another possible approach is to add return advice after opening the session since
 *  it gets called once per request, the issue is that there is no authenticated user
 *  at the time it is called. Therefore, the logic would have to listen to successful authentication
 *  events and then enable filters for the newly authenticated user on the current session that
 *  was previously opened, it would only be done once unlike here where we do it every time the
 *  the current session is looked up.
 *
 *  Also note that in unit tests multiple uses can get logged in and out within the same thread (session)
 *  which actually should never arise in a deployed environment which is why we went with this initial
 *  implementation for simplicity. This also explains why for the above suggested alternative approach, you
 *  would need to listen for authentication events to refresh the enabled filters config.
 *
 *  The logic in this class should be generic, it's currently implemented to filter by location,
 *  but in theory it should allow another module to filter by some other basis e.g. program
 * </pre>
 */

public class DataFilterSessionContext extends SpringSessionContext {
	
	private static final Logger log = LoggerFactory.getLogger(DataFilterSessionContext.class);
	
	private ThreadLocal<Session> tempSessionHolder = new ThreadLocal();
	
	public DataFilterSessionContext(SessionFactoryImplementor sessionFactory) {
		super(sessionFactory);
	}
	
	/**
	 * @see SpringSessionContext#currentSession()
	 */
	@Override
	public Session currentSession() throws HibernateException {
		if (tempSessionHolder.get() != null) {
			//This method being again from below, return the cached session object to avoid stack overflow
			if (log.isTraceEnabled()) {
				log.trace("Session holder already contains a session object");
			}
			return tempSessionHolder.get();
		}
		
		Session session = super.currentSession();
		if (Daemon.isDaemonThread()) {
			if (log.isTraceEnabled()) {
				log.trace("Skipping enabling of filters on daemon thread");
			}
			
			disableAllFilters(session);
			
			return session;
		}
		
		if (Context.isAuthenticated() && Context.getAuthenticatedUser().hasPrivilege(DataFilterConstants.PRIV_BY_PASS)) {
			if (log.isTraceEnabled()) {
				log.trace("Skipping enabling of filters for user with bypass privilege");
			}
			
			disableAllFilters(session);
			
			return session;
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Enabling filters on the current session");
		}
		
		//When AccessUtil.isFilterDisabled is called, it triggers a call to SessionFactory.getCurrentSession()
		//which gets us back here and we don't want that to happen, see beginning of this method.
		tempSessionHolder.set(session);
		Set<String> enabledFilters = new HashSet();
		try {
			for (HibernateFilterRegistration registration : Util.getHibernateFilterRegistrations()) {
				if (!Util.isFilterDisabled(registration.getName())) {
					enabledFilters.add(registration.getName());
				}
			}
		}
		finally {
			tempSessionHolder.remove();
		}
		
		Map<String, Map<String, Object>> filterParamsMap = new HashMap();
		
		filterLoop: for (HibernateFilterRegistration registration : Util.getHibernateFilterRegistrations()) {
			if (enabledFilters.contains(registration.getName())) {
				if (CollectionUtils.isNotEmpty(registration.getParameters())) {
					filterParamsMap.put(registration.getName(), new HashMap());
				}
				
				DataFilterContext filterContext = new DataFilterContext(registration.getName(), filterParamsMap);
				List<DataFilterListener> listeners = Context.getRegisteredComponents(DataFilterListener.class);
				//Just in case any listener makes a call to the DB
				tempSessionHolder.set(session);
				try {
					for (DataFilterListener listener : listeners) {
						if (listener.supports(registration.getName())) {
							//In theory, expect l one listener per filter, since we found one, no more will get called.
							//TODO During filter registration, check for cases where a filter has multiple listeners
							boolean enable = listener.onEnableFilter(filterContext);
							if (!enable) {
								enabledFilters.remove(registration.getName());
								session.disableFilter(registration.getName());
								//Don't call anymore filters since we've found one.
								continue filterLoop;
							}
							
							//Don't call anymore filters since we've found one.
							break;
						}
					}
				}
				finally {
					tempSessionHolder.remove();
				}
				
				enableFilter(registration, filterParamsMap.get(registration.getName()), session);
			} else {
				session.disableFilter(registration.getName());
			}
		}
		
		return session;
	}
	
	private void enableFilter(HibernateFilterRegistration registration, Map<String, Object> paramNameValueMap,
	                          Session session) {
		
		Filter filter = session.getEnabledFilter(registration.getName());
		if (filter == null) {
			filter = session.enableFilter(registration.getName());
		}
		
		if (registration.getParameters() != null) {
			for (FilterParameter parameter : registration.getParameters()) {
				Object value = paramNameValueMap.get(parameter.getName());
				if (value != null && value.getClass().isArray()) {
					filter.setParameterList(parameter.getName(), (Object[]) value);
				} else if (value instanceof Collection) {
					filter.setParameterList(parameter.getName(), (Collection) value);
				} else {
					filter.setParameter(parameter.getName(), value);
				}
			}
		}
		
	}
	
	private void disableAllFilters(Session session) {
		for (FilterRegistration registration : Util.getHibernateFilterRegistrations()) {
			session.disableFilter(registration.getName());
		}
	}
	
}
