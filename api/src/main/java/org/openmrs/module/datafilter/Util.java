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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;
import org.hibernate.cfg.Environment;
import org.hibernate.search.annotations.FullTextFilterDefs;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.module.datafilter.annotations.AggregateAnnotation;
import org.openmrs.module.datafilter.annotations.FilterAnnotation;
import org.openmrs.module.datafilter.annotations.FilterDefAnnotation;
import org.openmrs.module.datafilter.annotations.FilterDefsAnnotation;
import org.openmrs.module.datafilter.annotations.FiltersAnnotation;
import org.openmrs.module.datafilter.annotations.FullTextFilterDefAnnotation;
import org.openmrs.module.datafilter.annotations.FullTextFilterDefsAnnotation;
import org.openmrs.module.datafilter.annotations.ParamDefAnnotation;
import org.openmrs.module.datafilter.registration.FullTextFilterRegistration;
import org.openmrs.module.datafilter.registration.HibernateFilterParameter;
import org.openmrs.module.datafilter.registration.HibernateFilterRegistration;
import org.openmrs.util.OpenmrsClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class Util {
	
	private static final Logger log = LoggerFactory.getLogger(Util.class);
	
	private static final String FILTER_PATH_PREFIX = "classpath*:/filters/";
	
	private static final String FILTER_PATH_SUFFIX = "/*.json";
	
	private static List<HibernateFilterRegistration> hibernateFilterRegistrations;
	
	private static List<FullTextFilterRegistration> fullTextFilterRegistrations;
	
	/**
	 * Checks if the filter matching the specified name is disabled, every filter can be disabled via a
	 * global property, the name of the global property is the filter name with the a dot and disabled
	 * word appended to the end.
	 *
	 * @param filterName the name of the filter to match
	 * @return true if the filter is disabled otherwise false
	 */
	public static boolean isFilterDisabled(String filterName) {
		AdministrationDAO adminDAO = Context.getRegisteredComponent("adminDAO", AdministrationDAO.class);
		List<List<Object>> rows = adminDAO.executeSQL("SELECT property_value FROM global_property WHERE property = '"
		        + filterName + DataFilterConstants.DISABLED + "'",
		    true);
		if (rows.isEmpty() || rows.get(0).isEmpty() || rows.get(0).get(0) == null) {
			return false;
		}
		
		return "true".equalsIgnoreCase(rows.get(0).get(0).toString().trim());
	}
	
	protected static List<HibernateFilterRegistration> getHibernateFilterRegistrations() {
		if (hibernateFilterRegistrations == null) {
			loadFilterRegistrations(true);
		}
		
		return hibernateFilterRegistrations;
	}
	
	protected static List<FullTextFilterRegistration> getFullTextFilterRegistrations() {
		if (fullTextFilterRegistrations == null) {
			loadFilterRegistrations(false);
		}
		
		return fullTextFilterRegistrations;
	}
	
	/**
	 * Adds the defined filter annotations to persistent classes mapped with JPA annotations that need
	 * to be filtered.
	 */
	public static void initializeFilters() throws ReflectiveOperationException {
		log.info("Initializing filters");
		
		//Register hibernate filters
		for (HibernateFilterRegistration registration : getHibernateFilterRegistrations()) {
			if (registration.getProperty() == null) {
				ParamDef[] paramDefs = null;
				if (CollectionUtils.isNotEmpty(registration.getParameters())) {
					paramDefs = new ParamDef[registration.getParameters().size()];
					int index = 0;
					for (HibernateFilterParameter parameter : registration.getParameters()) {
						paramDefs[index] = new ParamDefAnnotation(parameter.getName(), parameter.getType());
						index++;
					}
				}
				
				for (Class clazz : registration.getTargetClasses()) {
					registerFilter(clazz,
					    new FilterDefAnnotation(registration.getName(), registration.getDefaultCondition(), paramDefs),
					    new FilterAnnotation(registration.getName(), registration.getCondition()));
				}
				
			} else {
				//This is a filter to be applied to a property
				if (registration.getTargetClasses().size() > 1) {
					throw new APIException("Only one target class can be defined for a filter added to a property");
				}
				
				try {
					addAnnotationToField(registration.getProperty(), registration.getTargetClasses().get(0),
					    new FilterAnnotation(registration.getName(), registration.getCondition()));
				}
				catch (ReflectiveOperationException e) {
					throw new APIException(e);
				}
			}
		}
		
		//Register full text filters
		for (FullTextFilterRegistration registration : getFullTextFilterRegistrations()) {
			//Full text filters are added to one entity but can be enabled for any entity
			registerFullTextFilter(registration.getTargetClasses().get(0), new FullTextFilterDefAnnotation(
			        registration.getName(), registration.getImplClass(), registration.getCacheMode()));
		}
		
		Context.addConfigProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, DataFilterSessionContext.class.getName());
		
		log.info("Successfully initialized filters");
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
	                                     FilterAnnotation filterAnnotation)
	    throws ReflectiveOperationException {
		
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
	protected static void registerFullTextFilter(Class<?> entityClass, FullTextFilterDefAnnotation filterDefAnnotation)
	    throws ReflectiveOperationException {
		addAnnotationToGroup(entityClass, FullTextFilterDefs.class, filterDefAnnotation);
	}
	
	/**
	 * Utility method that adds a grouped annotation to it's containing aggregate annotation.
	 * 
	 * @param entityClass the class that has the aggregate annotation
	 * @param aggregateAnnClass the aggregate annotation type
	 * @param toAdd the grouped annotation instance to add
	 */
	private static void addAnnotationToGroup(Class<?> entityClass, Class<? extends Annotation> aggregateAnnClass,
	                                         Annotation toAdd)
	    throws ReflectiveOperationException {
		
		Annotation aggregateAnnotation = entityClass.getAnnotation(aggregateAnnClass);
		if (aggregateAnnotation == null) {
			if (FilterDefs.class.equals(aggregateAnnClass)) {
				aggregateAnnotation = new FilterDefsAnnotation();
			} else if (Filters.class.equals(aggregateAnnClass)) {
				aggregateAnnotation = new FiltersAnnotation();
			} else if (FullTextFilterDefs.class.equals(aggregateAnnClass)) {
				aggregateAnnotation = new FullTextFilterDefsAnnotation();
			}
			
			addAnnotationToClass(entityClass, aggregateAnnotation);
		}
		
		((AggregateAnnotation) aggregateAnnotation).add(toAdd);
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
			log.debug("Adding " + annotationName + " annotation to " + clazz.getName());
		}
		
		Method method = Class.class.getDeclaredMethod("getDeclaredAnnotationMap");
		boolean accessible = method.isAccessible();
		try {
			method.setAccessible(true);
			Map<Class<? extends Annotation>, Annotation> map = (Map<Class<? extends Annotation>, Annotation>) method
			        .invoke(clazz);
			//TODO handle the case where the annotation is already present in case of module restart
			map.put(annotation.annotationType(), annotation);
			
			if (log.isDebugEnabled()) {
				log.debug("Successfully added " + annotationName + " annotation to " + clazz.getName());
			}
		}
		catch (InvocationTargetException | IllegalAccessException e) {
			log.error("Failed to add " + annotationName + " annotation to " + clazz.getName(), e);
			throw e;
		}
		finally {
			//Always reset
			method.setAccessible(accessible);
		}
		
	}
	
	/**
	 * Adds an annotation to the field with a matching name in the specified class
	 * 
	 * @param fieldName the name of the field
	 * @param clazz the class to add the annotation
	 * @param annotation the annotation to add
	 */
	private static void addAnnotationToField(String fieldName, Class<?> clazz, Annotation annotation)
	    throws ReflectiveOperationException {
		
		final String annotationName = annotation.annotationType().getName();
		if (log.isDebugEnabled()) {
			log.debug("Adding " + annotationName + " annotation to " + clazz.getName() + "." + fieldName);
		}
		
		Field field = clazz.getDeclaredField(fieldName);
		boolean fieldAccessible = field.isAccessible();
		field.setAccessible(true);
		Method method = Field.class.getDeclaredMethod("declaredAnnotations");
		boolean methodAccessible = method.isAccessible();
		method.setAccessible(true);
		try {
			Map<Class<? extends Annotation>, Annotation> map = (Map<Class<? extends Annotation>, Annotation>) method
			        .invoke(field);
			
			//TODO handle the case where the annotation is already present in case of module restart
			//TODO We also need to take care of Filters annotations if present
			map.put(annotation.annotationType(), annotation);
			
			if (log.isDebugEnabled()) {
				log.debug("Successfully added " + annotationName + " annotation to " + clazz.getName() + "." + fieldName);
			}
		}
		catch (InvocationTargetException | IllegalAccessException e) {
			log.error("Failed to add " + annotationName + " annotation to " + clazz.getName() + "." + fieldName, e);
			throw e;
		}
		finally {
			//Always reset
			field.setAccessible(fieldAccessible);
			method.setAccessible(methodAccessible);
		}
	}
	
	/**
	 * Loads the registered filters in the json files
	 * 
	 * @param isHibernate specifies whether hibernate or full text filters are the ones to load
	 */
	private static synchronized void loadFilterRegistrations(boolean isHibernate) {
		if (log.isDebugEnabled()) {
			log.debug("Loading " + (isHibernate ? "hibernate" : "full text") + " filter registrations");
		}
		
		if (isHibernate) {
			if (hibernateFilterRegistrations != null) {
				return;
			}
			hibernateFilterRegistrations = new ArrayList();
		} else {
			if (fullTextFilterRegistrations != null) {
				return;
			}
			fullTextFilterRegistrations = new ArrayList();
		}
		
		//During openmrs Installation or upgrade, the thread context classloader is that of the webapp assigned
		//by the servlet container which doesn't know about module resources, so we need to use the openmrs one.
		//TODO See TRUNK-5678, when it is done then we can remove this logic
		ClassLoader classLoader = OpenmrsClassLoader.getInstance();
		PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(classLoader);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		//See here we need to use the openmrs classloader to load the type field in case of module classes
		TypeFactory typeFactory = mapper.getTypeFactory().withClassLoader(classLoader);
		mapper.setTypeFactory(typeFactory);
		
		final String pathPattern = FILTER_PATH_PREFIX + (isHibernate ? "hibernate" : "fulltext") + FILTER_PATH_SUFFIX;
		
		try {
			Resource[] resources = resourceResolver.getResources(pathPattern);
			for (Resource resource : resources) {
				Class clazz = isHibernate ? HibernateFilterRegistration.class : FullTextFilterRegistration.class;
				JavaType classListType = typeFactory.constructCollectionType(List.class, clazz);
				if (isHibernate) {
					hibernateFilterRegistrations.addAll(mapper.readValue(resource.getInputStream(), classListType));
				} else {
					fullTextFilterRegistrations.addAll(mapper.readValue(resource.getInputStream(), classListType));
				}
			}
		}
		catch (IOException e) {
			throw new APIException(
			        "Failed to load some " + (isHibernate ? "hibernate" : "full text") + " filter registrations", e);
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Successfully loaded " + (isHibernate ? "hibernate" : "full text") + " filter registrations");
		}
	}
	
}
