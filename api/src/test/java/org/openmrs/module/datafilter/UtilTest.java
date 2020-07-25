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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.openmrs.module.datafilter.DataFilterConstants.BYPASS_PRIV_SUFFIX;
import static org.openmrs.module.datafilter.DataFilterConstants.MODULE_ID;
import static org.openmrs.module.datafilter.Util.getDocumentBuilder;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.FilterDef;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.module.datafilter.annotations.FilterDefAnnotation;
import org.openmrs.module.datafilter.registration.HibernateFilterParameter;
import org.openmrs.module.datafilter.registration.HibernateFilterRegistration;
import org.openmrs.util.OpenmrsClassLoader;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Document;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class, FileUtils.class })
public class UtilTest {
	
	private static final String TEST_HIBERNATE_CFG_FILE = "testHibernateCfg.xml";
	
	private static final String TEST_ENC_TYPE_HBM_FILE = "testEncounterType.hbm.xml";
	
	private static final String TEST_LOCATION_HBM_FILE = "testLocation.hbm.xml";
	
	private static final String PATH_FILTER_DEF = "/hibernate-mapping/filter-def";
	
	private static final String PATH_FILTER_DEF_PARAM = PATH_FILTER_DEF + "/filter-param";
	
	private static final String PATH_FILTER = "/hibernate-mapping/class/filter";
	
	private static final String PATH_MAPPING = "/hibernate-configuration/session-factory/mapping";
	
	private static XPath xpath = XPathFactory.newInstance().newXPath();
	
	@Mock
	private AdministrationDAO adminDAO;
	
	public static String getAttribute(Object document, String path, String attribute) throws XPathExpressionException {
		return xpath.compile(path + "/@" + attribute).evaluate(document);
	}
	
	public static boolean elementExists(Object doc, String path) throws XPathExpressionException {
		return getCount(doc, path) > 0;
	}
	
	public static int getCount(Object doc, String path) throws XPathExpressionException {
		return Integer.valueOf(xpath.compile("count(" + path + ")").evaluate(doc));
	}
	
	public static boolean elementExists(Object doc, String path, String attribName, String attribValue)
	        throws XPathExpressionException {
		return getCount(doc, path + "[@" + attribName + "='" + attribValue + "']") > 0;
	}
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
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
		assertEquals(19, Util.getHibernateFilterRegistrations().size());
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
	public void addFilterToMappingResource_shouldAddTheFilterToTheMappingResourceName() throws Exception {
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
		InputStream in = OpenmrsClassLoader.getInstance().getResourceAsStream(TEST_LOCATION_HBM_FILE);
		
		Util.addFilterToMappingResource(in, out, filterReg);
		
		Document updatedResource = getDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
		assertTrue(elementExists(updatedResource, PATH_FILTER_DEF));
		assertTrue(elementExists(updatedResource, PATH_FILTER));
		assertEquals(filterName, getAttribute(updatedResource, PATH_FILTER_DEF, "name"));
		assertEquals(defaultCondition, getAttribute(updatedResource, PATH_FILTER_DEF, "condition"));
		assertEquals(filterName, getAttribute(updatedResource, PATH_FILTER, "name"));
		assertEquals(condition, getAttribute(updatedResource, PATH_FILTER, "condition"));
		assertEquals(2, getCount(updatedResource, PATH_FILTER_DEF_PARAM));
		assertTrue(elementExists(updatedResource, PATH_FILTER_DEF_PARAM, "name", param1.getName()));
		assertTrue(elementExists(updatedResource, PATH_FILTER_DEF_PARAM, "type", param1.getType()));
		assertTrue(elementExists(updatedResource, PATH_FILTER_DEF_PARAM, "name", param2.getName()));
		assertTrue(elementExists(updatedResource, PATH_FILTER_DEF_PARAM, "type", param2.getType()));
	}
	
	@Test
	public void addFilterToMappingResource_shouldAddTheFiltersIgnoringNullConditionsAndParameters() throws Exception {
		final String filterName = "myFilterName";
		HibernateFilterRegistration filterReg = new HibernateFilterRegistration();
		filterReg.setName(filterName);
		filterReg.setCondition(" ");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = OpenmrsClassLoader.getInstance().getResourceAsStream(TEST_LOCATION_HBM_FILE);
		
		Util.addFilterToMappingResource(in, out, filterReg);
		
		Document updatedResource = getDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
		assertTrue(elementExists(updatedResource, PATH_FILTER_DEF));
		assertTrue(elementExists(updatedResource, PATH_FILTER));
		assertEquals(filterName, getAttribute(updatedResource, PATH_FILTER_DEF, "name"));
		assertTrue(StringUtils.isBlank(getAttribute(updatedResource, PATH_FILTER_DEF, "condition")));
		assertEquals(filterName, getAttribute(updatedResource, PATH_FILTER, "name"));
		assertTrue(StringUtils.isBlank(getAttribute(updatedResource, PATH_FILTER, "condition")));
	}
	
