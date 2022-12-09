/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.DataFilterContext;
import org.openmrs.module.datafilter.DataFilterListener;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AccessUtil.class, Context.class })
public class DataFilterListenerTest {
	
	private DataFilterListener dataFilterListener;
	
	@Before
	public void before() {
		mockStatic(AccessUtil.class);
		mockStatic(Context.class);
		when(AccessUtil.getAllProgramRoles()).thenReturn(new ArrayList<String>());
		when(Context.isAuthenticated()).thenReturn(false);
		dataFilterListener = new ImplDataFilterListener();
	}
	
	@Test
	public void onEnableFilter_shouldReplaceRolePlaceholdersWithDefaultValues() {
		// setup
		Context.logout();
		Map<String, Map<String, Object>> filterAndParamValueMap = new HashMap<String, Map<String, Object>>();
		filterAndParamValueMap.put(ImplConstants.PROGRAM_BASED_FILTER_NAME_PREFIX, new HashMap<String, Object>());
		
		// replay
		dataFilterListener.onEnableFilter(
		    new DataFilterContext(ImplConstants.PROGRAM_BASED_FILTER_NAME_PREFIX, filterAndParamValueMap));
		
		// verify
		for (String key : filterAndParamValueMap.get(ImplConstants.PROGRAM_BASED_FILTER_NAME_PREFIX).keySet()) {
			assertEquals("[#####]",
			    filterAndParamValueMap.get(ImplConstants.PROGRAM_BASED_FILTER_NAME_PREFIX).get(key).toString());
		}
	}
}
