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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleException;

public class DataFilterActivator extends BaseModuleActivator {
	
	private static final Log log = LogFactory.getLog(DataFilterActivator.class);
	
	/**
	 * @see BaseModuleActivator#started()
	 */
	@Override
	public void started() {
		if (log.isInfoEnabled()) {
			log.info("Data Filter Module started");
		}
	}
	
	/**
	 * @see BaseModuleActivator#stopped()
	 */
	@Override
	public void stopped() {
		if (log.isInfoEnabled()) {
			log.info("Data Filter Module stopped");
		}
	}
	
	/**
	 * @see BaseModuleActivator#willStart()
	 */
	@Override
	public void willStart() {
		
		try {
			Util.loadJavaAgent();
		}
		catch (Exception e) {
			throw new ModuleException("Failed to load java agent", e);
		}
		
		try {
			Util.addFilterAnnotations();
		}
		catch (ReflectiveOperationException e) {
			throw new ModuleException("Failed to add filter annotations");
		}
		
	}
	
	/**
	 * @see BaseModuleActivator#willStop()
	 */
	@Override
	public void willStop() {
		if (log.isInfoEnabled()) {
			log.info("Removing filter annotations");
		}
		//Remove Annotations
	}
	
}
