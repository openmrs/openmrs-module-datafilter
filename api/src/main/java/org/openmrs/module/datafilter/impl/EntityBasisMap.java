/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.impl;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Creatable;
import org.openmrs.User;
import org.openmrs.module.datafilter.DataFilterConstants;

/**
 * An instance of this class encapsulates information about a mapping between an entity and a basis
 * they have been granted access to e.g. an entity could be a user or a role and a basis could be a
 * location or a program that is linked to records they should have access to.
 */
@Entity
@Table(name = DataFilterConstants.MODULE_ID
        + "_entity_basis_map", uniqueConstraints = @UniqueConstraint(name = DataFilterConstants.MODULE_ID
                + "_entity_basis_map", columnNames = { "entity_identifier", "entity_type", "basis_identifier",
                        "basis_type" }))
public class EntityBasisMap extends BaseOpenmrsObject implements Creatable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "entity_basis_map_id")
	private Integer entityBasisMapId;
	
	@Column(name = "entity_identifier", nullable = false, updatable = false, length = 127)
	private String entityIdentifier;
	
	@Column(name = "entity_type", nullable = false, updatable = false)
	private String entityType;
	
	@Column(name = "basis_identifier", nullable = false, updatable = false, length = 127)
	private String basisIdentifier;
	
	@Column(name = "basis_type", nullable = false, updatable = false)
	private String basisType;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "creator", nullable = false, updatable = false)
	private User creator;
	
	@Column(name = "date_created", nullable = false, updatable = false)
	private Date dateCreated;
	
	/**
	 * @see BaseOpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getEntityBasisMapId();
	}
	
	/**
	 * @see BaseOpenmrsObject#setId(Integer)
	 */
	@Override
	public void setId(Integer id) {
		setEntityBasisMapId(id);
	}
	
	/**
	 * Gets the entityBasisMapId
	 *
	 * @return the entityBasisMapId
	 */
	public Integer getEntityBasisMapId() {
		return entityBasisMapId;
	}
	
	/**
	 * Sets the entityBasisMapId
	 *
	 * @param entityBasisMapId the entityBasisMapId to set
	 */
	public void setEntityBasisMapId(Integer entityBasisMapId) {
		this.entityBasisMapId = entityBasisMapId;
	}
	
	/**
	 * Gets the entityIdentifier
	 *
	 * @return the entityIdentifier
	 */
	public String getEntityIdentifier() {
		return entityIdentifier;
	}
	
	/**
	 * Sets the entityIdentifier
	 *
	 * @param entityIdentifier the entityIdentifier to set
	 */
	public void setEntityIdentifier(String entityIdentifier) {
		this.entityIdentifier = entityIdentifier;
	}
	
	/**
	 * Gets the entityType
	 *
	 * @return the entityType
	 */
	public String getEntityType() {
		return entityType;
	}
	
	/**
	 * Sets the entityType
	 *
	 * @param entityType the entityType to set
	 */
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	
	/**
	 * Gets the basisId
	 *
	 * @return the basisId
	 */
	public String getBasisIdentifier() {
		return basisIdentifier;
	}
	
	/**
	 * Sets the basisId
	 *
	 * @param basisIdentifier the basisId to set
	 */
	public void setBasisIdentifier(String basisIdentifier) {
		this.basisIdentifier = basisIdentifier;
	}
	
	/**
	 * Gets the basisType
	 *
	 * @return the basisType
	 */
	public String getBasisType() {
		return basisType;
	}
	
	/**
	 * Sets the basisType
	 *
	 * @param basisType the basisType to set
	 */
	public void setBasisType(String basisType) {
		this.basisType = basisType;
	}
	
	/**
	 * @see Creatable#getCreator()
	 */
	@Override
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @see Creatable#setCreator(User)
	 */
	@Override
	public void setCreator(User user) {
		creator = user;
	}
	
	/**
	 * @see Creatable#getDateCreated()
	 */
	@Override
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @see Creatable#setDateCreated(Date)
	 */
	@Override
	public void setDateCreated(Date date) {
		dateCreated = date;
	}
	
}
