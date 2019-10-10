/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.location;

import static org.openmrs.module.datafilter.location.LocationBasedAccessConstants.LOCATION_BASED_FILTER_NAME_PATIENT;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.hibernate.search.FullTextQuery;
import org.hibernate.search.filter.FullTextFilter;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.FullTextQueryAndEntityClass;
import org.openmrs.api.db.FullTextQueryCreatedEvent;
import org.openmrs.module.datafilter.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component("dataFilterFullTextQueryCreatedEventListener")
public class FullTextQueryCreatedEventListener implements ApplicationListener<FullTextQueryCreatedEvent> {
	
	private static final Logger log = LoggerFactory.getLogger(FullTextQueryCreatedEventListener.class);
	
	private static final HashMap<Class<?>, String> CLASS_FIELD_MAP;
	
	static {
		CLASS_FIELD_MAP = new HashMap(3);
		CLASS_FIELD_MAP.put(PersonName.class, "person.personId");
		CLASS_FIELD_MAP.put(PersonAttribute.class, "person.personId");
		CLASS_FIELD_MAP.put(PatientIdentifier.class, "patient.personId");
	}
	
	/**
	 * @see ApplicationListener#onApplicationEvent(ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(FullTextQueryCreatedEvent event) {
		if (Context.isAuthenticated() && Context.getAuthenticatedUser().isSuperUser()) {
			if (log.isTraceEnabled()) {
				log.trace("Skipping enabling of filters for super user");
			}
			
			return;
		}
		
		FullTextQueryAndEntityClass queryAndClass = (FullTextQueryAndEntityClass) event.getSource();
		FullTextQuery query = queryAndClass.getQuery();
		Class<?> entityClass = queryAndClass.getEntityClass();
		if (!CLASS_FIELD_MAP.containsKey(entityClass)) {
			if (log.isDebugEnabled()) {
				log.debug("Skipping enabling of filters on the full text query for " + entityClass.getName());
			}
			
			return;
		}
		
		if (Util.isFilterDisabled(LOCATION_BASED_FILTER_NAME_PATIENT)) {
			return;
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Enabling filters on the full text query for " + entityClass.getName());
		}
		
		FullTextFilter filter = query
		        .enableFullTextFilter(LocationBasedAccessConstants.LOCATION_BASED_FULL_TEXT_FILTER_NAME_PATIENT);
		filter.setParameter("field", CLASS_FIELD_MAP.get(entityClass));
		Collection<String> personIds = AccessUtil.getAccessiblePersonIds(Location.class);
		if (personIds.isEmpty()) {
			//If the user isn't granted access to patients at any basis, we add -1 because ids are all > 0,
			//in theory the query will match no records if the user isn't granted access to any basis
			personIds = Collections.singleton("-1");
		}
		filter.setParameter("patientIds", personIds);
		
	}
	
}
