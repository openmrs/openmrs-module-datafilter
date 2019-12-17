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

import static org.openmrs.module.datafilter.DataFilterConstants.MODULE_ID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.openmrs.module.datafilter.registration.HibernateFilterRegistration;
import org.openmrs.util.OpenmrsClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.stereotype.Component;

/**
 * Custom BeanFactoryPostProcessor that registers filters to hbm files by doing the following for
 * all filtered entities mapped using xml.
 * 
 * <pre>
 * <ul>
 * <li>Find all filtered entities that are mapped using xml and load the contents of their hbm
 * files</li>
 * <li>Apply an xslt to all their hbm files to add the filter tags</li>
 * <li>Write the transformed contents of the hbm files to the temp directory</li>
 * <li>Load OpenMRS core's hibernate.cfg.xml file</li>
 * <li>Apply an xslt to the core hibernate.cfg.xml file to switch each mapping entry for any
 * filtered entity to point to their respective paths of the transformed hbm files created
 * above</li>
 * <li>Write the transformed contents of the hibernate.cfg.xml file to the temp directory</li>
 * <li>Get the session factory bean and change the configLocations property to point to the location
 * of our transformed hibernate.cfg.xml file that references the transformed hbm files containing
 * our filters</li>
 * <ul/>
 * 
 * <pre/>
 */
