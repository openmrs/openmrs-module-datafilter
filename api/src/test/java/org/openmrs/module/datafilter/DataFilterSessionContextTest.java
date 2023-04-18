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

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.context.Daemon;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.springframework.orm.hibernate5.SpringSessionContext;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*" })
@PrepareForTest({ SpringSessionContext.class, Daemon.class, Util.class })
public class DataFilterSessionContextTest {
	
	@Mock
	private SessionFactoryImplementor sfImpl;
	
	@Mock
	private Logger mockLogger;
	
	private AutoCloseable mocksCloseable = null;
	
	@Before
	public void setup() {
		mocksCloseable = MockitoAnnotations.openMocks(this);
		DataFilterSessionContext.reset();
	}
	
	@After
	public void tearDown() throws Exception {
		mocksCloseable.close();
	}
	
	@Test
	public void currentSession_shouldSkipIfTheMethodHasAlreadyBeenCalledOnTheCurrentThread() {
		mockStatic(Util.class);
		suppress(method(SpringSessionContext.class, "currentSession"));
		
		Session mockSession = mock(Session.class);
		when(sfImpl.openSession()).thenReturn(mockSession);
		Map<String, Object> sessionProperties = new HashMap<>();
		sessionProperties.put(DataFilterConstants.MODULE_ID + "filters.applied", "true");
		when(mockSession.getProperties()).thenReturn(sessionProperties);
		
		Whitebox.setInternalState(DataFilterSessionContext.class, "log", mockLogger);
		when(mockLogger.isTraceEnabled()).thenReturn(true);
		
		DataFilterSessionContext sessionContext = new DataFilterSessionContext(sfImpl) {
			
			@Override
			Session currentSessionInternal() {
				return mockSession;
			}
		};
		
		try (Session ignored = sessionContext.currentSession()) {
			verify(mockLogger, times(1))
			        .trace(startsWith("Skipping filter logic because filters are already set on the current session"));
		}
	}
	
	@Test
	public void currentSession_shouldNotSkipIfTheMethodHasNotYetBeenCalledOnTheCurrentThread() {
		mockStatic(Util.class);
		final Session mockSession = mock(Session.class);
		when(mockSession.getProperties()).thenReturn(mock(Map.class));
		Whitebox.setInternalState(DataFilterSessionContext.class, "log", mockLogger);
		
		DataFilterSessionContext sessionContext = new DataFilterSessionContext(sfImpl) {
			
			@Override
			Session currentSessionInternal() {
				return mockSession;
			}
		};
		
		try (Session ignored = sessionContext.currentSession()) {
			verify(mockSession, times(1)).setProperty(eq(DataFilterConstants.MODULE_ID + "filters.applied"), eq("true"));
		}
	}
	
	@Test
	public void currentSession_shouldSkipIfTheMethodIsCalledOnADaemonThread() {
		mockStatic(Daemon.class);
		mockStatic(Util.class);
		PowerMockito.when(Daemon.isDaemonThread()).thenReturn(true);
		suppress(method(SpringSessionContext.class, "currentSession"));
		
		final Session mockSession = mock(Session.class);
		when(mockSession.getProperties()).thenReturn(mock(Map.class));
		
		Whitebox.setInternalState(DataFilterSessionContext.class, "log", mockLogger);
		
		DataFilterSessionContext sessionContext = new DataFilterSessionContext(sfImpl) {
			
			@Override
			Session currentSessionInternal() {
				return mockSession;
			}
		};
		
		try (Session ignored = sessionContext.currentSession()) {
			verify(mockLogger, times(1)).trace(startsWith("Skipping enabling of filters on daemon thread"));
		}
	}
	
}
