/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.api.db.hibernate;

import org.hibernate.SessionFactory;
import org.openmrs.module.datafilter.EntityBasisMap;
import org.openmrs.module.datafilter.api.db.DataFilterDAO;

public class HibernateDataFilterDAO implements DataFilterDAO {
	
	private SessionFactory sessionFactory;
	
	/**
	 * Sets the sessionFactory
	 *
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see DataFilterDAO#saveEntityBasisMapById(EntityBasisMap)
	 */
	@Override
	public EntityBasisMap saveEntityBasisMapById(EntityBasisMap entityBasisMap) {
		return null;
	}
	
	/**
	 * @see DataFilterDAO#deleteEntityBasisMap(EntityBasisMap)
	 */
	@Override
	public void deleteEntityBasisMap(EntityBasisMap entityBasisMap) {
		
	}
	
}
