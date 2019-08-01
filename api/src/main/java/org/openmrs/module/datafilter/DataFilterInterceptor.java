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
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EmptyInterceptor;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.Type;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Privilege;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.Daemon;
import org.springframework.stereotype.Component;

/**
 * This interceptor provides a safety net to catch any cases where an entity that the authenticated
 * user has no access to is getting loaded from the DB, by default the module runs in strict mode
 * implying that the interceptor is enabled by default, also note that the interceptor isn't applied
 * for super and daemon user.
 */
@Component("dataFilterInterceptor")
public class DataFilterInterceptor extends EmptyInterceptor {
	
	private static final Log log = LogFactory.getLog(DataFilterInterceptor.class);
	
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
				Map<Class<?>, Collection<String>> locationBasedClassAndFiltersMap = AccessUtil
				        .getLocationBasedClassAndFiltersMap();
				Map<Class<?>, Collection<String>> encTypeBasedClassAndFiltersMap = AccessUtil
				        .getEncounterTypeViewPrivilegeBasedClassAndFiltersMap();
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
						String strictModeStr = as.getGlobalProperty(DataFilterConstants.GP_RUN_IN_STRICT_MODE);
						if ("false".equalsIgnoreCase(strictModeStr)) {
							if (log.isDebugEnabled()) {
								log.trace("Skipping DataFilterInterceptor because the module is not running in strict mode");
							}
						} else {
							if (filteredByLoc) {
								boolean check = true;
								for (String filterName : locationBasedClassAndFiltersMap.get(entity.getClass())) {
									check = !AccessUtil.isFilterDisabled(filterName);
									if (check) {
										break;
									}
								}
								
								if (check) {
									if (user == null
									        || !AccessUtil.getAccessiblePersonIds(Location.class).contains(id.toString())) {
										throw new ContextAuthenticationException(
										        DataFilterConstants.ILLEGAL_RECORD_ACCESS_MESSAGE);
									}
								}
							}
							
							if (filteredByEnc) {
								boolean check = true;
								for (String filterName : encTypeBasedClassAndFiltersMap.get(entity.getClass())) {
									check = !AccessUtil.isFilterDisabled(filterName);
									if (check) {
										break;
									}
								}
								
								if (check) {
									Encounter encounter;
									if (entity instanceof Encounter) {
										encounter = ((Encounter) entity);
									} else {
										encounter = ((Obs) entity).getEncounter();
									}
									
									if (encounter != null) {
										Privilege requiredPrivilege = encounter.getEncounterType().getViewPrivilege();
										if (requiredPrivilege != null) {
											if (user == null || !user.hasPrivilege(requiredPrivilege.getPrivilege())) {
												throw new ContextAuthenticationException(
												        DataFilterConstants.ILLEGAL_RECORD_ACCESS_MESSAGE);
											}
										}
									}
								}
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
	
}
