/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.impl.api.db.hibernate;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.api.context.Context;

import javax.persistence.FlushModeType;

final class InterceptorUtil {
	
	/**
	 * Gets a GP value from the DB without triggering a hibernate flush.
	 * 
	 * @param gpName the name of the global property
	 * @return the global property value
	 */
	public static String getGpValueNoFlush(String gpName) {
		Session session = Context.getRegisteredComponents(SessionFactory.class).get(0).getCurrentSession();
		//Hibernate will flush any changes in the current session before querying the DB when fetching
		//the GP value below and we end up in this method again, therefore we need to disable auto flush
		final FlushModeType flushMode = session.getFlushMode();
		session.setFlushMode(FlushMode.MANUAL);
		try {
			return Context.getAdministrationService().getGlobalProperty(gpName);
		}
		finally {
			//reset
			session.setFlushMode(flushMode);
		}
	}
	
}
