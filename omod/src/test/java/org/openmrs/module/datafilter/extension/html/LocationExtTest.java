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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.impl.EntityBasisMap;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class LocationExtTest {

	private LocationExt locationExt;

	@Mock
	private LocationService locationService;

	@Mock
	private UserService userService;

	@Mock
	private DataFilterService dataFilterService;

	@Before
	public void setUp() {
		initMocks(this);
		PowerMockito.mockStatic(Context.class);

		locationExt = new LocationExt();
		when(Context.getLocationService()).thenReturn(locationService);
		when(Context.getUserService()).thenReturn(userService);
		when(Context.getRegisteredComponents(DataFilterService.class)).thenReturn(Arrays.asList(dataFilterService));

		List<Location> locationList = Arrays.asList(getLocationMock("l1"), getLocationMock("l2"));
		when(locationService.getAllLocations()).thenReturn(locationList);
	}

	@Test
	public void shouldGenerateContentWithNoSelectedLocationsWhenUserHasNoLocationsMapped() {
		locationExt.setParameterMap(new HashMap<String, String>());
		String content = locationExt.getOverrideContent("");
		assertTrue(content.contains("<input type='checkbox' name='locationStrings' id='locationStrings.l1' value='l1'>"));
		assertTrue(content.contains("<input type='checkbox' name='locationStrings' id='locationStrings.l2' value='l2'>"));
	}

	@Test
	public void shouldGenerateContentWithCheckedUserLocations() {
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("userId", "1");
		locationExt.setParameterMap(parameterMap);
		User user = new User(1);
		when(userService.getUser(1)).thenReturn(user);

		EntityBasisMap entityBasisMap = new EntityBasisMap();
		entityBasisMap.setEntityIdentifier("1");
		entityBasisMap.setBasisIdentifier("2");

		when(dataFilterService.getEntityBasisMaps(user, Location.class.getName())).thenReturn(
				Arrays.asList(entityBasisMap));
		when(locationService.getLocation(2)).thenReturn(getLocationMock("l2"));

		String content = locationExt.getOverrideContent("");

		assertTrue(content.contains("<input type='checkbox' name='locationStrings' id='locationStrings.l1' value='l1'>"));
		assertTrue(content.contains(
				"<input type='checkbox' name='locationStrings' id='locationStrings.l2' value='l2' checked>"));;
	}

	private Location getLocationMock(String locationName) {
		Location location = new Location(Integer.parseInt(locationName.substring(1)));
		location.setName(locationName);
		return location;
	}

}
