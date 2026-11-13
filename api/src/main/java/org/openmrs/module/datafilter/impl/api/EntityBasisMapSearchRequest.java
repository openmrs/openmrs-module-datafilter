/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.impl.api;

/**
 * A model for search
 */
public class EntityBasisMapSearchRequest {
	
	private String entityIdentifier;
	
	private String entityType;
	
	private String basisIdentifier;
	
	private String basisType;
	
	public String getEntityIdentifier() {
		return entityIdentifier;
	}
	
	public void setEntityIdentifier(String entityIdentifier) {
		this.entityIdentifier = entityIdentifier;
	}
	
	public String getEntityType() {
		return entityType;
	}
	
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	
	public String getBasisIdentifier() {
		return basisIdentifier;
	}
	
	public void setBasisIdentifier(String basisIdentifier) {
		this.basisIdentifier = basisIdentifier;
	}
	
	public String getBasisType() {
		return basisType;
	}
	
	public void setBasisType(String basisType) {
		this.basisType = basisType;
	}
}
