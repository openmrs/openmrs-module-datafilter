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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.openmrs.module.datafilter.DataFilterConstants.ENC_TYPE_PRIV_BASED_FILTER_NAME_PREFIX;
import static org.openmrs.module.datafilter.DataFilterConstants.LOCATION_BASED_FILTER_NAME_PREFIX;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.Type;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.datafilter.location.AccessUtil;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AccessUtil.class, Context.class, Daemon.class })
public class DataFilterInterceptorTest {
	
	private DataFilterInterceptor interceptor = new DataFilterInterceptor();
	
	@Rule
	public ExpectedException ee = ExpectedException.none();
	
	private AdministrationService adminService = null;
	
	private static Map<Class<?>, Collection<String>> locationBasedClassAndFiltersMap = new HashMap();
	
	private static Map<Class<?>, Collection<String>> encTypeViewPrivilegeBasedClassAndFiltersMap = new HashMap();
	
	public static final Set<Class<?>> LOCATION_BASED_FILTERED_CLASSES = Stream
	        .of(Patient.class, Visit.class, Encounter.class, Obs.class).collect(Collectors.toSet());
	
	public static final Set<Class<?>> ENC_TYPE_BASED_FILTERED_CLASSES = Stream.of(Encounter.class, Obs.class)
	        .collect(Collectors.toSet());
	
	@Before
	public void beforeEachMethod() {
		for (Class<?> clazz : LOCATION_BASED_FILTERED_CLASSES) {
			locationBasedClassAndFiltersMap.put(clazz, DataFilterConstants.LOCATION_BASED_FILTER_NAMES);
		}
		for (Class<?> clazz : ENC_TYPE_BASED_FILTERED_CLASSES) {
			encTypeViewPrivilegeBasedClassAndFiltersMap.put(clazz, DataFilterConstants.ENC_TYPE_VIEW_PRIV_FILTER_NAMES);
		}
		mockStatic(Context.class);
		mockStatic(AccessUtil.class);
		adminService = mock(AdministrationService.class);
		when(Context.getAdministrationService()).thenReturn(adminService);
		SessionFactory sf = mock(SessionFactory.class);
		when(sf.getCurrentSession()).thenReturn(mock(Session.class));
		when(Context.getRegisteredComponents(eq(SessionFactory.class))).thenReturn(Collections.singletonList(sf));
		when(AccessUtil.isFilterDisabled(anyString())).thenReturn(false);
		when(adminService.getGlobalPropertyValue(eq(DataFilterConstants.GP_RUN_IN_STRICT_MODE), anyBoolean()))
		        .thenReturn(true);
		when(AccessUtil.getLocationBasedClassAndFiltersMap()).thenReturn(locationBasedClassAndFiltersMap);
		when(AccessUtil.getEncounterTypeViewPrivilegeBasedClassAndFiltersMap())
		        .thenReturn(encTypeViewPrivilegeBasedClassAndFiltersMap);
	}
	
	@Test
	public void onLoad_shouldFailWithAnExceptionIfTheAuthenticatedUserIsNotAllowedToViewThePatientGettingLoaded() {
		final Integer userId = 1;
		final Integer patientId = 101;
		Collection<String> accessiblePatientIds = Stream.of("1", "4").collect(Collectors.toSet());
		when(Context.getAuthenticatedUser()).thenReturn(new User(userId));
		when(AccessUtil.getAccessiblePersonIds(eq(Location.class))).thenReturn(accessiblePatientIds);
		ee.expect(ContextAuthenticationException.class);
		ee.expectMessage(equalTo(DataFilterConstants.ILLEGAL_RECORD_ACCESS_MESSAGE));
		interceptor.onLoad(new Patient(), patientId, null, null, null);
	}
	
	@Test
	public void onLoad_shouldPassIfTheAuthenticatedUserIsAllowedToViewThePatientGettingLoaded() {
		final Integer userId = 1;
		final Integer patientId = 101;
		Collection<String> accessiblePatientIds = Stream.of(patientId.toString()).collect(Collectors.toSet());
		when(Context.getAuthenticatedUser()).thenReturn(new User(userId));
		when(AccessUtil.getAccessiblePersonIds(eq(Location.class))).thenReturn(accessiblePatientIds);
		interceptor.onLoad(new Patient(), patientId, null, null, null);
	}
	
	@Test
	public void onLoad_shouldPassForTheDaemonThread() {
		mockStatic(Daemon.class);
		when(Daemon.isDaemonThread()).thenReturn(true);
		interceptor.onLoad(null, null, null, null, null);
	}
	
	@Test
	public void onLoad_shouldPassForSuperUser() {
		User superUser = mock(User.class);
		when(superUser.isSuperUser()).thenReturn(true);
		when(Context.getAuthenticatedUser()).thenReturn(superUser);
		interceptor.onLoad(new Patient(), null, null, null, null);
	}
	
