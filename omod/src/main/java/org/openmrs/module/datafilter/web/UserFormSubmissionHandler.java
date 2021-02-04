/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.impl.EntityBasisMap;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component("userFormSubmissionHandler")
public class UserFormSubmissionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(UserFormSubmissionHandler.class);
	
	@Transactional
	public void handle(ServletRequest request, ServletResponse response, FilterChain chain)
	        throws IOException, ServletException {
		
		//Process form data from legacy UI
		chain.doFilter(request, response);
		
		if (((HttpServletRequest) request).getMethod().equals("POST")
		        && ((HttpServletResponse) response).getStatus() < 400) {
			
			DataFilterService dataFilterService = Context.getRegisteredComponents(DataFilterService.class).get(0);
			LocationService locationService = Context.getLocationService();
			UserService userService = Context.getUserService();
			
			User user = null;
			//TODO remove this if condition when the legacy ui passes userId
			String userName = request.getParameter("username");
			if (!StringUtils.isEmpty(userName)) {
				user = userService.getUserByUsername(userName);
			}
			
			if (user == null) {
				String userUUID = ((HttpServletResponse) response).getHeader("userUUID");
				user = userService.getUserByUuid(userUUID);
			}
			
			if (user == null) {
				log.info("User details missing or Invalid user details");
				return;
			}
			
			String[] locationStrings = request.getParameterValues("locationStrings") != null
			        ? request.getParameterValues("locationStrings")
			        : new String[0];
			Collection<EntityBasisMap> mappedLocationsMap = dataFilterService.getEntityBasisMaps(user,
			    Location.class.getName());
			List<OpenmrsObject> locationsToBeRevoked = getLocationsToBeRevoked(locationService.getAllLocations(),
			    mappedLocationsMap, locationStrings);
			if (!locationsToBeRevoked.isEmpty()) {
				dataFilterService.revokeAccess(user, locationsToBeRevoked);
			}
			if (locationStrings.length < 1) {
				log.info("No locations selected");
				return;
			}
			Set<OpenmrsObject> allSelectedLocations = Arrays.stream(locationStrings).map(l -> locationService.getLocation(l))
			        .filter(Objects::nonNull).collect(Collectors.toSet());
			if (!allSelectedLocations.isEmpty()) {
				dataFilterService.grantAccess(user, allSelectedLocations);
			}
		}
		
	}
	
	private List<OpenmrsObject> getLocationsToBeRevoked(List<Location> allLocationsForLoggedInUser,
	        Collection<EntityBasisMap> entityBasisMapsForUserBeingEdited, String[] locationStrings) {
		
		Set<String> mappedLocationIDsForUser = entityBasisMapsForUserBeingEdited.stream()
		        .map(entityBasisMap -> entityBasisMap.getBasisIdentifier()).collect(Collectors.toSet());
		
		List<String> markedLocations = Arrays.asList(locationStrings);
		
		List<Location> locationsLoggedInUserHasControl = allLocationsForLoggedInUser.stream()
		        .filter((location) -> mappedLocationIDsForUser.contains(location.getLocationId().toString()))
		        .collect(Collectors.toList());
		
		if (!locationsLoggedInUserHasControl.isEmpty()) {
			return locationsLoggedInUserHasControl.stream().filter(location -> !markedLocations.contains(location.getName()))
			        .collect(Collectors.toList());
		}
		return new ArrayList<>();
	}
}
