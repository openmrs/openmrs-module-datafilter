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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.openmrs.module.datafilter.DataFilterBeanFactoryPostProcessor.CFG_LOC_PROP_NAME;
import static org.openmrs.module.datafilter.DataFilterBeanFactoryPostProcessor.CORE_HIBERNATE_CFG_FILE;
import static org.openmrs.module.datafilter.DataFilterBeanFactoryPostProcessor.DATAFILTER_HIBERNATE_CFG_FILE;
import static org.openmrs.module.datafilter.DataFilterBeanFactoryPostProcessor.SESSION_FACTORY_BEAN_NAME;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedList;

public class DataFilterBeanFactoryPostProcessorTest {
	
	private DataFilterBeanFactoryPostProcessor processor;
	
	@Mock
	private ConfigurableListableBeanFactory mockFactory;
	
	@Mock
	private BeanDefinition mockBeanDefinition;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		when(mockFactory.getBeanDefinition(SESSION_FACTORY_BEAN_NAME)).thenReturn(mockBeanDefinition);
		processor = new DataFilterBeanFactoryPostProcessor();
	}
	
	@Test
	public void postProcessBeanFactory_shouldRegisterFiltersToMappingResourceFiles() {
		ManagedList<TypedStringValue> configLocations = new ManagedList();
		configLocations.add(new TypedStringValue("classpath:" + CORE_HIBERNATE_CFG_FILE));
		MutablePropertyValues propertyValues = new MutablePropertyValues();
		propertyValues.add(CFG_LOC_PROP_NAME, configLocations);
		when(mockBeanDefinition.getPropertyValues()).thenReturn(propertyValues);
		processor.postProcessBeanFactory(mockFactory);
		
		Optional<TypedStringValue> typedStringValue = configLocations.stream()
		        .filter(loc -> (loc.getValue()).contains(DATAFILTER_HIBERNATE_CFG_FILE)).findFirst();
		
		assertNotNull(typedStringValue.get().getValue());
	}
	
}
