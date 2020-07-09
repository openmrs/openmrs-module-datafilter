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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.openmrs.module.datafilter.registration.HibernateFilterRegistration;
import org.openmrs.util.OpenmrsClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.stereotype.Component;

/**
 * Custom BeanFactoryPostProcessor that registers filters to hbm files by doing the following for
 * all filtered entities mapped using xml. <pre>
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
 * <pre/>
 */
@Component
public class DataFilterBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
	
	private static final Logger log = LoggerFactory.getLogger(DataFilterBeanFactoryPostProcessor.class);
	
	public static final String SESSION_FACTORY_BEAN_NAME = "sessionFactory";
	
	public static final String CFG_LOC_PROP_NAME = "configLocations";
	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		log.info("In datafilter's BeanFactoryPostProcessor");
		
		try {
			Util.initializeFilters();
		}
		catch (ReflectiveOperationException e) {
			throw new BeanCreationException("Failed to initialize filters", e);
		}
		
		Map<Class, List<HibernateFilterRegistration>> classFiltersMap = Util.getClassFiltersMap();
		if (classFiltersMap.isEmpty()) {
			return;
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Filtered classes with hbm files: " + classFiltersMap.keySet());
		}
		
		log.info("Reconfiguring the sessionFactory bean's configLocations");
		
		final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(SESSION_FACTORY_BEAN_NAME);
		
		ManagedList<TypedStringValue> configLocationsList = (ManagedList) beanDefinition.getPropertyValues()
		        .get(CFG_LOC_PROP_NAME);
		
		List<String> hbmConfigFiles = new ArrayList(configLocationsList.size());
		configLocationsList.stream().forEach(loc -> hbmConfigFiles.add(loc.getValue().substring(10)));
		
		log.info("Hibernate Config locations: " + hbmConfigFiles);
		
		final String timestamp = new Long(System.currentTimeMillis()).toString();
		
		Map<String, Map<String, String>> cfgAndOldAndTransformedMappingFiles = new HashMap();
		for (Map.Entry<Class, List<HibernateFilterRegistration>> entry : classFiltersMap.entrySet()) {
			//TODO parse the hibernate cfg file once outside of this method and reuse it everywhere,
			//Same for the individual hbm files, Util.getMappingResource should return name and resource map
			//So that we don't have to parse each file again when adding filters.
			String className = entry.getKey().getName();
			if (log.isDebugEnabled()) {
				log.debug("Looking up mapping resource for: " + className);
			}
			
			String hbmResourceName = null;
			String hbmConfigFile = null;
			for (String candidate : hbmConfigFiles) {
				hbmResourceName = Util.getMappingResource(candidate, className);
				if (hbmResourceName != null) {
					if (log.isDebugEnabled()) {
						log.debug("Found mapping resource for " + className + " in config file: " + candidate);
					}
					
					hbmConfigFile = candidate;
					break;
				}
			}
			
			if (hbmResourceName == null) {
				//This is most likely a filter to be added to a module resource
				//TODO keep track of skipped module resources so we can actually catch bad filter registrations 
				continue;
			}
			
			String newMappingFile = createTransformedMappingFiles(hbmResourceName, timestamp, entry.getValue());
			if (cfgAndOldAndTransformedMappingFiles.get(hbmConfigFile) == null) {
				cfgAndOldAndTransformedMappingFiles.put(hbmConfigFile, new HashMap());
			}
			
			cfgAndOldAndTransformedMappingFiles.get(hbmConfigFile).put(hbmResourceName, newMappingFile);
		}
		
		log.info("Hibernate cfg files and their old And transformed mapping files: " + cfgAndOldAndTransformedMappingFiles);
		
		if (cfgAndOldAndTransformedMappingFiles.isEmpty()) {
			return;
		}
		
		for (Map.Entry<String, Map<String, String>> entry : cfgAndOldAndTransformedMappingFiles.entrySet()) {
			log.info(entry.getKey() + " -> " + entry.getValue());
			if (entry.getValue().isEmpty()) {
				continue;
			}
			
			String newCfgFilePath;
			try {
				newCfgFilePath = createTransformedHibernateCfgFile(entry.getValue(), timestamp, entry.getKey());
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
			
			configLocationsList.remove(new TypedStringValue("classpath:" + entry.getKey()));
			configLocationsList.add(new TypedStringValue("file:" + newCfgFilePath));
		}
		
		File transformedResourcesRepo = FileUtils.getFile(FileUtils.getTempDirectory(), MODULE_ID, timestamp);
		beanDefinition.getPropertyValues().addPropertyValue("filteredResourcesLocation",
		    transformedResourcesRepo.getAbsolutePath());
		
		beanDefinition.setBeanClassName(DataFilterSessionFactoryBean.class.getName());
		
		log.info("Successfully reconfigured the sessionFactory bean's configLocations to: " + configLocationsList.stream()
		        .map(typedStringValue -> typedStringValue.getValue()).collect(Collectors.toList()));
	}
	
	/**
	 * Creates a new transformed hibernate cfg file.
	 * 
	 * @param oldAndTransformedMappingFiles map of previous and respective absolute paths of their new
	 *            hbm file.
	 * @param timestamp the timestamp to use for the output directory name
	 * @param cfgFile the source hibernate cfg file
	 * @return the absolute path of the new hibernate cfg file
	 */
	private static String createTransformedHibernateCfgFile(Map<String, String> oldAndTransformedMappingFiles,
	        String timestamp, String cfgFile) {
		
		InputStream in = OpenmrsClassLoader.getInstance().getResourceAsStream(cfgFile);
		ByteArrayOutputStream outFinal = null;
		
		for (Map.Entry<String, String> entry : oldAndTransformedMappingFiles.entrySet()) {
			if (outFinal != null) {
				in = new ByteArrayInputStream(outFinal.toByteArray());
			}
			
			ByteArrayOutputStream outTemp = new ByteArrayOutputStream();
			Util.updateResourceLocation(in, entry.getKey(), entry.getValue(), outTemp);
			outFinal = outTemp;
		}
		
		File newCfgFile = FileUtils.getFile(FileUtils.getTempDirectory(), MODULE_ID, timestamp, MODULE_ID + "-" + cfgFile);
		
		try {
			if (log.isDebugEnabled()) {
				log.debug("Hibernate cfg file " + cfgFile + " replaced with: " + newCfgFile.getAbsolutePath());
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
	 * @param hbmResourceName the name of the original mapping resource
	 * @param timestamp the timestamp to use for the output directory name
	 * @param filterRegs the list of filters registrations for the class associated to the mapping file
	 * @return map of previous and respective absolute paths of their new hbm file.
	 */
	private static String createTransformedMappingFiles(String hbmResourceName, String timestamp,
	        List<HibernateFilterRegistration> filterRegs) {
		
		File transformedResourcesRepo = FileUtils.getFile(FileUtils.getTempDirectory(), MODULE_ID, timestamp);
		
		try {
			File newMappingFile = Util.createNewMappingFile(hbmResourceName, filterRegs, transformedResourcesRepo);
			
			if (log.isDebugEnabled()) {
				log.debug("Mapping resource " + hbmResourceName + ": replaced with:" + newMappingFile.getAbsolutePath());
			}
			return newMappingFile.getAbsolutePath();
		}
		catch (IOException e) {
			throw new BeanCreationException("Failed to create transformed mapping file for " + hbmResourceName, e);
		}
	}
	
}
