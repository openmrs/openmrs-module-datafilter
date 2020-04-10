/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.filter;

import org.apache.commons.lang.StringUtils;
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

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SubmissionFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(SubmissionFilter.class);

	private DataFilterService dataFilterService;

	private LocationService locationService;

	private UserService userService;

	public SubmissionFilter() {
		this.dataFilterService = Context.getRegisteredComponents(DataFilterService.class).get(0);
		this.locationService = Context.getLocationService();
		this.userService = Context.getUserService();
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) {
		if (log.isDebugEnabled()) {
			log.debug("Initializing datafilter web filter....");
		}
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		chain.doFilter(request, response);

		if (((HttpServletRequest) request).getMethod().equals("POST") &&
				((HttpServletResponse) response).getStatus() != 400) {

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
			String[] locationStrings = request.getParameterValues("locationStrings");
			if (locationStrings == null) {
				log.info("No locations selected");
				return;
			}
			Set<OpenmrsObject> allSelectedLocations = Arrays.stream(locationStrings)
					.map(l -> locationService.getLocation(l))
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());

			Collection<EntityBasisMap> mappedLocationsMap = dataFilterService.get(user, Location.class.getName());
			List<OpenmrsObject> locationsToBeRevoked = getLocationsToBeRevoked(locationService.getAllLocations(),
					mappedLocationsMap, locationStrings);
			if (!locationsToBeRevoked.isEmpty()) {
				dataFilterService.revokeAccess(user, locationsToBeRevoked);
			}
			if (!allSelectedLocations.isEmpty()) {
				dataFilterService.grantAccess(user, allSelectedLocations);
			}

		}
	}

	/**
	 * @see Filter#destroy()
	 */
	@Override
	public void destroy() {
		if (log.isDebugEnabled()) {
			log.debug("Destroying datafilter web filter....");
		}
	}

	private List<OpenmrsObject> getLocationsToBeRevoked(List<Location> allLocationsForLoggedInUser,
			Collection<EntityBasisMap> entityBasisMapsForUserBeingEdited, String[] locationStrings) {

		Set<String> mappedLocationIDsForUser = entityBasisMapsForUserBeingEdited.stream().map(
				entityBasisMap -> entityBasisMap.getBasisIdentifier()).collect(Collectors.toSet());

		List<String> markedLocations = Arrays.asList(locationStrings);

		List<Location> locationsLoggedInUserHasControl = allLocationsForLoggedInUser.stream().filter(
				(location) -> mappedLocationIDsForUser.contains(location.getLocationId().toString()))
				.collect(Collectors.toList());

		if (!locationsLoggedInUserHasControl.isEmpty()) {
			return locationsLoggedInUserHasControl.stream().filter(location -> !markedLocations.contains(
					location.getName())).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}
}