	@Test
	public void onLoad_shouldPassIfTheInterceptorIsDisabled() {
		when(adminService.getGlobalProperty(eq(DataFilterConstants.GP_RUN_IN_STRICT_MODE))).thenReturn("false");
		interceptor.onLoad(new Patient(), null, null, null, null);
	}
	
	@Test
	public void onLoad_shouldPassForAnEntityThatIsNotFiltered() {
		interceptor.onLoad(new Role(), null, null, null, null);
	}
	
	@Test
	public void onLoad_shouldPassForAllFilteredTypesIfAllLocationBasedFiltersAreDisabled() {
		User user = mock(User.class);
		when(AccessUtil.isFilterDisabled(anyString())).thenReturn(true);
		when(Context.getAuthenticatedUser()).thenReturn(user);
		interceptor.onLoad(new Patient(), null, null, null, null);
	}
	
	@Test
	public void onLoad_shouldFailWithAnExceptionIfTheAuthenticatedUserIsNotAllowedToViewThePrivilegedEncounterGettingLoaded() {
		final Integer userId = 1;
		final Integer encounterId = 101;
		User user = mock(User.class);
		user.setId(userId);
		when(Context.getAuthenticatedUser()).thenReturn(user);
		when(AccessUtil.isFilterDisabled(startsWith(LOCATION_BASED_FILTER_NAME_PREFIX))).thenReturn(true);
		final String privilege = "Some Privilege";
		final Integer encounterTypeId = 5000;
		when(AccessUtil.getViewPrivilege(Matchers.eq(encounterTypeId))).thenReturn(privilege);
		when(user.hasPrivilege(Matchers.eq(privilege))).thenReturn(false);
		ee.expect(ContextAuthenticationException.class);
		ee.expectMessage(equalTo(DataFilterConstants.ILLEGAL_RECORD_ACCESS_MESSAGE));
		EncounterType encType = new EncounterType();
		encType.setId(encounterTypeId);
		interceptor.onLoad(new Encounter(), encounterId, new Object[] { encType }, new String[] { "encounterType" },
		    new Type[] { new ManyToOneType(null, null) });
	}
	
	@Test
	public void onLoad_shouldPassIfAllEncounterTypeViewPrivilegeBasedFiltersAreDisabled() throws Exception {
		User user = mock(User.class);
		when(AccessUtil.isFilterDisabled(startsWith(LOCATION_BASED_FILTER_NAME_PREFIX))).thenReturn(true);
		when(AccessUtil.isFilterDisabled(startsWith(ENC_TYPE_PRIV_BASED_FILTER_NAME_PREFIX))).thenReturn(true);
		when(Context.getAuthenticatedUser()).thenReturn(user);
		for (Class<?> clazz : ENC_TYPE_BASED_FILTERED_CLASSES) {
			interceptor.onLoad(clazz.newInstance(), null, null, null, null);
		}
	}
	
	@Test
	public void onLoad_shouldPassIfTheEncounterTypeForTheEncounterHasNoViewPrivilege() {
		User user = mock(User.class);
		when(AccessUtil.isFilterDisabled(startsWith(LOCATION_BASED_FILTER_NAME_PREFIX))).thenReturn(true);
		when(Context.getAuthenticatedUser()).thenReturn(user);
		final Integer encounterTypeId = 1;
		when(AccessUtil.getViewPrivilege(Matchers.eq(encounterTypeId))).thenReturn(null);
		interceptor.onLoad(new Encounter(), null, new Object[] { new EncounterType(encounterTypeId) },
		    new String[] { "encounterType" }, null);
	}
	
	@Test
	public void onLoad_shouldFailIfTheAuthUserIsNotAllowedToViewThePrivilegedObsGettingLoadedAndTheEncTypeIsNotYetLoaded() {
		final Integer userId = 1;
		final Integer obsId = 101;
		final Integer encounterId = 1000;
		User user = mock(User.class);
		user.setId(userId);
		when(Context.getAuthenticatedUser()).thenReturn(user);
		when(AccessUtil.isFilterDisabled(startsWith(LOCATION_BASED_FILTER_NAME_PREFIX))).thenReturn(true);
		final String privilege = "Some Privilege";
		final Integer encounterTypeId = 5000;
		when(AccessUtil.getViewPrivilege(Matchers.eq(encounterTypeId))).thenReturn(privilege);
		when(user.hasPrivilege(Matchers.eq(privilege))).thenReturn(false);
		ee.expect(ContextAuthenticationException.class);
		ee.expectMessage(equalTo(DataFilterConstants.ILLEGAL_RECORD_ACCESS_MESSAGE));
		when(AccessUtil.getEncounterTypeId(Matchers.eq(encounterId))).thenReturn(encounterTypeId);
		interceptor.onLoad(new Obs(), obsId, new Object[] { new Encounter(encounterId) }, new String[] { "encounter" },
		    new Type[] { new ManyToOneType(null, null) });
	}
	
