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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.Filters;
import org.hibernate.search.annotations.FullTextFilterDefs;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.datafilter.annotations.AggregateAnnotation;
import org.openmrs.module.datafilter.annotations.FilterAnnotation;
import org.openmrs.module.datafilter.annotations.FilterDefAnnotation;
import org.openmrs.module.datafilter.annotations.FullTextFilterDefAnnotation;

public class Util {
	
	private static final Log log = LogFactory.getLog(Util.class);
	
	/**
	 * Sets up location based filtering by adding the filter annotations to persistent classes mapped
	 * with JPA annotations that need to be filtered.
	 */
	protected static void configureLocationBasedFiltering() {
		if (log.isInfoEnabled()) {
			log.info("Setting up location based filtering");
		}
		
		registerFilter(Visit.class, new FilterDefAnnotation(DataFilterConstants.LOCATION_BASED_FILTER_NAME_VISIT),
		    new FilterAnnotation(DataFilterConstants.LOCATION_BASED_FILTER_NAME_VISIT,
		            DataFilterConstants.FILTER_CONDITION_PATIENT_ID));
		
		registerFilter(Encounter.class, new FilterDefAnnotation(DataFilterConstants.LOCATION_BASED_FILTER_NAME_ENCOUNTER),
		    new FilterAnnotation(DataFilterConstants.LOCATION_BASED_FILTER_NAME_ENCOUNTER,
		            DataFilterConstants.FILTER_CONDITION_PATIENT_ID));
		
		registerFilter(Obs.class, new FilterDefAnnotation(DataFilterConstants.LOCATION_BASED_FILTER_NAME_OBS),
		    new FilterAnnotation(DataFilterConstants.LOCATION_BASED_FILTER_NAME_OBS,
		            StringUtils.replaceOnce(DataFilterConstants.FILTER_CONDITION_PATIENT_ID, "patient_id", "person_id")));
		
		registerFilter(Patient.class, new FilterDefAnnotation(DataFilterConstants.LOCATION_BASED_FILTER_NAME_PATIENT),
		    new FilterAnnotation(DataFilterConstants.LOCATION_BASED_FILTER_NAME_PATIENT,
		            DataFilterConstants.FILTER_CONDITION_PATIENT_ID));
		
		registerFullTextFilter(Patient.class, new FullTextFilterDefAnnotation(
		        DataFilterConstants.LOCATION_BASED_FULL_TEXT_FILTER_NAME_PATIENT, PatientIdFullTextFilter.class));
		
		if (log.isInfoEnabled()) {
			log.info("Successfully set up location based filtering");
		}
	}
	
	/**
	 * Sets up privilege based filtering by adding the filter annotations to persistent classes mapped
	 * with JPA annotations that need to be filtered.
	 */
	protected static void configurePrivilegeBasedFiltering() {
		
	}
	
	/**
	 * Adds the specified {@link org.hibernate.annotations.FilterDef} and
	 * {@link org.hibernate.annotations.Filter} annotations to the specified class object.
	 * 
	 * @param entityClass the class object to add the annotations
	 * @param filterDefAnnotation the {@link org.hibernate.annotations.FilterDef} annotation to add
	 * @param filterAnnotation the {@link org.hibernate.annotations.Filter} annotation to add
	 */
	protected static void registerFilter(Class<?> entityClass, FilterDefAnnotation filterDefAnnotation,
	                                     FilterAnnotation filterAnnotation) {
		
		addAnnotationToGroup(entityClass, FilterDefs.class, filterDefAnnotation);
		addAnnotationToGroup(entityClass, Filters.class, filterAnnotation);
	}
	
	/**
	 * Adds the specified {@link org.hibernate.search.annotations.FullTextFilterDef} annotation to the
	 * specified class object.
	 *
	 * @param entityClass the class object to add the annotation
	 * @param filterDefAnnotation the {@link org.hibernate.search.annotations.FullTextFilterDef}
	 *            annotation to add
	 */
	protected static void registerFullTextFilter(Class<?> entityClass, FullTextFilterDefAnnotation filterDefAnnotation) {
		addAnnotationToGroup(entityClass, FullTextFilterDefs.class, filterDefAnnotation);
	}
	
	/**
	 * Utility method that adds a grouped annotation to it's containing aggregate annotation.
	 * 
	 * @param entityClass the class that has the aggregate annotation
	 * @param aggregateAnnotationClass the aggregate annotation type
	 * @param toAdd the grouped annotation instance to add
	 * @param <A>
	 */
	private static <A extends Annotation> void addAnnotationToGroup(Class<?> entityClass, Class<A> aggregateAnnotationClass,
	                                                                Object toAdd) {
		
		A aggregateAnnotation = entityClass.getAnnotation(aggregateAnnotationClass);
		((AggregateAnnotation) aggregateAnnotation).add(toAdd);
	}
	
	/**
	 * Adds the specified annotation to the specified class
	 * 
	 * @param clazz the class to add the annotation
	 * @param annotation the annotation to add
	 */
	protected static void addAnnotationToClass(Class<?> clazz, Annotation annotation) throws ReflectiveOperationException {
		
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
			//TODO We also need to take care of FilterDefs and Filters annotations if present
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