@Component
public class DataFilterBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
	
	private static final Logger log = LoggerFactory.getLogger(DataFilterBeanFactoryPostProcessor.class);
	
	public static final String SESSION_FACTORY_BEAN_NAME = "sessionFactory";
	
	public static final String CFG_LOC_PROP_NAME = "configLocations";
	
	public static final String CORE_HIBERNATE_CFG_FILE = "hibernate.cfg.xml";
	
	public static final String DATAFILTER_HIBERNATE_CFG_FILE = "datafilterHibernate.cfg.xml";
	
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		log.info("In datafilter's BeanFactoryPostProcessor");
		
		Map<Class, List<HibernateFilterRegistration>> classFiltersMap = Util.getClassFiltersMap();
		if (classFiltersMap.isEmpty()) {
			return;
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Filtered classes with hbm files: " + classFiltersMap.keySet());
		}
		
		log.info("Reconfiguring the sessionFactory bean's configLocations");
		
		final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(SESSION_FACTORY_BEAN_NAME);
		
		ManagedList<TypedStringValue> configLocations = (ManagedList) beanDefinition.getPropertyValues()
		        .get(CFG_LOC_PROP_NAME);
		
		Optional candidate = configLocations.stream()
		        .filter(loc -> ("classpath:" + CORE_HIBERNATE_CFG_FILE).equals(loc.getValue())).findFirst();
		
		if (!candidate.isPresent()) {
			//What? Was the file renamed in core?
			Class<?> beanClass;
			try {
				beanClass = OpenmrsClassLoader.getInstance().loadClass(beanDefinition.getBeanClassName());
			}
			catch (ClassNotFoundException e) {
				throw new BeanCreationException("Failed to reconfigure sessionFactory bean", e);
			}
			
			throw new InvalidPropertyException(beanClass, CFG_LOC_PROP_NAME,
			        CORE_HIBERNATE_CFG_FILE + " entry not found among configLocations");
		}
		
		final String timestamp = new Long(System.currentTimeMillis()).toString();
		
		String newCfgFilePath;
		try {
			
			Map<String, String> oldAndTransformedMappingFiles = createTransformedMappingFiles(classFiltersMap, timestamp);
			
			newCfgFilePath = createTransformedHibernateCfgFile(oldAndTransformedMappingFiles, timestamp);
		}
		finally {
			File transformedFilesDir = FileUtils.getFile(FileUtils.getTempDirectory(), MODULE_ID);
			if (transformedFilesDir.exists()) {
				try {
					FileUtils.forceDeleteOnExit(transformedFilesDir);
				}
				catch (IOException e) {
					//Ignore since it is in the temp dir anyways
				}
			}
		}
		
		configLocations.remove(candidate.get());
		configLocations.add(new TypedStringValue("file:" + newCfgFilePath));
		
		log.info("Successfully reconfigured the sessionFactory bean's configLocations to: " + configLocations.stream()
		        .map(typedStringValue -> typedStringValue.getValue()).collect(Collectors.toList()));
	}
	
	/**
	 * Creates a new transformed hibernate cfg file.
	 * 
	 * @param oldAndTransformedMappingFiles map of previous and respective absolute paths of their new
	 *            hbm file.
	 * @param timestamp the timestamp to use for the output directory name
	 * @return the absolute path of the new hibernate cfg file
	 */
	private static String createTransformedHibernateCfgFile(Map<String, String> oldAndTransformedMappingFiles,
	                                                        String timestamp) {
		
		InputStream in = OpenmrsClassLoader.getInstance().getResourceAsStream(CORE_HIBERNATE_CFG_FILE);
		ByteArrayOutputStream outFinal = null;
		
		for (Map.Entry<String, String> entry : oldAndTransformedMappingFiles.entrySet()) {
			if (outFinal != null) {
				in = new ByteArrayInputStream(outFinal.toByteArray());
			}
			
			ByteArrayOutputStream outTemp = new ByteArrayOutputStream();
			Util.updateResourceLocation(in, entry.getKey(), entry.getValue(), outTemp);
			outFinal = outTemp;
		}
		
		File newCfgFile = FileUtils.getFile(FileUtils.getTempDirectory(), MODULE_ID, timestamp,
		    DATAFILTER_HIBERNATE_CFG_FILE);
		
		try {
			if (log.isDebugEnabled()) {
				log.debug("Hibernate cfg file in use: " + newCfgFile.getAbsolutePath());
			}
			
			FileUtils.writeByteArrayToFile(newCfgFile, outFinal.toByteArray());
		}
		catch (IOException e) {
			throw new BeanCreationException("Failed to create transformed hibernate cfg file", e);
		}
		
		return newCfgFile.getAbsolutePath();
	}
	
	/**
	 * Creates new transformed hbm files that contain our filters
	 *
	 * @param classFiltersMap map of classes and their filter registrations
	 * @param timestamp the timestamp to use for the output directory name
	 * @return map of previous and respective absolute paths of their new hbm file.
	 */
	private static Map<String, String> createTransformedMappingFiles(Map<Class, List<HibernateFilterRegistration>> classFiltersMap,
	                                                                 String timestamp) {
		
		File transformedResourcesRepo = FileUtils.getFile(FileUtils.getTempDirectory(), MODULE_ID, timestamp);
		Map<String, String> oldAndTransformedMappingFiles = new HashMap();
		
		for (Map.Entry<Class, List<HibernateFilterRegistration>> entry : classFiltersMap.entrySet()) {
			//TODO parse the hibernate cfg file once outside of this method and reuse it everywhere,
			//Same for the individual hbm files, Util.getMappingResource should return name and resource map
			//So that we don't have to parse each file again when adding filters.
			String hbmResourceName = Util.getMappingResource(CORE_HIBERNATE_CFG_FILE, entry.getKey().getName());
			if (hbmResourceName == null) {
				throw new BeanCreationException("Failed to find hbm file for: " + entry.getKey());
			}
			
			try {
				File newMappingFile = Util.createNewMappingFile(hbmResourceName, entry.getValue(), transformedResourcesRepo);
				
				if (log.isDebugEnabled()) {
					log.debug("Mapping file in use for " + entry.getKey() + ": " + newMappingFile.getAbsolutePath());
				}
				
				oldAndTransformedMappingFiles.put(hbmResourceName, newMappingFile.getAbsolutePath());
			}
			catch (IOException e) {
				throw new BeanCreationException("Failed to create transformed mapping file for " + entry.getKey(), e);
			}
		}
		
		return oldAndTransformedMappingFiles;
	}
	
}
