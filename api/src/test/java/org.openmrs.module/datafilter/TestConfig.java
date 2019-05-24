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

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class TestConfig {
	
	/**
	 * @see SpringLiquibase
	 */
	@Bean
	public SpringLiquibase getSpringLiquibase(SessionFactory sf) {
		SpringLiquibase liquibase = new SpringLiquibase();
		DataSource dataSource = ((SessionFactoryImpl) sf).getConnectionProvider().unwrap(DataSource.class);
		liquibase.setDataSource(dataSource);
		liquibase.setChangeLog("liquibase.xml");
		return liquibase;
	}
	
}
