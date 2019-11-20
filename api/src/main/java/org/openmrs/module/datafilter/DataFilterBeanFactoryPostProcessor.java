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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class DataFilterBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
	
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		for (String beanName : beanFactory.getBeanDefinitionNames()) {
			BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
			beanDefinition.getBeanClassName();
			if ("sessionFactory".equals(beanName)) {
				//System.out.println(beanDefinition.getPropertyValues().get("configLocations"));
				//beanDefinition.getPropertyValues().addPropertyValue("configLocations",
				//    "classpath:datafilterHibernate.cfg.xml");
				//System.out.println(beanDefinition.getPropertyValues().get("configLocations"));
                beanDefinition.setBeanClassName("org.openmrs.module.datafilter.DFSessionFactoryBean");
			}
		}
	}
	
}
