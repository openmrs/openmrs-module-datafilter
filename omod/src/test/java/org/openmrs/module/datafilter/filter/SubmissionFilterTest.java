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

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.impl.EntityBasisMap;
import org.openmrs.module.datafilter.impl.api.DataFilterService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class SubmissionFilterTest {

    @Mock
    private FilterChain filterChain;

    @Mock
    private LocationService locationService;

    @Mock
    private DataFilterService dataFilterService;

    @Mock
    private UserService userService;

    private SubmissionFilter submissionFilter;

    private RequestDispatcher mockRequestDispatcher;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        PowerMockito.mockStatic(Context.class);
        when(Context.getRegisteredComponents(DataFilterService.class))
                .thenReturn(Arrays.asList(dataFilterService));
        when(Context.getLocationService()).thenReturn(locationService);
        when(Context.getUserService()).thenReturn(userService);

        submissionFilter = new SubmissionFilter();
        mockRequestDispatcher = Mockito.mock(RequestDispatcher.class);
    }


    @Test
    public void shouldNotGrandAccessToUserIfNotPostRequest() throws Exception {
        HttpServletRequest request = buildMockHttpServletRequest("GET","user_x",null);

        MockHttpServletResponse response = buildMockHttpServletResponse();

        submissionFilter.doFilter(request, response, filterChain);

        verify(userService, never()).getUserByUsername(any(String.class));
        verify(locationService, never()).getLocation(any(String.class));
        verify(dataFilterService, never()).grantAccess(any(User.class), anyCollectionOf(OpenmrsObject.class));
    }

    @Test
    public void shouldNotGrandAccessToUserIfResponseStatusIs400() throws Exception {
        HttpServletRequest request = buildMockHttpServletRequest("POST","user_x",new String[]{"l1","l2"});

        MockHttpServletResponse response = buildMockHttpServletResponse();
        response.setStatus(400);

        submissionFilter.doFilter(request, response, filterChain);

        verify(userService, never()).getUserByUsername(any(String.class));
        verify(locationService, never()).getLocation(any(String.class));
        verify(dataFilterService, never()).grantAccess(any(User.class), anyCollectionOf(OpenmrsObject.class));
    }

    @Test
    public void shouldNotGrandAccessToUserIfUserDetailsMissing() throws Exception {
        HttpServletRequest request = buildMockHttpServletRequest("POST","",new String[]{"l1","l2"});

        MockHttpServletResponse response = buildMockHttpServletResponse();

        submissionFilter.doFilter(request, response, filterChain);

        verify(userService, never()).getUserByUsername(any(String.class));
        verify(locationService, never()).getLocation(any(String.class));
        verify(dataFilterService, never()).grantAccess(any(User.class), anyCollectionOf(OpenmrsObject.class));
    }

    @Test
    public void shouldNotGrandAccessIfUserIsNotValid() throws Exception {
        HttpServletRequest request = buildMockHttpServletRequest("POST","invalid_user",new String[]{"l1","l2"});

        MockHttpServletResponse response = buildMockHttpServletResponse();

        when(userService.getUserByUsername(any(String.class))).thenReturn(null);
        submissionFilter.doFilter(request, response, filterChain);

        verify(userService).getUserByUsername(any(String.class));
        verify(locationService, never()).getLocation(any(String.class));
        verify(dataFilterService, never()).grantAccess(any(User.class), anyCollectionOf(OpenmrsObject.class));
    }

    @Test
    public void shouldNotGrandAccessToUserIfLocationMissing() throws Exception {
        HttpServletRequest request = buildMockHttpServletRequest("POST","invalid_user",new String[]{});
        ((MockHttpServletRequest) request).setMethod("POST");
        ((MockHttpServletRequest) request).addParameter("username", "user_x");

        MockHttpServletResponse response = buildMockHttpServletResponse();

        when(userService.getUserByUsername(any(String.class))).thenReturn(new User());
        submissionFilter.doFilter(request, response, filterChain);

        verify(userService).getUserByUsername(any(String.class));
        verify(locationService, never()).getLocation(any(String.class));
        verify(dataFilterService, never()).grantAccess(any(User.class), anyCollectionOf(OpenmrsObject.class));
    }

    @Test
    public void shouldNotGrandAccessIfLocationIsNotValid() throws Exception {
        HttpServletRequest request = buildMockHttpServletRequest("POST","user_x",new String[]{"invalid_1","invalid_2"});

        MockHttpServletResponse response = buildMockHttpServletResponse();

        User user = new User();
        when(locationService.getLocation(any(String.class))).thenReturn(null);
        when(userService.getUserByUsername(any(String.class))).thenReturn(user);

        submissionFilter.doFilter(request, response, filterChain);

        verify(dataFilterService, never()).grantAccess(any(User.class), anyCollectionOf(OpenmrsObject.class));
    }

    @Test
    public void shouldGrandAccessToUser() throws Exception {

        HttpServletRequest request = buildMockHttpServletRequest("POST","user_x",new String[]{"l1","l2"});

        MockHttpServletResponse response = buildMockHttpServletResponse();

        User user = new User();
        when(locationService.getAllLocations()).thenReturn(getLocations(Arrays.asList("l1","l2","l3")));
        when(locationService.getLocation("l1")).thenReturn(getLocation( "l1"));
        when(locationService.getLocation("l2")).thenReturn(getLocation( "l2"));
        when(dataFilterService.get(any(User.class), any(String.class))).thenReturn(new ArrayList<EntityBasisMap>());
        when(userService.getUserByUsername(any(String.class))).thenReturn(user);

        submissionFilter.doFilter(request, response, filterChain);

        verify(locationService, times(2)).getLocation(any(String.class));
        verify(dataFilterService).grantAccess(any(User.class), anyCollectionOf(OpenmrsObject.class));
        verify(dataFilterService, never()).revokeAccess(any(User.class), anyCollectionOf(OpenmrsObject.class));
    }

    @Test
    public void shouldGrantAndRevokeAccessToUser() throws IOException, ServletException {

        HttpServletRequest request = buildMockHttpServletRequest("POST","user_x",new String[]{"l1","l2"});

        MockHttpServletResponse response = buildMockHttpServletResponse();

        User user = Mockito.mock(User.class);
        when(locationService.getAllLocations()).thenReturn(getLocations(Arrays.asList("l1","l2","l3","l5","l7")));
        when(locationService.getLocation("l1")).thenReturn(getLocation( "l1"));
        when(locationService.getLocation("l2")).thenReturn(getLocation( "l2"));

        when(userService.getUserByUsername(any(String.class))).thenReturn(user);
        when(dataFilterService.get(any(User.class), any(String.class))).thenReturn(getEntityBasisMaps(Arrays.asList("1","3","4","6","7")));

        Mockito.doNothing().when(filterChain).doFilter(request, response);

        submissionFilter.doFilter(request, response, filterChain);

        verify(dataFilterService).revokeAccess(any(User.class), (Collection<OpenmrsObject>) argThat(IsCollectionWithSize.hasSize(2)));
        verify(dataFilterService).revokeAccess(any(User.class), (Collection<OpenmrsObject>) argThat(IsCollectionWithSize.hasSize(2)));
    }

    @Test
    public void shouldNotGrandAccessIfUserUUIDIsNotValid() throws Exception {
        HttpServletRequest request = buildMockHttpServletRequest("POST","",new String[]{"l1","l2"});

        MockHttpServletResponse response = buildMockHttpServletResponse();
        String invalid_uuid = "invalid-uuid";
        response.addHeader("userUUID", invalid_uuid);

        when(userService.getUserByUuid(invalid_uuid)).thenReturn(null);
        submissionFilter.doFilter(request, response, filterChain);

        verify(userService, never()).getUserByUsername(any(String.class));
        verify(userService, times(1)).getUserByUuid(invalid_uuid);
        verify(locationService, never()).getLocation(any(String.class));
        verify(dataFilterService, never()).grantAccess(any(User.class), anyCollectionOf(OpenmrsObject.class));
    }

    @Test
    public void shouldGrandAccessForUserByUUID() throws Exception {
        HttpServletRequest request = buildMockHttpServletRequest("POST","",new String[]{"l1","l2"});

        MockHttpServletResponse response = buildMockHttpServletResponse();
        String uuid = "uuid";
        response.addHeader("userUUID", uuid);

        User user = new User();
        when(locationService.getAllLocations()).thenReturn(getLocations(Arrays.asList("l1","l2","l3")));
        when(locationService.getLocation("l1")).thenReturn(getLocation( "l1"));
        when(locationService.getLocation("l2")).thenReturn(getLocation( "l2"));
        when(dataFilterService.get(any(User.class), any(String.class))).thenReturn(new ArrayList<EntityBasisMap>());
        when(userService.getUserByUuid(uuid)).thenReturn(user);

        submissionFilter.doFilter(request, response, filterChain);

        verify(userService,times(1)).getUserByUuid(uuid);
        verify(locationService, times(2)).getLocation(any(String.class));
        verify(dataFilterService).grantAccess(any(User.class), anyCollectionOf(OpenmrsObject.class));
        verify(dataFilterService, never()).revokeAccess(any(User.class), anyCollectionOf(OpenmrsObject.class));
    }

    private HttpServletRequest buildMockHttpServletRequest(String method,String username,String[] locationStrings) {
        HttpServletRequest request = new MockHttpServletRequest();
        ((MockHttpServletRequest) request).setMethod(method);
        ((MockHttpServletRequest) request).addParameter("locationStrings", locationStrings);
        ((MockHttpServletRequest) request).addParameter("username", username);
        return request;
    }

    private MockHttpServletResponse buildMockHttpServletResponse() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        return response;
    }

    private Collection<EntityBasisMap> getEntityBasisMaps(List<String> basisIdentifiers) {
        List<EntityBasisMap> entityBasisMaps = new ArrayList<>();
        basisIdentifiers.stream().forEach((basisIdentifier)->entityBasisMaps.add(getEntityBasisMap(basisIdentifier)));
        return entityBasisMaps;
    }

    private EntityBasisMap getEntityBasisMap(String identifier) {
        EntityBasisMap entityBasisMap = new EntityBasisMap();
        entityBasisMap.setBasisIdentifier(identifier);
        return entityBasisMap;
    }

    private List<Location> getLocations(List<String> locationNames) {
        List<Location> allLocationsForLoggedInUser = new ArrayList<>();
        locationNames.stream().forEach((locationName)->allLocationsForLoggedInUser.add(
                getLocation(locationName)));
        return allLocationsForLoggedInUser;
    }

    private Location getLocation(String locationName) {
        Location location = new Location(Integer.parseInt(locationName.substring(1)));
        location.setName(locationName);
        return location;
    }
}
