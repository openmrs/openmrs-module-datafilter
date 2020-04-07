/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.impl.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.Program;
import org.openmrs.User;
import org.openmrs.module.datafilter.DataFilterSessionContext;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.module.datafilter.impl.BaseFilterTest;
import org.openmrs.module.datafilter.impl.EntityBasisMap;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;

public class DataFilterServiceTest extends BaseFilterTest {
	
	@Autowired
	private DataFilterService service;
	
	@Before
	public void beforeTestMethod() {
		executeDataSet(TestConstants.MODULE_TEST_DATASET_XML);
	}
	
	@Test
	public void hasAccess_shouldReturnFalseIfTheUserHasNoAccessToTheSpecifiedBasis() throws Exception {
		assertFalse(service.hasAccess(new User(501), new Location(1)));
		assertFalse(service.hasAccess(new User(3000), new Program(2)));
	}
	
	@Test
	public void hasAccess_shouldReturnTrueIfTheUserHasAccessToTheSpecifiedBasis() {
		assertTrue(service.hasAccess(new User(3000), new Location(1)));
		assertTrue(service.hasAccess(new User(3000), new Location(4000)));
		assertTrue(service.hasAccess(new User(3000), new Program(1)));
		assertTrue(service.hasAccess(new User(501), new Location(4000)));
	}
	
	@Test
	public void grantAccess_shouldGrantTheUserAccessToRecordsAtTheSpecifiedBasis() {
		User user = new User(3000);
		Location location = new Location(4001);
		assertFalse(service.hasAccess(user, location));
		service.grantAccess(user, location);
		assertTrue(service.hasAccess(user, location));
	}
	
	@Test
	public void grantAccess_shouldGrantTheUserAccessToRecordsAtTheSpecifiedBases() {
		User user = new User(501);
		Collection<OpenmrsObject> bases = Stream.of(new Location(1), new Location(4001)).collect(Collectors.toSet());
		for (OpenmrsObject basis : bases) {
			assertFalse(service.hasAccess(user, basis));
		}
		
		assertNotNull(((ThreadLocal) Whitebox.getInternalState(DataFilterSessionContext.class, "areFiltersSet")).get());
		
		service.grantAccess(user, bases);
		
		assertNull(((ThreadLocal) Whitebox.getInternalState(DataFilterSessionContext.class, "areFiltersSet")).get());
		
		for (OpenmrsObject basis : bases) {
			assertTrue(service.hasAccess(user, basis));
		}
	}
	
	@Test
	public void revokeAccess_shouldRevokeAccessForTheUserToRecordsAtTheSpecifiedBasis() {
		User user = new User(3000);
		Location location = new Location(4000);
		assertTrue(service.hasAccess(user, location));
		service.revokeAccess(user, location);
		assertFalse(service.hasAccess(user, location));
	}
	
	@Test
	public void revokeAccess_shouldRevokeAccessForTheUserToRecordsAtTheSpecifiedBases() {
		User user = new User(3000);
		Collection<OpenmrsObject> bases = Stream.of(new Location(1), new Location(4000)).collect(Collectors.toSet());
		for (OpenmrsObject basis : bases) {
			assertTrue(service.hasAccess(user, basis));
		}
		
		assertNotNull(((ThreadLocal) Whitebox.getInternalState(DataFilterSessionContext.class, "areFiltersSet")).get());
		
		service.revokeAccess(user, bases);
		
		assertNull(((ThreadLocal) Whitebox.getInternalState(DataFilterSessionContext.class, "areFiltersSet")).get());
		
		for (OpenmrsObject basis : bases) {
			assertFalse(service.hasAccess(user, basis));
		}
	}

	@Test
	public void get_shouldGetAllEntityBasisForAnEntityForABasisType(){
		User user = new User(3000);
		Collection<EntityBasisMap> map = service.get(user,Location.class.getName());
		Assert.assertEquals(2,map.size());
	}
}
