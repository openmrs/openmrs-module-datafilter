/*
 * Add Copyright
 */
package org.openmrs.module.datafilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;

public class DataFilterActivator extends BaseModuleActivator {
	
	protected Log log = LogFactory.getLog(getClass());
	
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
		if (log.isInfoEnabled()) {
			log.info("Adding filter annotations");
		}
		//TODO Apply annotations
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