	@Test
	public void onLoad_shouldFailIfTheAuthUserIsNotAllowedToViewThePrivilegedObsGettingLoadedAndTheEncTypeIsLoaded() {
		final Integer userId = 1;
		final Integer obsId = 101;
		User user = mock(User.class);
		user.setId(userId);
		when(Context.getAuthenticatedUser()).thenReturn(user);
		when(AccessUtil.isFilterDisabled(startsWith(LOCATION_BASED_FILTER_NAME_PREFIX))).thenReturn(true);
		final String privilege = "Some Privilege";
		final Integer encounterTypeId = 5000;
		when(AccessUtil.getViewPrivilege(Matchers.eq(encounterTypeId))).thenReturn(privilege);
		when(user.hasPrivilege(Matchers.eq(privilege))).thenReturn(false);
		ee.expect(ContextAuthenticationException.class);
		ee.expectMessage(equalTo(DataFilterConstants.ILLEGAL_RECORD_ACCESS_MESSAGE));
		Encounter e = new Encounter();
		e.setEncounterType(new EncounterType(encounterTypeId));
		interceptor.onLoad(new Obs(), obsId, new Object[] { e }, new String[] { "encounter" },
		    new Type[] { new ManyToOneType(null, null) });
	}
	
	@Test
	public void onLoad_shouldPassIfTheTheObsBelongsToNoEncounter() {
		when(AccessUtil.isFilterDisabled(startsWith(LOCATION_BASED_FILTER_NAME_PREFIX))).thenReturn(true);
		when(AccessUtil.getViewPrivilege(Matchers.any())).thenReturn("Some Privilege");
		interceptor.onLoad(new Obs(), null, new Object[] { null }, new String[] { "encounter" }, null);
	}
	
	@Test
	public void onLoad_shouldPassIfTheAssociatedEncounterTypeForTheObsEncounterHasNoViewPrivilege() {
		User user = mock(User.class);
		when(AccessUtil.isFilterDisabled(startsWith(LOCATION_BASED_FILTER_NAME_PREFIX))).thenReturn(true);
		when(Context.getAuthenticatedUser()).thenReturn(user);
		final Integer encounterId = 101;
		interceptor.onLoad(new Obs(), null, new Object[] { new Encounter(encounterId) }, new String[] { "encounter" }, null);
	}
	
	@Test
	public void onLoad_shouldPassIfTheAuthenticatedUserIsAllowedToViewThePatientVisitGettingLoaded() {
		final Integer patientId = 101;
		Collection<String> accessiblePatientIds = Stream.of(patientId.toString()).collect(Collectors.toSet());
		when(Context.getAuthenticatedUser()).thenReturn(new User());
		when(AccessUtil.getAccessiblePersonIds(eq(Location.class))).thenReturn(accessiblePatientIds);
		interceptor.onLoad(new Visit(), null, new Object[] { new Patient(patientId) }, new String[] { "patient" }, null);
	}
	
	@Test
	public void onLoad_shouldPassIfTheAuthenticatedUserIsAllowedToViewThePatientEncounterGettingLoaded() {
		final Integer patientId = 101;
		Collection<String> accessiblePatientIds = Stream.of(patientId.toString()).collect(Collectors.toSet());
		when(AccessUtil.isFilterDisabled(startsWith(ENC_TYPE_PRIV_BASED_FILTER_NAME_PREFIX))).thenReturn(true);
		when(Context.getAuthenticatedUser()).thenReturn(new User());
		when(AccessUtil.getAccessiblePersonIds(eq(Location.class))).thenReturn(accessiblePatientIds);
		interceptor.onLoad(new Encounter(), null, new Object[] { new Patient(101) }, new String[] { "patient" }, null);
	}
	
	@Test
	public void onLoad_shouldPassIfTheAuthenticatedUserIsAllowedToViewTheObsGettingLoaded() {
		final Integer patientId = 101;
		Collection<String> accessiblePatientIds = Stream.of(patientId.toString()).collect(Collectors.toSet());
		when(AccessUtil.isFilterDisabled(startsWith(ENC_TYPE_PRIV_BASED_FILTER_NAME_PREFIX))).thenReturn(true);
		when(Context.getAuthenticatedUser()).thenReturn(new User());
		when(AccessUtil.getAccessiblePersonIds(eq(Location.class))).thenReturn(accessiblePatientIds);
		interceptor.onLoad(new Obs(), null, new Object[] { new Patient(patientId) }, new String[] { "person" }, null);
	}
	
}
