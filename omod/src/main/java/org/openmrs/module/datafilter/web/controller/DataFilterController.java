/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.impl.EntityBasisMap;
import org.openmrs.module.datafilter.impl.api.DataFilterDefaultResponse;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.openmrs.module.datafilter.impl.api.EntityBasisMapResponseMapper;
import org.openmrs.module.datafilter.impl.api.EntityBasisMapSearchRequest;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/datafilter")
public class DataFilterController extends BaseRestController {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	private DataFilterService dataFilterService;
	
	@Autowired
	private EntityBasisMapResponseMapper dataFilterMapper;
	
	@RequestMapping(method = RequestMethod.POST, value = "entitybasismap")
	@ResponseBody
	public ResponseEntity<Object> grantAccess(@Valid @RequestBody EntityBasisMap entityBasisMapRequest) {
		try {
			OpenmrsObject entity = null;
			OpenmrsObject basis = null;
			String entityDomain = entityBasisMapRequest.getEntityType();
			String entityIdentifier = entityBasisMapRequest.getEntityIdentifier();
			String basisDomain = entityBasisMapRequest.getBasisType();
			String basisIdentifier = entityBasisMapRequest.getBasisIdentifier();
			
			if (StringUtils.isNotBlank(entityDomain) && StringUtils.isNotBlank(entityIdentifier)
			        && entityDomain.equalsIgnoreCase("org.openmrs.User")) {
				entity = Context.getUserService().getUserByUuid(entityIdentifier);
			} else if (StringUtils.isNotBlank(entityDomain) && StringUtils.isNotBlank(entityIdentifier)
			        && entityDomain.equalsIgnoreCase("org.openmrs.Patient")) {
				entity = Context.getPatientService().getPatientByUuid(entityIdentifier);
			}
			
			if (StringUtils.isNotBlank(basisDomain) && StringUtils.isNotBlank(basisIdentifier)
			        && basisDomain.equalsIgnoreCase("org.openmrs.Location")) {
				basis = Context.getLocationService().getLocationByUuid(basisIdentifier);
			}
			if (entity == null) {
				return new ResponseEntity<>("Data filter Entity is required!", HttpStatus.BAD_REQUEST);
			}
			
			if (basis == null) {
				return new ResponseEntity<>("Data filter Basis is required!", HttpStatus.BAD_REQUEST);
			}
			dataFilterService.grantAccess(entity, basis);
			return new ResponseEntity<>("Data filter access granted successfully", HttpStatus.CREATED);
			
		}
		catch (Exception e) {
			log.error("Runtime error while trying to grant access", e);
			return new ResponseEntity<>("Runtime error while trying to grant data filter access", HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "search")
	@ResponseBody
	public List<DataFilterDefaultResponse> searchEntityBasisMap(@Valid @RequestBody EntityBasisMapSearchRequest searchQuery)
	        throws IOException {
		Collection<EntityBasisMap> entityBasisMaps = null;
		
		if (StringUtils.isNotBlank(searchQuery.getBasisType()) || StringUtils.isNotBlank(searchQuery.getEntityType())) {
			
			OpenmrsObject entity = null;
			OpenmrsObject basis = null;
			String entityDomain = searchQuery.getEntityType();
			String entityIdentifier = searchQuery.getEntityIdentifier();
			String basisDomain = searchQuery.getBasisType();
			String basisIdentifier = searchQuery.getBasisIdentifier();
			
			if (StringUtils.isNotBlank(entityDomain) && StringUtils.isNotBlank(entityIdentifier)
			        && entityDomain.equalsIgnoreCase("org.openmrs.User")) {
				entity = Context.getUserService().getUserByUuid(entityIdentifier);
			} else if (StringUtils.isNotBlank(entityDomain) && StringUtils.isNotBlank(entityIdentifier)
			        && entityDomain.equalsIgnoreCase("org.openmrs.Patient")) {
				entity = Context.getPatientService().getPatientByUuid(entityIdentifier);
			}
			
			if (StringUtils.isNotBlank(basisDomain) && StringUtils.isNotBlank(basisIdentifier)
			        && basisDomain.equalsIgnoreCase("org.openmrs.Location")) {
				basis = Context.getLocationService().getLocationByUuid(basisIdentifier);
			}
			
			if (entity != null && basis == null) {
				entityBasisMaps = dataFilterService.getEntityBasisMaps(entity, basisDomain);
			} else if (basis != null && entity == null) {
				entityBasisMaps = dataFilterService.getEntityBasisMapsByBasis(entityDomain, basis);
			}
		}
		return dataFilterMapper.constructResponse(entityBasisMaps.stream().collect(Collectors.toList()));
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "revoke")
	@ResponseBody
	public ResponseEntity<Object> revokeAccess(@Valid @RequestBody EntityBasisMap entityBasisMapRequest) {
		try {
			OpenmrsObject entity = null;
			OpenmrsObject basis = null;
			String entityDomain = entityBasisMapRequest.getEntityType();
			String entityIdentifier = entityBasisMapRequest.getEntityIdentifier();
			String basisDomain = entityBasisMapRequest.getBasisType();
			String basisIdentifier = entityBasisMapRequest.getBasisIdentifier();
			
			if (StringUtils.isNotBlank(entityDomain) && StringUtils.isNotBlank(entityIdentifier)
			        && entityDomain.equalsIgnoreCase("org.openmrs.User")) {
				entity = Context.getUserService().getUserByUuid(entityIdentifier);
			} else if (StringUtils.isNotBlank(entityDomain) && StringUtils.isNotBlank(entityIdentifier)
			        && entityDomain.equalsIgnoreCase("org.openmrs.Patient")) {
				entity = Context.getPatientService().getPatientByUuid(entityIdentifier);
			}
			
			if (StringUtils.isNotBlank(basisDomain) && StringUtils.isNotBlank(basisIdentifier)
			        && basisDomain.equalsIgnoreCase("org.openmrs.Location")) {
				basis = Context.getLocationService().getLocationByUuid(basisIdentifier);
			}
			if (entity == null) {
				return new ResponseEntity<>("Data filter Entity is required!", HttpStatus.BAD_REQUEST);
			}
			
			if (basis == null) {
				return new ResponseEntity<>("Data filter Basis is required!", HttpStatus.BAD_REQUEST);
			}
			dataFilterService.revokeAccess(entity, basis);
			return new ResponseEntity<>("Access has been revoked successfully", HttpStatus.OK);
			
		}
		catch (Exception e) {
			log.error("Runtime error while trying to revoke data filter access", e);
			return new ResponseEntity<>("Runtime error while trying to revoke data filter access", HttpStatus.BAD_REQUEST);
		}
	}
}
