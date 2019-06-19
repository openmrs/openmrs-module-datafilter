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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.Daemon;
import org.springframework.stereotype.Component;

/**
 * This interceptor provides a safety net to catch any cases where an entity that the authenticated
 * user has no access to is getting loaded from the DB, by default the module runs in a non strict
 * mode implying that the interceptor is disabled by default, also note that the interceptor isn't
 * applied for super and daemon user.
 */
@Component("dataFilterInterceptor")
public class DataFilterInterceptor extends EmptyInterceptor {
	
	private static final Log log = LogFactory.getLog(DataFilterInterceptor.class);
	
	public static final Set<Class<?>> filteredClasses = Stream.of(Patient.class, Visit.class, Encounter.class, Obs.class)
	        .collect(Collectors.toSet());
	
	/**
	 * @see EmptyInterceptor#onLoad(Object, Serializable, Object[], String[], Type[])
	 */
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		
		if (Daemon.isDaemonThread()) {
			if (log.isDebugEnabled()) {
				log.trace("Skipping DataFilterInterceptor for daemon thread");
			}
		} else if (filteredClasses.contains(entity.getClass())) {
			User user = Context.getAuthenticatedUser();
			if (user != null && user.isSuperUser()) {
				if (log.isDebugEnabled()) {
					log.trace("Skipping DataFilterInterceptor for super user");
				}
			} else {
				AdministrationService as = Context.getAdministrationService();
				Boolean strictMode = as.getGlobalPropertyValue(DataFilterConstants.GP_RUN_IN_STRICT_MODE, false);
				if (!strictMode) {
					if (log.isDebugEnabled()) {
						log.trace("Skipping DataFilterInterceptor because the module is not running in strict mode");
					}
				} else if (user == null || !AccessUtil.getAccessiblePersonIds(Location.class).contains(id.toString())) {
					throw new ContextAuthenticationException(DataFilterConstants.ILLEGAL_RECORD_ACCESS_MESSAGE);
				}
			}
		}
		
		return super.onLoad(entity, id, state, propertyNames, types);
	}
	
}
