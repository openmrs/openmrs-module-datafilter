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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.context.Daemon;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.springframework.orm.hibernate5.SpringSessionContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SpringSessionContext.class, Daemon.class, Util.class })
public class DataFilterSessionContextTest {
	
	@Mock
	private SessionFactoryImplementor sfImpl;
	
	@Mock
	private Logger mockLogger;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		DataFilterSessionContext.reset();
	}
	
	@Test
	public void currentSession_shouldSkipIfTheMethodHasAlreadyBeenCalledOnTheCurrentThread() {
		ThreadLocal areFiltersSet = Whitebox.getInternalState(DataFilterSessionContext.class, "areFiltersSet");
		areFiltersSet.set(true);
		suppress(method(SpringSessionContext.class, "currentSession"));
		Whitebox.setInternalState(DataFilterSessionContext.class, "log", mockLogger);
		when(mockLogger.isTraceEnabled()).thenReturn(true);
		
		new DataFilterSessionContext(sfImpl).currentSession();
		
		verify(mockLogger, times(1)).isTraceEnabled();
		verify(mockLogger, times(1))
		        .trace(Matchers.startsWith("Skipping filter logic because filters are already set on the current session"));
	}
	
	@Test
	public void currentSession_shouldNotSkipIfTheMethodHasNotYetBeenCalledOnTheCurrentThread() {
		mockStatic(Daemon.class);
		mockStatic(Util.class);
		when(Daemon.isDaemonThread()).thenReturn(true);
		suppress(method(SpringSessionContext.class, "currentSession"));
		Whitebox.setInternalState(DataFilterSessionContext.class, "log", mockLogger);
		when(mockLogger.isTraceEnabled()).thenReturn(true);
		
		new DataFilterSessionContext(sfImpl).currentSession();
		
		verify(mockLogger, times(1)).isTraceEnabled();
		verify(mockLogger, times(1)).trace(Matchers.eq("Skipping enabling of filters on daemon thread"));
		assertTrue(
		    ((ThreadLocal<Boolean>) Whitebox.getInternalState(DataFilterSessionContext.class, "areFiltersSet")).get());
	}
	
}
