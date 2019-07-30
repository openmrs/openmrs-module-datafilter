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
import org.hibernate.cfg.Environment;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.datafilter.annotations.FilterDefsAnnotation;
import org.openmrs.module.datafilter.annotations.FiltersAnnotation;
import org.openmrs.module.datafilter.annotations.FullTextFilterDefsAnnotation;

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
		//TODO Possibly the module should use a configuration file where other modules
		//or admins can register their own filters
		try {
			//TODO First check if the class has the @Entity annotation before we even bother to add others
			Util.addAnnotationToClass(Patient.class, new FilterDefsAnnotation());
			Util.addAnnotationToClass(Patient.class, new FiltersAnnotation());
			Util.addAnnotationToClass(Patient.class, new FullTextFilterDefsAnnotation());
			Util.addAnnotationToClass(Visit.class, new FilterDefsAnnotation());
			Util.addAnnotationToClass(Visit.class, new FiltersAnnotation());
			Util.addAnnotationToClass(Encounter.class, new FilterDefsAnnotation());
			Util.addAnnotationToClass(Encounter.class, new FiltersAnnotation());
			Util.addAnnotationToClass(Obs.class, new FilterDefsAnnotation());
			Util.addAnnotationToClass(Obs.class, new FiltersAnnotation());
		}
		catch (ReflectiveOperationException e) {
			throw new APIException(e);
		}
		
		Util.configureLocationBasedFiltering();
		
		Util.configureEncounterTypeViewPrivilegeBasedFiltering();
		
		Context.addConfigProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, DataFilterSessionContext.class.getName());
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
