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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleException;
import org.openmrs.module.datafilter.filter.FilterAnnotation;
import org.openmrs.module.datafilter.filter.FilterDefAnnotation;

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
		
		addAnnotationToClass(Encounter.class, new FilterDefAnnotation(DataFilterConstants.FILTER_NAME_ENCOUNTER));
		addAnnotationToClass(Encounter.class, new FilterAnnotation(DataFilterConstants.FILTER_NAME_ENCOUNTER));
		
		if (log.isInfoEnabled()) {
			log.info("Successfully added filter annotations");
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
	
	private void addAnnotationToClass(Class<?> clazz, Annotation annotation) {
		
		final String annotationName = annotation.annotationType().getName();
		if (log.isDebugEnabled()) {
			log.debug("Adding " + annotationName + " annotation to " + clazz);
		}
		
		try {
			Method method = Class.class.getDeclaredMethod("getDeclaredAnnotationMap");
			boolean accessible = method.isAccessible();
			try {
				method.setAccessible(true);
				Map<Class<? extends Annotation>, Annotation> map = (Map<Class<? extends Annotation>, Annotation>) method
				        .invoke(clazz);
				//TODO handle the case where the annotation is already present in case of module restart
				//TODO We also need to take of FilterDefs and Filters annotations if present
				map.put(annotation.annotationType(), annotation);
				
				if (log.isDebugEnabled()) {
					log.debug("Successfully added " + annotationName + " annotation to " + clazz);
				}
			}
			catch (InvocationTargetException | IllegalAccessException e) {
				throw e;
			}
			finally {
				//Always reset
				method.setAccessible(accessible);
			}
		}
		catch (ReflectiveOperationException e) {
			throw new ModuleException("Failed to add annotation " + annotation + " to " + clazz, e);
		}
	}
	
}
