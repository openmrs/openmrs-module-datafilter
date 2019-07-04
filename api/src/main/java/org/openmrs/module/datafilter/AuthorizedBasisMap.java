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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Creatable;
import org.openmrs.User;

/**
 * An instance of this class encapsulates information about a mapping between an authorized entity
 * and a basis they have been granted access to e.g. an authorized entity could be a user or a role
 * and a basis could be a location or a program that is linked to a records they have access to.
 */
@Entity
@Table(name = DataFilterConstants.MODULE_ID + "_authorized_entity_basis_map")
public class AuthorizedBasisMap extends BaseOpenmrsObject implements Creatable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "authorized_entity_basis_map_id")
	private Integer authorizedBasisMapId;
	
	@Column(name = "authorized_entity_identifier", nullable = false, updatable = false)
	private String authorizedEntityIdentifier;
	
	@Column(name = "authorized_entity_type", nullable = false, updatable = false)
	private String authorizedEntityType;
	
	@Column(name = "basis_id", nullable = false, updatable = false)
	private Integer basisId;
	
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
		return getAuthorizedBasisMapId();
	}
	
	/**
	 * @see BaseOpenmrsObject#setId(Integer)
	 */
	@Override
	public void setId(Integer id) {
		setAuthorizedBasisMapId(id);
	}
	
	/**
	 * Gets the authorizedBasisMapId
	 *
	 * @return the authorizedBasisMapId
	 */
	public Integer getAuthorizedBasisMapId() {
		return authorizedBasisMapId;
	}
	
	/**
	 * Sets the authorizedBasisMapId
	 *
	 * @param authorizedBasisMapId the authorizedBasisMapId to set
	 */
	public void setAuthorizedBasisMapId(Integer authorizedBasisMapId) {
		this.authorizedBasisMapId = authorizedBasisMapId;
	}
	
	/**
	 * Gets the authorizedEntityIdentifier
	 *
	 * @return the authorizedEntityIdentifier
	 */
	public String getAuthorizedEntityIdentifier() {
		return authorizedEntityIdentifier;
	}
	
	/**
	 * Sets the authorizedEntityIdentifier
	 *
	 * @param authorizedEntityIdentifier the authorizedEntityIdentifier to set
	 */
	public void setAuthorizedEntityIdentifier(String authorizedEntityIdentifier) {
		this.authorizedEntityIdentifier = authorizedEntityIdentifier;
	}
	
	/**
	 * Gets the authorizedEntityType
	 *
	 * @return the authorizedEntityType
	 */
	public String getAuthorizedEntityType() {
		return authorizedEntityType;
	}
	
	/**
	 * Sets the authorizedEntityType
	 *
	 * @param authorizedEntityType the authorizedEntityType to set
	 */
	public void setAuthorizedEntityType(String authorizedEntityType) {
		this.authorizedEntityType = authorizedEntityType;
	}
	
	/**
	 * Gets the basisId
	 *
	 * @return the basisId
	 */
	public Integer getBasisId() {
		return basisId;
	}
	
	/**
	 * Sets the basisId
	 *
	 * @param basisId the basisId to set
	 */
	public void setBasisId(Integer basisId) {
		this.basisId = basisId;
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
