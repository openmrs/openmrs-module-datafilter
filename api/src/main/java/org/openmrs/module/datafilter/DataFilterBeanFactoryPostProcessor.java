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

import java.util.Optional;
import java.util.stream.Collectors;

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
	
	private static final String SESSION_FACTORY_BEAN_NAME = "sessionFactory";
	
	private static final String CFG_LOC_PROP_NAME = "configLocations";
	
	private static final String CORE_HIBERNATE_CFG_FILE = "classpath:hibernate.cfg.xml";
	
	private static final String DATAFILTER_HIBERNATE_CFG_FILE = "classpath:datafilterHibernate.cfg.xml";
	
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		log.info("Reconfiguring the sessionFactory bean's configLocations");
		
		BeanDefinition beanDefinition = beanFactory.getBeanDefinition(SESSION_FACTORY_BEAN_NAME);
		ManagedList<TypedStringValue> configLocations = (ManagedList) beanDefinition.getPropertyValues()
		        .get(CFG_LOC_PROP_NAME);
		
		Optional candidate = configLocations.stream().filter(loc -> CORE_HIBERNATE_CFG_FILE.equals(loc.getValue()))
		        .findFirst();
		
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
		configLocations.add(new TypedStringValue(DATAFILTER_HIBERNATE_CFG_FILE));
		
		log.info("Successfully reconfigured the sessionFactory bean's configLocations to: " + configLocations.stream()
		        .map(typedStringValue -> typedStringValue.getValue()).collect(Collectors.toList()));
	}
	
}
