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

import static org.openmrs.module.datafilter.DataFilterConstants.MODULE_ID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.filter.FullTextFilter;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.db.FullTextQueryAndEntityClass;
import org.openmrs.api.db.FullTextQueryCreatedEvent;
import org.openmrs.module.datafilter.registration.DataFilterContext;
import org.openmrs.module.datafilter.registration.DataFilterListener;
import org.openmrs.module.datafilter.registration.FilterParameter;
import org.openmrs.module.datafilter.registration.FullTextDataFilterContext;
import org.openmrs.module.datafilter.registration.FullTextFilterRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component(MODULE_ID + "FullTextQueryCreatedEventListener")
public class FullTextQueryCreatedEventListener implements ApplicationListener<FullTextQueryCreatedEvent> {
	
	private static final Logger log = LoggerFactory.getLogger(FullTextQueryCreatedEventListener.class);
	
	/**
	 * @see ApplicationListener#onApplicationEvent(ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(FullTextQueryCreatedEvent event) {
		if (Daemon.isDaemonThread()) {
			if (log.isTraceEnabled()) {
				log.trace("Skipping enabling of full text filters on daemon thread");
			}
			
			return;
		}
		
		Set<String> enabledFilters = new HashSet();
		Set<Class> filteredClasses = new HashSet();
		for (FullTextFilterRegistration registration : Util.getFullTextFilterRegistrations()) {
			filteredClasses.addAll(registration.getTargetClasses());
			if (!Util.isFilterDisabled(registration.getName())) {
				enabledFilters.add(registration.getName());
			}
		}
		
		FullTextQueryAndEntityClass queryAndClass = (FullTextQueryAndEntityClass) event.getSource();
		FullTextQuery query = queryAndClass.getQuery();
		Class<?> entityClass = queryAndClass.getEntityClass();
		
		if (!filteredClasses.contains(entityClass)) {
			if (log.isDebugEnabled()) {
				log.debug("Skipping enabling of filters on the full text query for " + entityClass.getName());
			}
			
			return;
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Enabling filters on the full text query for " + entityClass.getName());
		}
		
		Map<String, Map<String, Object>> filterParamsMap = new HashMap();
		
		filterLoop: for (FullTextFilterRegistration registration : Util.getFullTextFilterRegistrations()) {
			if (!enabledFilters.contains(registration.getName())) {
				continue;
			}
			
			if (CollectionUtils.isNotEmpty(registration.getParameters())) {
				filterParamsMap.put(registration.getName(), new HashMap());
			}
			
			DataFilterContext filterContext = new FullTextDataFilterContext(registration.getName(), filterParamsMap,
			        entityClass);
			
			List<DataFilterListener> listeners = Context.getRegisteredComponents(DataFilterListener.class);
			
			for (DataFilterListener listener : listeners) {
				if (listener.supports(registration.getName())) {
					//In theory, expect l one listener per filter, since we found one, no more will get called.
					//TODO During filter registration, check for cases where a filter has multiple listeners
					boolean enable = listener.onEnableFilter(filterContext);
					if (!enable) {
						enabledFilters.remove(registration.getName());
						//Don't call anymore filters since we've found one.
						continue filterLoop;
					}
					
					//Don't call anymore filters since we've found one.
					break;
				}
			}
			
			enableFilter(registration, filterParamsMap.get(registration.getName()), query);
		}
		
	}
	
	private void enableFilter(FullTextFilterRegistration registration, Map<String, Object> paramNameValueMap,
	                          FullTextQuery query) {
		
		FullTextFilter filter = query.enableFullTextFilter(registration.getName());
		if (registration.getParameters() != null) {
			for (FilterParameter parameter : registration.getParameters()) {
				filter.setParameter(parameter.getName(), paramNameValueMap.get(parameter.getName()));
			}
		}
		
	}
	
}
