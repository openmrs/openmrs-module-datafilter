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

import java.util.Date;
import java.util.Map;

public class DataFilterDefaultResponse {
	
	private String uuid;
	
	private Date dateCreated;
	
	private Map entity;
	
	private Map basis;
	
	private Map creator;
	
	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public Map getEntity() {
		return entity;
	}
	
	public void setEntity(Map entity) {
		this.entity = entity;
	}
	
	public Map getBasis() {
		return basis;
	}
	
	public void setBasis(Map basis) {
		this.basis = basis;
	}
	
	public Map getCreator() {
		return creator;
	}
	
	public void setCreator(Map creator) {
		this.creator = creator;
	}
}
