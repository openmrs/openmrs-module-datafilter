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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.datafilter.registration.HibernateFilterRegistration;
import org.openmrs.util.OpenmrsClassLoader;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*" })
@PrepareForTest({ Util.class, OpenmrsClassLoader.class })
public class DataFilterSessionFactoryBeanTest {
	
	@Mock
	private Logger mockLogger;
	
	@Mock
	private OpenmrsClassLoader mockClassLoader;
	
	private DataFilterSessionFactoryBean sessionFactoryBean;
	
	private static class Module1Entity {}
	
	private static class UnfilteredModule1Entity {}
	
	private static class Module2Entity {}
	
	private AutoCloseable mocksClosable = null;
	
	@Before
	public void setup() {
		sessionFactoryBean = new DataFilterSessionFactoryBean();
		
		mocksClosable = MockitoAnnotations.openMocks(this);
		mockStatic(Util.class);
		mockStatic(OpenmrsClassLoader.class);
		Whitebox.setInternalState(DataFilterSessionFactoryBean.class, Logger.class, mockLogger);
	}
	
	@After
	public void tearDown() throws Exception {
		if (mocksClosable != null) {
			mocksClosable.close();
		}
	}
	
	@Test
	public void setMappingResources_shouldDoNothingIfNoModuleFilterRegistrationsAreFound() {
		when(mockLogger.isDebugEnabled()).thenReturn(true);
		when(Util.getClassFiltersMap()).thenReturn(Collections.emptyMap());
		
		sessionFactoryBean.setMappingResources();
		
		verify(mockLogger, times(1)).debug("No registered filters found for hbm files");
	}
	
	@Test
	public void setMappingResources_shouldRegisterModuleFilters() throws Exception {
		final String module1EntityHbmFile = "Module1Entity.hbm.xml";
		final String module1UnFilteredEntityHbmFile = "Module1UnFilteredEntity.hbm.xml";
		final String module2EntityHbmFile = "Module2Entity.hbm.xml";
		final String filteredResourcesLocation = "/some/path";
		final Set<String> mappingResources = Stream
		        .of(module1EntityHbmFile, module1UnFilteredEntityHbmFile, module2EntityHbmFile).collect(Collectors.toSet());
		Whitebox.setInternalState(sessionFactoryBean, "mappingResources", mappingResources);
		Map<Class<?>, List<HibernateFilterRegistration>> classFiltersMap = new HashMap<>();
		List<HibernateFilterRegistration> module1EntityFilters = Collections
		        .singletonList(mock(HibernateFilterRegistration.class));
		List<HibernateFilterRegistration> module2EntityFilters = Collections
		        .singletonList(mock(HibernateFilterRegistration.class));
		classFiltersMap.put(Module1Entity.class, module1EntityFilters);
		classFiltersMap.put(Module2Entity.class, module2EntityFilters);
		when(Util.getClassFiltersMap()).thenReturn(classFiltersMap);
		sessionFactoryBean.setFilteredResourcesLocation(filteredResourcesLocation);
		when(Util.getMappedClassName(module1EntityHbmFile)).thenReturn(Module1Entity.class.getName());
		when(Util.getMappedClassName(module1UnFilteredEntityHbmFile)).thenReturn(UnfilteredModule1Entity.class.getName());
		when(Util.getMappedClassName(module2EntityHbmFile)).thenReturn(Module2Entity.class.getName());
		when(OpenmrsClassLoader.getInstance()).thenReturn(mockClassLoader);
		when(mockClassLoader.loadClass(Module1Entity.class.getName())).thenReturn((Class) Module1Entity.class);
		when(mockClassLoader.loadClass(UnfilteredModule1Entity.class.getName()))
		        .thenReturn((Class) UnfilteredModule1Entity.class);
		when(mockClassLoader.loadClass(Module2Entity.class.getName())).thenReturn((Class) Module2Entity.class);
		String expectedModule1ResourcePath = filteredResourcesLocation + "/" + module1EntityHbmFile;
		File newModule1EntityHbmFile = mock(File.class);
		when(newModule1EntityHbmFile.getAbsolutePath()).thenReturn(expectedModule1ResourcePath);
		String expectedModule2ResourcePath = filteredResourcesLocation + "/" + module2EntityHbmFile;
		File newModule2EntityHbmFile = mock(File.class);
		when(newModule2EntityHbmFile.getAbsolutePath()).thenReturn(expectedModule2ResourcePath);
		when(Util.createNewMappingFile(eq(module1EntityHbmFile), eq(module1EntityFilters), any(File.class)))
		        .thenReturn(newModule1EntityHbmFile);
		when(Util.createNewMappingFile(eq(module2EntityHbmFile), anyList(), any(File.class)))
		        .thenReturn(newModule2EntityHbmFile);
		
		sessionFactoryBean.setMappingResources(mappingResources.toArray(new String[] {}));
		
		Resource[] mappingLocations = Whitebox.getInternalState(sessionFactoryBean, "mappingLocations");
		assertEquals(2, mappingLocations.length);
		assertNotNull(Stream.of(mappingLocations)
		        .filter(r -> ((FileSystemResource) r).getPath().equals(expectedModule1ResourcePath)).findFirst().get());
		assertNotNull(Stream.of(mappingLocations)
		        .filter(r -> ((FileSystemResource) r).getPath().equals(expectedModule2ResourcePath)).findFirst().get());
		assertEquals(1, mappingResources.size());
		assertEquals(module1UnFilteredEntityHbmFile, mappingResources.iterator().next());
	}
	
}
