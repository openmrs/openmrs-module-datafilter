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
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Entity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
 * Custom BeanFactoryPostProcessor that registers filters to HBM files by doing the following for
 * all filtered entities mapped via xml.
 * 
 * <pre>
 * <ul>
 * <li>Load the existing hbm files for all filtered entities</li>
 * <li>Parse the xml in the hbm files to add the filter tags</li>
 * <li>Write the mutated xml contents with the added filter tags to new hbm files in the module's
 * config directory</li>
 * <li>Load OpenMRS core's hibernate.cfg.xml file</li>
 * <li>Parse the xml in hibernate.cfg.xml file and switch each mapping entry for any filtered entity
 * to point to the respective new hbm file created above</li>
 * <li>Write the mutated hibernate.cfg.xml contents to a new cfg file in the module's config
 * directory</li>
 * <li>Get the session factory bean and change the configLocations property to point to our new
 * mutated hibernate.cfg.xml file that references the new hbm files containing our filters</li>
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
		
		List<HibernateFilterRegistration> filterRegistrations = Util.getHibernateFilterRegistrations();
		if (filterRegistrations.isEmpty()) {
			return;
		}
		
		Map<Class, List<HibernateFilterRegistration>> classFiltersMap = new HashMap();
		
		for (HibernateFilterRegistration filterReg : filterRegistrations) {
			for (Class clazz : filterReg.getTargetClasses()) {
				if (!clazz.isAnnotationPresent(Entity.class)) {
					if (classFiltersMap.get(clazz) == null) {
						classFiltersMap.put(clazz, new ArrayList());
					}
					classFiltersMap.get(clazz).add(filterReg);
				}
			}
		}
		
		if (classFiltersMap.isEmpty()) {
			return;
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Filtered classes with hbm files: " + classFiltersMap.keySet());
		}
		
		Map<String, String> oldAndNewResourceFileMap = new HashMap();
		String timestamp = new Long(System.currentTimeMillis()).toString();
		
		for (Map.Entry<Class, List<HibernateFilterRegistration>> entry : classFiltersMap.entrySet()) {
			String hbmResourceName = Util.getMappingResource(CORE_HIBERNATE_CFG_FILE, entry.getKey().getName());
			if (hbmResourceName == null) {
				throw new BeanCreationException("Failed to find hbm file for: " + entry.getKey());
			}
			
			InputStream in = OpenmrsClassLoader.getInstance().getResourceAsStream(hbmResourceName);
			ByteArrayOutputStream outFinal = null;
			
			try {
				for (HibernateFilterRegistration filterReg : entry.getValue()) {
					if (outFinal != null) {
						in = new ByteArrayInputStream(outFinal.toByteArray());
					}
					
					ByteArrayOutputStream outTemp = new ByteArrayOutputStream();
					Util.addFilterToMappingResource(in, outTemp, filterReg);
					outFinal = outTemp;
				}
			}
			finally {
				IOUtils.closeQuietly(in);
			}
			
			String hbmFilename = hbmResourceName;
			if (hbmFilename.indexOf("/") > 0) {
				hbmFilename = hbmFilename.substring(hbmFilename.lastIndexOf("/"));
			}
			
			File newMappingFile = FileUtils.getFile(FileUtils.getTempDirectory(), MODULE_ID, timestamp, hbmFilename);
			
			try {
				if (log.isDebugEnabled()) {
					log.debug("Mapping file in use for " + entry.getKey() + ": " + newMappingFile.getAbsolutePath());
				}
				FileUtils.writeByteArrayToFile(newMappingFile, outFinal.toByteArray());
				oldAndNewResourceFileMap.put(hbmResourceName, newMappingFile.getAbsolutePath());
			}
			catch (IOException e) {
				throw new BeanCreationException("Failed to create new mapping file for " + entry.getKey(), e);
			}
		}
		
		InputStream in = OpenmrsClassLoader.getInstance().getResourceAsStream(CORE_HIBERNATE_CFG_FILE);
		ByteArrayOutputStream outFinal = null;
		
		try {
			for (Map.Entry<String, String> entry : oldAndNewResourceFileMap.entrySet()) {
				if (outFinal != null) {
					in = new ByteArrayInputStream(outFinal.toByteArray());
				}
				
				ByteArrayOutputStream outTemp = new ByteArrayOutputStream();
				Util.updateResourceLocation(in, entry.getKey(), entry.getValue(), outTemp);
				outFinal = outTemp;
			}
		}
		finally {
			IOUtils.closeQuietly(in);
		}
		
		File newCfgFile = FileUtils.getFile(FileUtils.getTempDirectory(), MODULE_ID, timestamp,
		    DATAFILTER_HIBERNATE_CFG_FILE);
		
		try {
			log.info("Hibernate cfg file in use: " + newCfgFile.getAbsolutePath());
			FileUtils.writeByteArrayToFile(newCfgFile, outFinal.toByteArray());
		}
		catch (IOException e) {
			throw new BeanCreationException("Failed to create hibernate cfg file ", e);
		}
		
		log.info("Reconfiguring the sessionFactory bean's configLocations");
		
		BeanDefinition beanDefinition = beanFactory.getBeanDefinition(SESSION_FACTORY_BEAN_NAME);
		
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
		
		configLocations.remove(candidate.get());
		configLocations.add(new TypedStringValue("file:" + newCfgFile.getAbsolutePath()));
		
		log.info("Successfully reconfigured the sessionFactory bean's configLocations to: " + configLocations.stream()
		        .map(typedStringValue -> typedStringValue.getValue()).collect(Collectors.toList()));
	}
	
}
