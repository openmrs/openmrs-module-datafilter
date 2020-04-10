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

import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.datafilter.impl.EntityBasisMap;
import org.openmrs.module.datafilter.impl.api.DataFilterService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class LocationExt extends Extension {

    String label = "";

   public String getLabel() {
        label = "Location";
        return label;
    }


    /**
     * Returns the required privilege in order to see this section. Can be a
     * comma delimited list of privileges. If the default empty string is
     * returned, only an authenticated user is required
     *
     * @return Privilege string
     */

    @Override
    public String getOverrideContent(String bodyContent) {
        List<Location> locations = Context.getLocationService().getAllLocations();

        List<String> locationNames = locations.stream().map(location -> location.getName()).collect(Collectors.toList());
        String userId = getParameterMap().get("userId");
        List<String> selectedLocations = getMappedLocations(userId);
        Content content = new Content(locationNames, selectedLocations);
        return content.generate();
    }

    private List<String> getMappedLocations(String userId) {
        DataFilterService dataFilterService = Context.getRegisteredComponents(DataFilterService.class).get(0);
        LocationService locationService = Context.getLocationService();
        List<String> selectedLocations = new ArrayList<>();
        if (!userId.equals("")) {
            User user = Context.getUserService().getUser(Integer.parseInt(userId));
            Collection<EntityBasisMap> mappedLocationsMap = dataFilterService.get(user, Location.class.getName());
            if (mappedLocationsMap != null) {
                selectedLocations = mappedLocationsMap.stream().map(mappedLocation -> locationService.getLocation(Integer.parseInt(mappedLocation.getBasisIdentifier())).getName()).collect(Collectors.toList());
            }
        }
        return selectedLocations;
    }

    @Override
    public MEDIA_TYPE getMediaType() {
        return MEDIA_TYPE.html;
    }
}
