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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.datafilter.DataFilterBeanFactoryPostProcessor.CFG_LOC_PROP_NAME;
import static org.openmrs.module.datafilter.DataFilterBeanFactoryPostProcessor.SESSION_FACTORY_BEAN_NAME;
import static org.openmrs.module.datafilter.Util.getDocumentBuilder;

import java.io.FileInputStream;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedList;
import org.w3c.dom.Document;

public class DataFilterBeanFactoryPostProcessorTest {
	
	private DataFilterBeanFactoryPostProcessor processor;
	
	@Mock
	private ConfigurableListableBeanFactory mockFactory;
	
	@Mock
	private BeanDefinition mockBeanDefinition;
	
	public class MockEntity {
		
	}
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(mockFactory.getBeanDefinition(SESSION_FACTORY_BEAN_NAME)).thenReturn(mockBeanDefinition);
		processor = new DataFilterBeanFactoryPostProcessor();
	}
	
	@Test
	@Ignore
	public void postProcessBeanFactory_shouldRegisterFiltersToMappingResourceFiles() throws Exception {
		ManagedList<TypedStringValue> configLocations = new ManagedList();
		final String hbmCfgFile = "hibernate.cfg.xml";
		final String testHbmCfgFile = "testHibernateCfg.xml";
		configLocations.add(new TypedStringValue("classpath:" + hbmCfgFile));
		configLocations.add(new TypedStringValue("classpath:" + testHbmCfgFile));
		MutablePropertyValues propertyValues = new MutablePropertyValues();
		propertyValues.add(CFG_LOC_PROP_NAME, configLocations);
		when(mockBeanDefinition.getPropertyValues()).thenReturn(propertyValues);
		
		processor.postProcessBeanFactory(mockFactory);
		
		Optional<TypedStringValue> dataFilterHbmCfgFile = configLocations.stream()
		        .filter(loc -> (loc.getValue()).contains("datafilter-" + hbmCfgFile)).findFirst();
		assertNotNull(dataFilterHbmCfgFile.get().getValue());
		
		String resource = dataFilterHbmCfgFile.get().getValue();
		Document doc = getDocumentBuilder().parse(new FileInputStream(resource.substring(5)));
		assertEquals(6, UtilTest.getCount(doc, "/hibernate-configuration/session-factory/mapping[@file]"));
		
		Optional<TypedStringValue> dataFilterTestHbmCfgFile = configLocations.stream()
		        .filter(loc -> (loc.getValue()).contains("datafilter-" + testHbmCfgFile)).findFirst();
		assertNotNull(dataFilterTestHbmCfgFile.get().getValue());
		
		resource = dataFilterTestHbmCfgFile.get().getValue();
		doc = getDocumentBuilder().parse(new FileInputStream(resource.substring(5)));
		assertTrue(UtilTest.getAttribute(doc, "/hibernate-configuration/session-factory/mapping", "file")
		        .contains("MockEntityResource.hbm.xml"));
		assertNotNull(propertyValues.getPropertyValue("filteredResourcesLocation"));
		verify(mockBeanDefinition, times(1)).setBeanClassName(DataFilterSessionFactoryBean.class.getName());
	}
	
}
