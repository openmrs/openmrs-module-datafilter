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

import org.openmrs.api.APIException;
import org.openmrs.module.BaseModuleActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataFilterActivator extends BaseModuleActivator {
	
	private static final Logger log = LoggerFactory.getLogger(DataFilterActivator.class);
	
	/**
	 * @see BaseModuleActivator#started()
	 */
	@Override
	public void started() {
		log.info("Data Filter Module started");
	}
	
	/**
	 * @see BaseModuleActivator#stopped()
	 */
	@Override
	public void stopped() {
		log.info("Data Filter Module stopped");
	}
	
	/**
	 * @see BaseModuleActivator#willRefreshContext() ()
	 */
	@Override
	public void willRefreshContext() {
		log.info("Start: Data Filter Module willRefreshContext");
		
		try {
			Util.initializeFilters();
		}
		catch (ReflectiveOperationException e) {
			throw new APIException(e);
		}
		
		log.info("End: Data Filter Module willRefreshContext");
	}
	
	/**
	 * @see BaseModuleActivator#willStop()
	 */
	@Override
	public void willStop() {
		if (log.isInfoEnabled()) {
			log.info("Removing filter annotations");
		}
		//TODO Remove Annotations
	}
	
}
