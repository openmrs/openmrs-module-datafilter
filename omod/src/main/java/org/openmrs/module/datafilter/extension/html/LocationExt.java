/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.datafilter.extension.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import ca.uhn.hl7v2.util.StringUtil;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.datafilter.impl.EntityBasisMap;
import org.openmrs.module.datafilter.impl.api.DataFilterService;

public class LocationExt extends Extension {
	
	/**
	 * Returns the required privilege in order to see this section. Can be a comma delimited list of
	 * privileges. If the default empty string is returned, only an authenticated user is required
	 *
	 * @return Privilege string
	 */
	
	@Override
	public String getOverrideContent(String bodyContent) {
		List<Location> locations = Context.getLocationService().getAllLocations();
		
		List<String> locationNames = getLocationNames(locations);
		String userId = getParameterMap().get("userId");
		List<String> selectedLocations = new ArrayList<>();
		
		if (StringUtil.isNotBlank(userId)) {
			selectedLocations = getExistingLocations(userId);
		}
		Content content = new Content(locationNames, selectedLocations);
		return content.generate();
	}
	
	private List<String> getLocationNames(List<Location> locations) {
		return locations.stream().map(location -> location.getName()).collect(Collectors.toList());
	}
	
	private List<String> getExistingLocations(String userId) {
		DataFilterService dataFilterService = Context.getRegisteredComponents(DataFilterService.class).get(0);
		UserService userService = Context.getUserService();
		
		User user = userService.getUser(Integer.parseInt(userId));
		Collection<EntityBasisMap> userLocations = dataFilterService.getEntityBasisMaps(user, Location.class.getName());
		
		return getLocationNamesFromEntityBasisMap(userLocations);
	}
	
	private List<String> getLocationNamesFromEntityBasisMap(Collection<EntityBasisMap> userLocations) {
		return userLocations.stream().map(userLocation -> getLocationName(userLocation.getBasisIdentifier()))
		        .collect(Collectors.toList());
	}
	
	private String getLocationName(String basisIdentifier) {
		LocationService locationService = Context.getLocationService();
		
		return locationService.getLocation(Integer.parseInt(basisIdentifier)).getName();
	}
	
	@Override
	public MEDIA_TYPE getMediaType() {
		return MEDIA_TYPE.html;
	}
}