	@Test
	public void updateResourceLocation_shouldReplaceMappingResourceLocationsWithFileLocations() throws Exception {
		mockStatic(FileUtils.class);
		String expectedFilePath = "/tmp/path/" + MODULE_ID + "/" + TEST_LOCATION_HBM_FILE;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = OpenmrsClassLoader.getInstance().getResourceAsStream(TEST_HIBERNATE_CFG_FILE);
		
		Util.updateResourceLocation(in, TEST_LOCATION_HBM_FILE, expectedFilePath, out);
		
		Document updatedCfg = getDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
		assertEquals(1, getCount(updatedCfg, PATH_MAPPING + "[@file]"));
		assertEquals(expectedFilePath, getAttribute(updatedCfg, PATH_MAPPING, "file"));
		assertFalse(elementExists(updatedCfg, PATH_MAPPING, "resource", TEST_LOCATION_HBM_FILE));
		//The other mapping resources should not have been updated
		assertTrue(elementExists(updatedCfg, PATH_MAPPING, "resource", TEST_ENC_TYPE_HBM_FILE));
	}
	
	@Test
	public void isFilterDisabled_shouldReturnFalseIfTheDisableGPForTheFilterIsNotSet() {
		mockStatic(Context.class);
		when(Context.getRegisteredComponent("adminDAO", AdministrationDAO.class)).thenReturn(adminDAO);
		when(adminDAO.executeSQL(anyString(), eq(true))).thenReturn(Collections.emptyList());
		assertFalse(Util.isFilterDisabled("someFilter"));
	}
	
	@Test
	public void isFilterDisabled_shouldReturnFalseIfTheDisableGPForTheFilterIsSetToFalse() {
		mockStatic(Context.class);
		when(Context.getRegisteredComponent("adminDAO", AdministrationDAO.class)).thenReturn(adminDAO);
		List<List<Object>> expectedRows = Collections.singletonList(Collections.singletonList("false"));
		when(adminDAO.executeSQL(anyString(), eq(true))).thenReturn(expectedRows);
		assertFalse(Util.isFilterDisabled("someFilter"));
	}
	
	@Test
	public void isFilterDisabled_shouldReturnTrueIfTheDisableGPForTheFilterISetToTrue() {
		mockStatic(Context.class);
		when(Context.getRegisteredComponent("adminDAO", AdministrationDAO.class)).thenReturn(adminDAO);
		List<List<Object>> expectedRows = Collections.singletonList(Collections.singletonList("true"));
		when(adminDAO.executeSQL(anyString(), eq(true))).thenReturn(expectedRows);
		assertTrue(Util.isFilterDisabled("someFilter"));
	}
	
	@Test
	public void skipFilter_shouldReturnFalseIfTheFilterIsNotDisabledAndTheUserHasNoByPassPrivilege() {
		mockStatic(Context.class);
		when(Context.getRegisteredComponent("adminDAO", AdministrationDAO.class)).thenReturn(adminDAO);
		assertFalse(Util.skipFilter("someFilter"));
	}
	
	@Test
	public void skipFilter_shouldReturnTrueIfTheFilterIsDisabled() {
		mockStatic(Context.class);
		when(Context.getRegisteredComponent("adminDAO", AdministrationDAO.class)).thenReturn(adminDAO);
		List<List<Object>> expectedRows = Collections.singletonList(Collections.singletonList("true"));
		when(adminDAO.executeSQL(anyString(), eq(true))).thenReturn(expectedRows);
		final String filterName = "someFilter";
		assertTrue(Util.skipFilter(filterName));
	}
	
	@Test
	public void skipFilter_shouldReturnTrueIfTheUserHasTheIndividualFilterByPassPrivilege() {
		mockStatic(Context.class);
		when(Context.getRegisteredComponent("adminDAO", AdministrationDAO.class)).thenReturn(adminDAO);
		final String filterName = "someFilter";
		PowerMockito.when(Context.isAuthenticated()).thenReturn(true);
		PowerMockito.when(Context.hasPrivilege(filterName + BYPASS_PRIV_SUFFIX)).thenReturn(true);
		assertTrue(Util.skipFilter("someFilter"));
	}
	
}
