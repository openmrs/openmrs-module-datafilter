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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.Daemon;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AccessUtil.class, Context.class, Daemon.class })
public class DataFilterInterceptorTest {
	
	private DataFilterInterceptor interceptor = new DataFilterInterceptor();
	
	@Rule
	public ExpectedException ee = ExpectedException.none();
	
	private AdministrationService adminService = null;
	
	@Before
	public void beforeEachMethod() {
		mockStatic(Context.class);
		mockStatic(AccessUtil.class);
		adminService = mock(AdministrationService.class);
		when(Context.getAdministrationService()).thenReturn(adminService);
		when(adminService.getGlobalPropertyValue(eq(DataFilterConstants.GP_RUN_IN_STRICT_MODE), anyBoolean()))
		        .thenReturn(true);
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
		when(adminService.getGlobalPropertyValue(eq(DataFilterConstants.GP_RUN_IN_STRICT_MODE), anyBoolean()))
		        .thenReturn(false);
		interceptor.onLoad(new Patient(), null, null, null, null);
	}
	
	@Test
	public void onLoad_shouldPassForAnEntityThatIsNotFiltered() {
		interceptor.onLoad(new Role(), null, null, null, null);
	}
	
}
