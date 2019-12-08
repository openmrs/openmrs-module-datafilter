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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.hibernate.annotations.FilterDef;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.module.datafilter.annotations.FilterDefAnnotation;
import org.openmrs.module.datafilter.registration.HibernateFilterParameter;
import org.openmrs.module.datafilter.registration.HibernateFilterRegistration;

public class UtilTest {
	
	private static final String TEST_HIBERNATE_CFG_FILE = "testHibernateCfg.xml";
	
	private static final String TEST_LOCATION_HBM_FILE = "testLocation.hbm.xml";
	
	private static XPath xpath = XPathFactory.newInstance().newXPath();
	
	@Test
	public void addAnnotationToClass_shouldAddTheSpecifiedAnnotationToTheSpecifiedClass()
	    throws ReflectiveOperationException {
		Class<? extends Annotation> annotationClass = FilterDef.class;
		Class<?> clazz = Concept.class;
		assertFalse(clazz.isAnnotationPresent(annotationClass));
		
		Util.addAnnotationToClass(clazz, new FilterDefAnnotation("some name", null, null));
		
		assertTrue(clazz.isAnnotationPresent(annotationClass));
	}
	
	@Test
	public void loadHibernateFilterRegistrations_shouldLoadAllHibernateFilterRegistrations() {
		assertEquals(11, Util.getHibernateFilterRegistrations().size());
	}
	
	@Test
	public void loadFullTextFilterRegistrations_shouldLoadAllFullTextFilterRegistrations() {
		assertEquals(1, Util.getFullTextFilterRegistrations().size());
	}
	
	@Test
	public void getMappingResource_shouldTheMappingResourceName() {
		assertEquals(TEST_LOCATION_HBM_FILE, Util.getMappingResource(TEST_HIBERNATE_CFG_FILE, Location.class.getName()));
		assertEquals("testEncounterType.hbm.xml",
		    Util.getMappingResource(TEST_HIBERNATE_CFG_FILE, EncounterType.class.getName()));
		assertNull(Util.getMappingResource(TEST_HIBERNATE_CFG_FILE, Concept.class.getName()));
	}
	
	@Test
	public void addFilterToMappingResource_shouldAddTheFilterToTheMappingResourceName() {
		final String filterName = "myFilterName";
		final String defaultCondition = "voided = 0";
		final String condition = "location_id > 5";
		HibernateFilterRegistration filterReg = new HibernateFilterRegistration();
		filterReg.setName(filterName);
		filterReg.setDefaultCondition(defaultCondition);
		filterReg.setCondition(condition);
		HibernateFilterParameter param1 = new HibernateFilterParameter();
		param1.setName("param1");
		param1.setType("string");
		HibernateFilterParameter param2 = new HibernateFilterParameter();
		param2.setName("param2");
		param2.setType("integer");
		filterReg.setParameters(Stream.of(param1, param2).collect(Collectors.toList()));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Util.addFilterToMappingResource(TEST_LOCATION_HBM_FILE, out, filterReg);
	}
	
}
