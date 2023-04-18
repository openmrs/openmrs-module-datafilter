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
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.openmrs.User;
import org.openmrs.UserSessionListener;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.datafilter.registration.FilterParameter;
import org.openmrs.module.datafilter.registration.HibernateFilterRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate5.SpringSessionContext;
import org.springframework.stereotype.Component;

import static org.openmrs.module.datafilter.DataFilterBeanFactoryPostProcessor.SESSION_FACTORY_BEAN_NAME;

/**
 * Custom hibernate CurrentSessionContext that enables filters on the current Session object from
 * this single centralized place before it is returned.
 *
 * @see DataFilterUserSessionListener
 */
public class DataFilterSessionContext extends SpringSessionContext {
	
	private static final Logger log = LoggerFactory.getLogger(DataFilterSessionContext.class);
	
	private static final String HAS_BEEN_FILTERED_PROPERTY = DataFilterConstants.MODULE_ID + "filters.applied";
	
	private static final ThreadLocal<Session> tempSessionHolder = new ThreadLocal<>();
	
	public DataFilterSessionContext(SessionFactoryImplementor sessionFactory) {
		super(sessionFactory);
	}
	
	/**
	 * @see SpringSessionContext#currentSession()
	 */
	@Override
	public Session currentSession() throws HibernateException {
		// in parts of the logic here, we need access to a session while the filters are being applied. We use the
		// tempSessionHolder to store the unfiltered session to avoid a stack overflow when this happens
		if (tempSessionHolder.get() != null) {
			log.trace("Session holder already contains a session object");
			
			return tempSessionHolder.get();
		}
		
		Session session = currentSessionInternal();
		
		//This method gets called many times and can slow down the system, in a web environment, every http request is
		//processed in a separate thread, so we use make use of this session flag to ensure it gets invoked
		//exactly once per user
		Map<String, Object> sessionProperties = session.getProperties();
		try {
			if (Boolean.parseBoolean((String) sessionProperties.get(HAS_BEEN_FILTERED_PROPERTY))) {
				log.trace("Skipping filter logic because filters are already set on the current session");
				return session;
			}
		}
		catch (ClassCastException ignored) {
			// we get here if HAS_BEEN_FILTERED_PROPERTY is not a String
		}
		
		if (Daemon.isDaemonThread()) {
			log.trace("Skipping enabling of filters on daemon thread");
			disableAllFilters(session);
			session.setProperty(HAS_BEEN_FILTERED_PROPERTY, Boolean.TRUE.toString());
			return session;
		}
		
		log.debug("Enabling filters on the current session");
		
		// When AccessUtil.isFilterDisabled is called, it triggers a call to SessionFactory.getCurrentSession()
		// which gets us back here and we don't want that to happen, see beginning of this method.
		tempSessionHolder.set(session);
		Set<String> enabledFilters = new HashSet<>();
		try {
			for (HibernateFilterRegistration registration : Util.getHibernateFilterRegistrations()) {
				if (!Util.skipFilter(registration.getName())) {
					enabledFilters.add(registration.getName());
				}
			}
		}
		finally {
			tempSessionHolder.remove();
		}
		
		Map<String, Map<String, Object>> filterParamsMap = new HashMap<>();
		
		filterLoop: for (HibernateFilterRegistration registration : Util.getHibernateFilterRegistrations()) {
			if (enabledFilters.contains(registration.getName())) {
				if (CollectionUtils.isNotEmpty(registration.getParameters())) {
					filterParamsMap.put(registration.getName(), new HashMap<>());
				}
				
				DataFilterContext filterContext = new DataFilterContext(registration.getName(), filterParamsMap);
				List<DataFilterListener> listeners = Context.getRegisteredComponents(DataFilterListener.class);
				// Just in case any listener makes a call to the DB
				tempSessionHolder.set(session);
				try {
					for (DataFilterListener listener : listeners) {
						try {
							if (listener.supports(registration.getName())) {
								// In theory, expect one listener per filter, since we found one, no more will get called.
								// TODO During filter registration, check for cases where a filter has multiple listeners
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
						catch (Exception e) {
							log.error("Exception caught while processing listener {} for registration {}",
							    listener.getClass(), registration.getName(), e);
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
		
		session.setProperty(HAS_BEEN_FILTERED_PROPERTY, Boolean.TRUE.toString());
		return session;
	}
	
	/**
	 * Internal method to get access to session without running through the filter application logic
	 *
	 * @return the current {@link Session} without applying any new filters
	 */
	Session currentSessionInternal() {
		return super.currentSession();
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
		for (HibernateFilterRegistration registration : Util.getHibernateFilterRegistrations()) {
			session.disableFilter(registration.getName());
		}
	}
	
	/**
	 * Custom {@link UserSessionListener} used to reset the filtersAlreadyEnabled flag so that we can
	 * update the enabled/disabled filters on the session object on the current thread, that way we
	 * don't expose sensitive data after a user logs out and also to unblock them after they login.
	 */
	@Component
	public static class DataFilterUserSessionListener implements UserSessionListener {
		
		/**
		 * @see UserSessionListener#loggedInOrOut(User, Event, Status)
		 */
		@Override
		public void loggedInOrOut(User user, Event event, Status status) {
			if (log.isDebugEnabled()) {
				log.debug("User log " + (event == Event.LOGIN ? "in" : "out")
				        + " event received, clearing filters set on the current session");
			}
			
			reset();
		}
		
	}
	
	/**
	 * Clears the flag set on the current thread so that filters can be re-enabled or disabled again on
	 * the session.
	 */
	public static void reset() {
		log.debug("Clearing filters set on the current session");
		
		try {
			SessionFactory sessionFactory = Context.getRegisteredComponent(SESSION_FACTORY_BEAN_NAME, SessionFactory.class);
			Session session = sessionFactory.getCurrentSession();
			if (session != null) {
				Map<String, Object> sessionProperties = session.getProperties();
				if (sessionProperties.containsKey(HAS_BEEN_FILTERED_PROPERTY)) {
					session.setProperty(HAS_BEEN_FILTERED_PROPERTY, Boolean.FALSE.toString());
				}
			}
		}
		catch (Exception e) {
			log.warn("Caught exception while trying to reset data filter session", e);
		}
	}
	
}
