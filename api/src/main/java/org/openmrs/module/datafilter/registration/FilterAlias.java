/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.registration;

public class FilterAlias {
	
	private String alias;
	
	private String table;
	
	private Class entityClass;
	
	/**
	 * Gets the alias
	 *
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}
	
	/**
	 * Sets the alias
	 *
	 * @param alias the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	/**
	 * Gets the table
	 *
	 * @return the table
	 */
	public String getTable() {
		return table;
	}
	
	/**
	 * Sets the table
	 *
	 * @param table the table to set
	 */
	public void setTable(String table) {
		this.table = table;
	}
	
	/**
	 * Gets the entityClass
	 *
	 * @return the entityClass
	 */
	public Class getEntityClass() {
		return entityClass;
	}
	
	/**
	 * Sets the entityClass
	 *
	 * @param entityClass the entityClass to set
	 */
	public void setEntityClass(Class entityClass) {
		this.entityClass = entityClass;
	}
	
}
