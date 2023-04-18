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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmrs.api.db.hibernate.HibernateSessionFactoryBean;
import org.openmrs.module.datafilter.registration.HibernateFilterRegistration;
import org.openmrs.util.OpenmrsClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class DataFilterSessionFactoryBean extends HibernateSessionFactoryBean {
	
	private static final Logger log = LoggerFactory.getLogger(DataFilterSessionFactoryBean.class);
	
	private String filteredResourcesLocation;
	
	/**
	 * Sets the filteredResourcesLocation
	 *
	 * @param filteredResourcesLocation the filteredResourcesLocation to set
	 */
	public void setFilteredResourcesLocation(String filteredResourcesLocation) {
		this.filteredResourcesLocation = filteredResourcesLocation;
	}
	
	/**
	 * @see HibernateSessionFactoryBean#setMappingResources(String...)
	 */
	@Override
	public void setMappingResources(String... mappingResources) {
		Map<Class<?>, List<HibernateFilterRegistration>> classFiltersMap = Util.getClassFiltersMap();
		if (classFiltersMap.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("No registered filters found for hbm files");
			}
			
			super.setMappingResources(mappingResources);
			return;
		}
		
		List<String> nonFilteredModuleResources = new ArrayList<>();
		List<String> filteredModuleHbmFiles = new ArrayList<>();
		File outputDir = new File(filteredResourcesLocation);
		
		for (String resource : mappingResources) {
			String classname = Util.getMappedClassName(resource);
			if (classname == null) {
				//Some module hbm files are actually empty
				nonFilteredModuleResources.add(resource);
				continue;
			}
			
			try {
				Class<?> clazz = OpenmrsClassLoader.getInstance().loadClass(classname);
				if (classFiltersMap.get(clazz) == null) {
					nonFilteredModuleResources.add(resource);
					continue;
				}
				
				File newMappingFile = Util.createNewMappingFile(resource, classFiltersMap.get(clazz), outputDir);
				if (log.isDebugEnabled()) {
					log.debug("Mapping file in use for {}: {}", clazz, newMappingFile.getAbsolutePath());
				}
				
				filteredModuleHbmFiles.add(newMappingFile.getAbsolutePath());
			}
			catch (Exception e) {
				throw new BeanCreationException(
				        "Failed to create transformed mapping file for " + classname + ", " + resource, e);
			}
		}
		
		this.mappingResources.clear();
		super.setMappingResources(nonFilteredModuleResources.toArray(new String[] {}));
		
		List<Resource> resourcesLocations = new ArrayList<>();
		for (String hbmfilePath : filteredModuleHbmFiles) {
			resourcesLocations.add(new FileSystemResource(hbmfilePath));
		}
		
		super.setMappingLocations(resourcesLocations.toArray(new Resource[] {}));
	}
	
}
