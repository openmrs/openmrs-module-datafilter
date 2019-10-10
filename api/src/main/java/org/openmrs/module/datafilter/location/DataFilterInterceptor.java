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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.EmptyInterceptor;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.Type;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.datafilter.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This interceptor provides a safety net to catch any cases where an entity that the authenticated
 * user has no access to is getting loaded from the DB, by default the module runs in strict mode
 * implying that the interceptor is enabled by default, also note that the interceptor isn't applied
 * for super and daemon user.
 */
@Component("dataFilterInterceptor")
public class DataFilterInterceptor extends EmptyInterceptor {
	
	private static final Logger log = LoggerFactory.getLogger(DataFilterInterceptor.class);
	
	protected static final Map<Class<?>, String> locationBasedClassAndFiltersMap;
	
	protected static final Map<Class<?>, String> encTypeBasedClassAndFiltersMap;
	
	static {
		locationBasedClassAndFiltersMap = new HashMap();
		locationBasedClassAndFiltersMap.put(Visit.class, LocationBasedAccessConstants.LOCATION_BASED_FILTER_NAME_VISIT);
		locationBasedClassAndFiltersMap.put(Encounter.class,
		    LocationBasedAccessConstants.LOCATION_BASED_FILTER_NAME_ENCOUNTER);
		locationBasedClassAndFiltersMap.put(Obs.class, LocationBasedAccessConstants.LOCATION_BASED_FILTER_NAME_OBS);
		locationBasedClassAndFiltersMap.put(Patient.class, LocationBasedAccessConstants.LOCATION_BASED_FILTER_NAME_PATIENT);
		
		encTypeBasedClassAndFiltersMap = new HashMap();
		encTypeBasedClassAndFiltersMap.put(Encounter.class,
		    LocationBasedAccessConstants.ENC_TYPE_PRIV_BASED_FILTER_NAME_ENCOUNTER);
		encTypeBasedClassAndFiltersMap.put(Obs.class, LocationBasedAccessConstants.ENC_TYPE_PRIV_BASED_FILTER_NAME_OBS);
	}
	
	/**
	 * @see EmptyInterceptor#onLoad(Object, Serializable, Object[], String[], Type[])
	 */
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		if (Daemon.isDaemonThread()) {
			if (log.isDebugEnabled()) {
				log.trace("Skipping DataFilterInterceptor for daemon thread");
			}
		} else {
			User user = Context.getAuthenticatedUser();
			if (user != null && user.isSuperUser()) {
				if (log.isDebugEnabled()) {
					log.trace("Skipping DataFilterInterceptor for super user");
				}
			} else {
				boolean filteredByLoc = locationBasedClassAndFiltersMap.keySet().contains(entity.getClass());
				boolean filteredByEnc = encTypeBasedClassAndFiltersMap.keySet().contains(entity.getClass());
				//TODO We should allow filter registrations to actually provide the logic of what the interceptor
				//should reject vs accept when loading a filtered type, some sort of callback and pass them the
				//entity and state.
				if (filteredByLoc || filteredByEnc) {
					Session session = Context.getRegisteredComponents(SessionFactory.class).get(0).getCurrentSession();
					//Hibernate will flush any changes in the current session before querying the DB when fetching
					//the GP value below and we end up in this method again, therefore we need to disable auto flush
					final FlushMode flushMode = session.getFlushMode();
					session.setFlushMode(FlushMode.MANUAL);
					try {
						AdministrationService as = Context.getAdministrationService();
						String strictModeStr = as.getGlobalProperty(LocationBasedAccessConstants.GP_RUN_IN_STRICT_MODE);
						if ("false".equalsIgnoreCase(strictModeStr)) {
							if (log.isDebugEnabled()) {
								log.trace("Skipping DataFilterInterceptor because the module is not running in strict mode");
							}
						} else {
							if (filteredByLoc) {
								checkIfHasLocationBasedAccess(entity, id, state, propertyNames, user,
								    locationBasedClassAndFiltersMap);
							}
							
							if (filteredByEnc) {
								checkIfHasEncounterTypeBasedAccess(entity, state, propertyNames, user,
								    encTypeBasedClassAndFiltersMap);
							}
						}
					}
					finally {
						//reset
						session.setFlushMode(flushMode);
					}
				}
			}
		}
		
		return super.onLoad(entity, id, state, propertyNames, types);
	}
	
	private void checkIfHasLocationBasedAccess(Object entity, Serializable id, Object[] state, String[] propertyNames,
	                                           User user, Map<Class<?>, String> filtersMap) {
		
		boolean check = !Util.isFilterDisabled(filtersMap.get(entity.getClass()));
		if (check) {
			Object personId = id;
			if (entity instanceof Visit || entity instanceof Encounter || entity instanceof Obs) {
				final String personPropertyName = entity instanceof Obs ? "person" : "patient";
				int patientIndex = ArrayUtils.indexOf(propertyNames, personPropertyName);
				personId = ((Person) state[patientIndex]).getPersonId();
			}
			
			if (user == null || !AccessUtil.getAccessiblePersonIds(Location.class).contains(personId.toString())) {
				throw new ContextAuthenticationException(LocationBasedAccessConstants.ILLEGAL_RECORD_ACCESS_MESSAGE);
			}
		}
	}
	
	private void checkIfHasEncounterTypeBasedAccess(Object entity, Object[] state, String[] propertyNames, User user,
	                                                Map<Class<?>, String> filtersMap) {
		
		boolean check = !Util.isFilterDisabled(filtersMap.get(entity.getClass()));
		if (check) {
			Integer encounterTypeId = null;
			boolean isEncounterLessObs = false;
			if (entity instanceof Encounter) {
				int encounterTypeIndex = ArrayUtils.indexOf(propertyNames, "encounterType");
				encounterTypeId = ((EncounterType) state[encounterTypeIndex]).getEncounterTypeId();
			} else {
				//This is an Obs
				int encounterIndex = ArrayUtils.indexOf(propertyNames, "encounter");
				Encounter encounter = (Encounter) state[encounterIndex];
				if (encounter == null) {
					isEncounterLessObs = true;
				} else {
					if (encounter.getEncounterType() != null) {
						encounterTypeId = encounter.getEncounterType().getEncounterTypeId();
					} else {
						//If it's an obs that's getting loaded, encounter.encounterType could be
						//null so fetch the encounter type id from the database
						encounterTypeId = AccessUtil.getEncounterTypeId(encounter.getEncounterId());
					}
				}
			}
			
			if (!isEncounterLessObs) {
				String requiredPrivilege = AccessUtil.getViewPrivilege(encounterTypeId);
				if (requiredPrivilege != null) {
					if (user == null || !user.hasPrivilege(requiredPrivilege)) {
						throw new ContextAuthenticationException(LocationBasedAccessConstants.ILLEGAL_RECORD_ACCESS_MESSAGE);
					}
				}
			}
		}
	}
	
}
