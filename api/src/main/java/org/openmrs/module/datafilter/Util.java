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
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.ModuleException;
import org.openmrs.module.datafilter.filter.FilterAnnotation;
import org.openmrs.module.datafilter.filter.FilterDefAnnotation;
import org.openmrs.module.datafilter.filter.FullTextFilterDefAnnotation;
import org.openmrs.module.datafilter.filter.PatientIdFullTextFilter;

public class Util {
	
	private static final Log log = LogFactory.getLog(Util.class);
	
	/**
	 * Adds the filter annotations to persistent classes mapped with JPA annotations that need to be
	 * filtered
	 */
	protected static void addFilterAnnotations() {
		if (log.isInfoEnabled()) {
			log.info("Adding filter annotations");
		}
		
		try {
			//TODO First check if the class has the @Entity annotation before we even bother to add others
			addAnnotationToClass(Encounter.class, new FilterDefAnnotation(DataFilterConstants.FILTER_NAME_ENCOUNTER));
			addAnnotationToClass(Encounter.class, new FilterAnnotation(DataFilterConstants.FILTER_NAME_ENCOUNTER,
			        DataFilterConstants.FILTER_CONDITION_PATIENT_ID));
			
			addAnnotationToClass(Visit.class, new FilterDefAnnotation(DataFilterConstants.FILTER_NAME_VISIT));
			addAnnotationToClass(Visit.class, new FilterAnnotation(DataFilterConstants.FILTER_NAME_VISIT,
			        DataFilterConstants.FILTER_CONDITION_PATIENT_ID));
			
			addAnnotationToClass(Patient.class, new FilterDefAnnotation(DataFilterConstants.FILTER_NAME_PATIENT));
			addAnnotationToClass(Patient.class, new FilterAnnotation(DataFilterConstants.FILTER_NAME_PATIENT,
			        DataFilterConstants.FILTER_CONDITION_PATIENT_ID));
			addAnnotationToClass(Patient.class, new FullTextFilterDefAnnotation(
			        DataFilterConstants.FULL_TEXT_FILTER_NAME_PATIENT, PatientIdFullTextFilter.class));
			
			if (log.isInfoEnabled()) {
				log.info("Successfully added filter annotations");
			}
		}
		catch (ReflectiveOperationException e) {
			throw new ModuleException("Failed to add filter annotations", e);
		}
	}
	
	/**
	 * Adds the specified annotation to the specified class
	 * 
	 * @param clazz the class to add the annotation
	 * @param annotation the annotation to add
	 */
	public static void addAnnotationToClass(Class<?> clazz, Annotation annotation) throws ReflectiveOperationException {
		
		final String annotationName = annotation.annotationType().getName();
		if (log.isDebugEnabled()) {
			log.debug("Adding " + annotationName + " annotation to " + clazz);
		}
		
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
			log.error("Failed to add " + annotationName + " annotation to " + clazz, e);
			throw e;
		}
		finally {
			//Always reset
			method.setAccessible(accessible);
		}
		
	}
	
}
