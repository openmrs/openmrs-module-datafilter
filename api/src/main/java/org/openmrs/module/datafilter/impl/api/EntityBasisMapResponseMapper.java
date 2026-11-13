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

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.module.datafilter.impl.EntityBasisMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EntityBasisMapResponseMapper {
	
	@Autowired
	LocationService locationService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	PatientService patientService;
	
	/**
	 * Handles only User, Patient, and Location objects TODO: Make this implementation generic to handle
	 * other OpenMRS objects
	 * 
	 * @param a
	 * @param response
	 * @return
	 */
	private DataFilterDefaultResponse mapToDefaultResponse(EntityBasisMap a, DataFilterDefaultResponse response) {
		response.setUuid(a.getUuid());
		response.setDateCreated(a.getDateCreated());
		response.setCreator(createUserMap(a.getCreator()));
		if (a.getEntityType().equals("org.openmrs.User")) {
			response.setEntity(createUserMap(userService.getUser(Integer.valueOf(a.getEntityIdentifier()))));
		} else if (a.getEntityType().equals("org.openmrs.Patient")) {
			response.setEntity(createPatientMap(patientService.getPatient(Integer.valueOf(a.getEntityIdentifier()))));
		}
		
		if (a.getBasisType().equals("org.openmrs.Location")) {
			response.setBasis(createLocationMap(locationService.getLocation(Integer.valueOf(a.getBasisIdentifier()))));
		}
		return response;
	}
	
	public List<DataFilterDefaultResponse> constructResponse(List<EntityBasisMap> entityBasisMaps) {
		return entityBasisMaps.stream().map(as -> this.mapToDefaultResponse(as, new DataFilterDefaultResponse()))
		        .collect(Collectors.toList());
	}
	
	private Map createLocationMap(Location l) {
		Map locationMap = null;
		if (l != null) {
			locationMap = new HashMap();
			locationMap.put("name", l.getName());
			locationMap.put("uuid", l.getUuid());
			locationMap.put("country", l.getCountry() != null ? l.getCountry() : "");
			locationMap.put("countyDistrict", l.getCountyDistrict() != null ? l.getCountyDistrict() : "");
			locationMap.put("cityVillage", l.getCityVillage() != null ? l.getCityVillage() : "");
			locationMap.put("country", l.getCountry() != null ? l.getCountry() : "");
			locationMap.put("address1", l.getAddress1() != null ? l.getAddress1() : "");
			locationMap.put("address2", l.getAddress2() != null ? l.getAddress2() : "");
			locationMap.put("address3", l.getAddress3() != null ? l.getAddress3() : "");
			locationMap.put("address4", l.getAddress4() != null ? l.getAddress4() : "");
			locationMap.put("address5", l.getAddress5() != null ? l.getAddress5() : "");
			locationMap.put("address6", l.getAddress6() != null ? l.getAddress6() : "");
			locationMap.putAll(l.getActiveAttributes().stream().filter(e -> e.getAttributeType() != null).collect(
			    Collectors.toMap(e -> e.getAttributeType().getName(), e -> e.getValue(), (e1, e2) -> e1 + "," + e2)));
		}
		return locationMap;
	}
	
	private Map createPatientMap(Patient p) {
		Map map = new HashMap();
		map.put("name", p.getPersonName().getFullName());
		map.put("uuid", p.getUuid());
		map.put("age", p.getAge());
		map.put("gender", p.getGender());
		map.putAll(p.getActiveIdentifiers().stream().filter(e -> e.getIdentifierType() != null)
		        .collect(Collectors.toMap(e -> e.getIdentifierType().toString().replaceAll("[- ]", ""),
		            e -> e.getIdentifier(), (e1, e2) -> e1 + "," + e2)));
		map.putAll(p.getActiveAttributes().stream().filter(e -> e.getValue() != null).collect(
		    Collectors.toMap(e -> e.getAttributeType().getName(), e -> e.getValue(), (e1, e2) -> e1 + "," + e2)));
		return map;
	}
	
	private Map createUserMap(User u) {
		Person p = u.getPerson();
		Map map = new HashMap();
		map.put("name", p.getPersonName().getFullName());
		map.put("uuid", u.getUuid());
		return map;
	}
}
