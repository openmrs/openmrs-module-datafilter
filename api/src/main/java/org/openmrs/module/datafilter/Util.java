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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.ParamDef;
import org.hibernate.cfg.Environment;
import org.hibernate.internal.util.xml.DTDEntityResolver;
import org.hibernate.search.annotations.FullTextFilterDefs;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.module.datafilter.annotations.AggregateAnnotation;
import org.openmrs.module.datafilter.annotations.FilterAnnotation;
import org.openmrs.module.datafilter.annotations.FilterDefAnnotation;
import org.openmrs.module.datafilter.annotations.FilterDefsAnnotation;
import org.openmrs.module.datafilter.annotations.FiltersAnnotation;
import org.openmrs.module.datafilter.annotations.FullTextFilterDefAnnotation;
import org.openmrs.module.datafilter.annotations.FullTextFilterDefsAnnotation;
import org.openmrs.module.datafilter.annotations.ParamDefAnnotation;
import org.openmrs.module.datafilter.registration.FullTextFilterRegistration;
import org.openmrs.module.datafilter.registration.HibernateFilterParameter;
import org.openmrs.module.datafilter.registration.HibernateFilterRegistration;
import org.openmrs.util.OpenmrsClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class Util {
	
	private static final Logger log = LoggerFactory.getLogger(Util.class);
	
	private static final String FILTER_PATH_PREFIX = "classpath*:/filters/";
	
	private static final String FILTER_PATH_SUFFIX = "/*.json";
	
	private static final String ADD_ENTITY_FILTER_XSLT_TEMPLATE = "add-entity-filter-xslt-template.xml";
	
	private static final String UPDATE_MAPPING_LOC_XSLT_TEMPLATE = "update-mapping-loc-xslt-template.xml";
	
	private static List<HibernateFilterRegistration> hibernateFilterRegistrations;
	
	private static List<FullTextFilterRegistration> fullTextFilterRegistrations;
	
	private static XPath xpath = XPathFactory.newInstance().newXPath();
	
	private static TransformerFactory transformerFactory = TransformerFactory.newInstance();
	
	private static DocumentBuilder documentBuilder;
	
	private static Template addEntityFilterXsltTemplate;
	
	private static Template updateMappingLocXsltTemplate;
	
	static {
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			documentBuilder.setEntityResolver(new DTDEntityResolver());
		}
		catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
		cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
		cfg.setClassLoaderForTemplateLoading(OpenmrsClassLoader.getInstance(), "");
		try {
			addEntityFilterXsltTemplate = cfg.getTemplate(ADD_ENTITY_FILTER_XSLT_TEMPLATE);
			updateMappingLocXsltTemplate = cfg.getTemplate(UPDATE_MAPPING_LOC_XSLT_TEMPLATE);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets the documentBuilder
	 *
	 * @return the documentBuilder
	 */
	public static DocumentBuilder getDocumentBuilder() {
		return documentBuilder;
	}
	
	/**
	 * Checks if the filter matching the specified name is disabled, every filter can be disabled via a
	 * global property, the name of the global property is the filter name with the a dot and disabled
	 * word appended to the end.
	 *
	 * @param filterName the name of the filter to match
	 * @return true if the filter is disabled otherwise false
	 */
	public static boolean isFilterDisabled(String filterName) {
		AdministrationDAO adminDAO = Context.getRegisteredComponent("adminDAO", AdministrationDAO.class);
		List<List<Object>> rows = adminDAO.executeSQL("SELECT property_value FROM global_property WHERE property = '"
		        + filterName + DataFilterConstants.DISABLED + "'",
		    true);
		if (rows.isEmpty() || rows.get(0).isEmpty() || rows.get(0).get(0) == null) {
			return false;
		}
		
		return "true".equalsIgnoreCase(rows.get(0).get(0).toString().trim());
	}
	
	protected static List<HibernateFilterRegistration> getHibernateFilterRegistrations() {
		if (hibernateFilterRegistrations == null) {
			loadFilterRegistrations(true);
		}
		
		return hibernateFilterRegistrations;
	}
	
	protected static List<FullTextFilterRegistration> getFullTextFilterRegistrations() {
		if (fullTextFilterRegistrations == null) {
			loadFilterRegistrations(false);
		}
		
		return fullTextFilterRegistrations;
	}
	
	/**
	 * Adds the defined filter annotations to persistent classes mapped with JPA annotations that need
	 * to be filtered.
	 */
	public static void initializeFilters() throws ReflectiveOperationException {
		log.info("Initializing filters");
		
		//Register hibernate filters
		for (HibernateFilterRegistration registration : getHibernateFilterRegistrations()) {
			if (registration.getProperty() == null) {
				ParamDef[] paramDefs = null;
				if (CollectionUtils.isNotEmpty(registration.getParameters())) {
					paramDefs = new ParamDef[registration.getParameters().size()];
					int index = 0;
					for (HibernateFilterParameter parameter : registration.getParameters()) {
						paramDefs[index] = new ParamDefAnnotation(parameter.getName(), parameter.getType());
						index++;
					}
				}
				
				for (Class clazz : registration.getTargetClasses()) {
					if (!clazz.isAnnotationPresent(Entity.class)) {
						continue;
					}
					
					registerFilter(clazz,
					    new FilterDefAnnotation(registration.getName(), registration.getDefaultCondition(), paramDefs),
					    new FilterAnnotation(registration.getName(), registration.getCondition()));
				}
				
			} else {
				//This is a filter to be applied to a property
				if (registration.getTargetClasses().size() > 1) {
					throw new APIException("Only one target class can be defined for a filter added to a property");
				}
				
				try {
					addAnnotationToField(registration.getProperty(), registration.getTargetClasses().get(0),
					    new FilterAnnotation(registration.getName(), registration.getCondition()));
				}
				catch (ReflectiveOperationException e) {
					throw new APIException(e);
				}
			}
		}
		
		//Register full text filters
		for (FullTextFilterRegistration registration : getFullTextFilterRegistrations()) {
			//Full text filters are added to one entity but can be enabled for any entity
			registerFullTextFilter(registration.getTargetClasses().get(0), new FullTextFilterDefAnnotation(
			        registration.getName(), registration.getImplClass(), registration.getCacheMode()));
		}
		
		Context.addConfigProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, DataFilterSessionContext.class.getName());
		
		log.info("Successfully initialized filters");
	}
	
	/**
	 * Adds the specified {@link org.hibernate.annotations.FilterDef} and
	 * {@link org.hibernate.annotations.Filter} annotations to the specified class object.
	 * 
	 * @param entityClass the class object to add the annotations
	 * @param filterDefAnnotation the {@link org.hibernate.annotations.FilterDef} annotation to add
	 * @param filterAnnotation the {@link org.hibernate.annotations.Filter} annotation to add
	 */
	protected static void registerFilter(Class<?> entityClass, FilterDefAnnotation filterDefAnnotation,
	        FilterAnnotation filterAnnotation) throws ReflectiveOperationException {
		
		addAnnotationToGroup(entityClass, FilterDefs.class, filterDefAnnotation);
		addAnnotationToGroup(entityClass, Filters.class, filterAnnotation);
	}
	
	/**
	 * Adds the specified {@link org.hibernate.search.annotations.FullTextFilterDef} annotation to the
	 * specified class object.
	 *
	 * @param entityClass the class object to add the annotation
	 * @param filterDefAnnotation the {@link org.hibernate.search.annotations.FullTextFilterDef}
	 *            annotation to add
	 */
	protected static void registerFullTextFilter(Class<?> entityClass, FullTextFilterDefAnnotation filterDefAnnotation)
	        throws ReflectiveOperationException {
		addAnnotationToGroup(entityClass, FullTextFilterDefs.class, filterDefAnnotation);
	}
	
	/**
	 * Utility method that adds a grouped annotation to it's containing aggregate annotation.
	 * 
	 * @param entityClass the class that has the aggregate annotation
	 * @param aggregateAnnClass the aggregate annotation type
	 * @param toAdd the grouped annotation instance to add
	 */
	private static void addAnnotationToGroup(Class<?> entityClass, Class<? extends Annotation> aggregateAnnClass,
	        Annotation toAdd) throws ReflectiveOperationException {
		
		Annotation aggregateAnnotation = entityClass.getAnnotation(aggregateAnnClass);
		if (aggregateAnnotation == null) {
			if (FilterDefs.class.equals(aggregateAnnClass)) {
				aggregateAnnotation = new FilterDefsAnnotation();
			} else if (Filters.class.equals(aggregateAnnClass)) {
				aggregateAnnotation = new FiltersAnnotation();
			} else if (FullTextFilterDefs.class.equals(aggregateAnnClass)) {
				aggregateAnnotation = new FullTextFilterDefsAnnotation();
			}
			
			addAnnotationToClass(entityClass, aggregateAnnotation);
		}
		
		((AggregateAnnotation) aggregateAnnotation).add(toAdd);
	}
	
	/**
	 * Adds the specified annotation to the specified class
	 * 
	 * @param clazz the class to add the annotation
	 * @param annotation the annotation to add
	 */
	public static void addAnnotationToClass(Class<?> clazz, Annotation annotation) throws ReflectiveOperationException {
		
		final String annotationName = annotation.annotationType().getName();
		if (log.isDebugEnabled()) {
			log.debug("Adding " + annotationName + " annotation to " + clazz.getName());
		}
		
		Method method = Class.class.getDeclaredMethod("getDeclaredAnnotationMap");
		boolean accessible = method.isAccessible();
		try {
			method.setAccessible(true);
			Map<Class<? extends Annotation>, Annotation> map = (Map<Class<? extends Annotation>, Annotation>) method
			        .invoke(clazz);
			//TODO handle the case where the annotation is already present in case of module restart
			map.put(annotation.annotationType(), annotation);
			
			if (log.isDebugEnabled()) {
				log.debug("Successfully added " + annotationName + " annotation to " + clazz.getName());
			}
		}
		catch (InvocationTargetException | IllegalAccessException e) {
			log.error("Failed to add " + annotationName + " annotation to " + clazz.getName(), e);
			throw e;
		}
		finally {
			//Always reset
			method.setAccessible(accessible);
		}
		
	}
	
	/**
	 * Adds an annotation to the field with a matching name in the specified class
	 * 
	 * @param fieldName the name of the field
	 * @param clazz the class to add the annotation
	 * @param annotation the annotation to add
	 */
	private static void addAnnotationToField(String fieldName, Class<?> clazz, Annotation annotation)
	        throws ReflectiveOperationException {
		
		final String annotationName = annotation.annotationType().getName();
		if (log.isDebugEnabled()) {
			log.debug("Adding " + annotationName + " annotation to " + clazz.getName() + "." + fieldName);
		}
		
		Field field = clazz.getDeclaredField(fieldName);
		boolean fieldAccessible = field.isAccessible();
		field.setAccessible(true);
		Method method = Field.class.getDeclaredMethod("declaredAnnotations");
		boolean methodAccessible = method.isAccessible();
		method.setAccessible(true);
		try {
			Map<Class<? extends Annotation>, Annotation> map = (Map<Class<? extends Annotation>, Annotation>) method
			        .invoke(field);
			
			//TODO handle the case where the annotation is already present in case of module restart
			//TODO We also need to take care of Filters annotations if present
			map.put(annotation.annotationType(), annotation);
			
			if (log.isDebugEnabled()) {
				log.debug("Successfully added " + annotationName + " annotation to " + clazz.getName() + "." + fieldName);
			}
		}
		catch (InvocationTargetException | IllegalAccessException e) {
			log.error("Failed to add " + annotationName + " annotation to " + clazz.getName() + "." + fieldName, e);
			throw e;
		}
		finally {
			//Always reset
			field.setAccessible(fieldAccessible);
			method.setAccessible(methodAccessible);
		}
	}
	
	/**
	 * Loads the registered filters in the json files
	 * 
	 * @param isHibernate specifies whether hibernate or full text filters are the ones to load
	 */
	private static synchronized void loadFilterRegistrations(boolean isHibernate) {
		if (log.isDebugEnabled()) {
			log.debug("Loading " + (isHibernate ? "hibernate" : "full text") + " filter registrations");
		}
		
		if (isHibernate) {
			if (hibernateFilterRegistrations != null) {
				return;
			}
			hibernateFilterRegistrations = new ArrayList<>();
		} else {
			if (fullTextFilterRegistrations != null) {
				return;
			}
			fullTextFilterRegistrations = new ArrayList<>();
		}
		
		//During openmrs Installation or upgrade, the thread context classloader is that of the webapp assigned
		//by the servlet container which doesn't know about module resources, so we need to use the openmrs one.
		//TODO See TRUNK-5678, when it is done then we can remove this logic
		ClassLoader classLoader = OpenmrsClassLoader.getInstance();
		PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(classLoader);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
		//Same here, we need to use the openmrs classloader to load the type field in case of module classes
		TypeFactory typeFactory = mapper.getTypeFactory().withClassLoader(classLoader);
		mapper.setTypeFactory(typeFactory);
		
		final String pathPattern = FILTER_PATH_PREFIX + (isHibernate ? "hibernate" : "fulltext") + FILTER_PATH_SUFFIX;
		
		try {
			Resource[] resources = resourceResolver.getResources(pathPattern);
			for (Resource resource : resources) {
				Class<?> clazz = isHibernate ? HibernateFilterRegistration.class : FullTextFilterRegistration.class;
				JavaType classListType = typeFactory.constructCollectionType(List.class, clazz);
				if (isHibernate) {
					hibernateFilterRegistrations.addAll(mapper.readValue(resource.getInputStream(), classListType));
				} else {
					fullTextFilterRegistrations.addAll(mapper.readValue(resource.getInputStream(), classListType));
				}
			}
		}
		catch (IOException e) {
			throw new APIException(
			        "Failed to load some " + (isHibernate ? "hibernate" : "full text") + " filter registrations", e);
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Successfully loaded " + (isHibernate ? "hibernate" : "full text") + " filter registrations");
		}
	}
	
	/**
	 * Parses the contents of the specified xml file
	 * 
	 * @param xmlFilename teh file name to parses
	 * @return a {@link Document} obecjt
	 */
	public static Document parseXmlFile(String xmlFilename) {
		try {
			return documentBuilder.parse(OpenmrsClassLoader.getInstance().getResourceAsStream(xmlFilename));
		}
		catch (SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets the hbm resource name for the class with the specified name from the specified hibernate cfg
	 * file, the returned value is actually the value of the resource attribute of the mapping for the
	 * class
	 * 
	 * @param cfgFilename the hibernate cfg file to search
	 * @param classname the fully qualified java class name to match
	 * @return the resource name
	 */
	public static String getMappingResource(String cfgFilename, String classname) {
		
		List<String> candidateResources = getMappingResources(cfgFilename);
		String mappingResource = null;
		for (String candidate : candidateResources) {
			String candidateClassName = getMappedClassName(candidate);
			if (classname.equals(candidateClassName)) {
				mappingResource = candidate;
				break;
			}
		}
		
		return mappingResource;
	}
	
	/**
	 * Gets the fully qualified java class name of the mapped class from the specified hbm file
	 * 
	 * @param hbmFilename the hbm file to search
	 * @return the name of the mapped class
	 */
	public static String getMappedClassName(String hbmFilename) {
		String className = readFromXmlFile("/hibernate-mapping/class/@name", hbmFilename, null);
		if (StringUtils.isBlank(className)) {
			//All sorts of crazy things are happening in modules
			if (log.isDebugEnabled()) {
				log.debug("Skipping " + hbmFilename + " because it is either empty or contains a subclass mapping");
			}
			
			return null;
		}
		
		if (className.indexOf('.') > 0) {
			//This is a fully qualified className, no need to get the package, some weird modules set the package
			//attribute value and still use a fully qualified class name and we would end with a bad classname
			return className;
		}
		
		String packageName = readFromXmlFile("/hibernate-mapping/@package", hbmFilename, null);
		
		if (StringUtils.isBlank(packageName)) {
			//Class not in a package? Warned you, module have all kinds of crazy stuff
			if (log.isDebugEnabled()) {
				log.debug(hbmFilename + " contains mapped class without a package");
			}
			
			return className;
		}
		
		return packageName + "." + className;
	}
	
	/**
	 * Evaluates the specified xpath expression against the specified xml and returns the value file.
	 * 
	 * @param xpathExpression the xpath expression to evaluate
	 * @param xmlFilename the name of the xml file to read from
	 * @param returnType the return type of the value
	 * @param <T>
	 * @return the evaluated value
	 */
	private static <T> T readFromXmlFile(String xpathExpression, String xmlFilename, QName returnType) {
		
		Document document = parseXmlFile(xmlFilename);
		try {
			XPathExpression expression = xpath.compile(xpathExpression);
			if (returnType == null) {
				return (T) expression.evaluate(document);
			}
			
			return (T) expression.evaluate(document, returnType);
		}
		catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets all the resource names of the mapping entries from the specified hibernate cfg file
	 * 
	 * @param cfgFilename the hibernate cfg file to search
	 * @return a list of all the resource names
	 */
	private static List<String> getMappingResources(String cfgFilename) {
		
		String xpathExpression = "/hibernate-configuration/session-factory/mapping/@resource";
		NodeList resourceAttributes = readFromXmlFile(xpathExpression, cfgFilename, XPathConstants.NODESET);
		List<String> mappingResources = new ArrayList();
		for (int i = 0; i < resourceAttributes.getLength(); i++) {
			mappingResources.add(resourceAttributes.item(i).getNodeValue());
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Discovered hbm files: " + mappingResources);
		}
		
		return mappingResources;
	}
	
	/**
	 * Processes the specified freemarker xslt template and applies the resulting xslt to a xml
	 * {@link InputStream} and adds the transformed bytes to the specified {@link OutputStream}.
	 * 
	 * @param in the {@link InputStream} for the xml payload
	 * @param xsltTemplate the freemarker {@link Template} of the xslt
	 * @param out the {@link OutputStream} to which to add the transformed bytes
	 * @param model a map of data to use to evaluate the freemarker template
	 */
	public static void applyXslt(InputStream in, Template xsltTemplate, OutputStream out, Map model) {
		try {
			ByteArrayOutputStream xsltOut = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(xsltOut);
			//Generate the final xslt to apply to the mapping resource for this filter registration
			xsltTemplate.process(model, writer);
			
			InputStream xslt = new ByteArrayInputStream(xsltOut.toByteArray());
			Transformer transformer = transformerFactory.newTransformer(new StreamSource(xslt));
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			Document doc = documentBuilder.parse(in);
			DocumentType docType = doc.getDoctype();
			if (docType != null) {
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, docType.getPublicId());
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, docType.getSystemId());
			}
			transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
			StreamResult result = new StreamResult(out);
			transformer.transform(new DOMSource(doc), result);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			IOUtils.closeQuietly(in);
		}
	}
	
	/**
	 * Adds a filter to a hbm mapping file.
	 * 
	 * @param in the {@link InputStream} of the hbm mapping resource to add the filter
	 * @param out the {@link OutputStream} to which to write the transformed mapping resource bytes
	 *            after the filters have been added
	 * @param filterReg the {@link org.openmrs.module.datafilter.registration.FilterRegistration} object
	 */
	public static void addFilterToMappingResource(InputStream in, OutputStream out, HibernateFilterRegistration filterReg) {
		Map<String, Object> model = new HashMap<>();
		model.put("filterName", filterReg.getName());
		model.put("defaultCondition", StringEscapeUtils.escapeXml(filterReg.getDefaultCondition()));
		model.put("condition", StringEscapeUtils.escapeXml(filterReg.getCondition()));
		model.put("parameters", filterReg.getParameters());
		
		applyXslt(in, addEntityFilterXsltTemplate, out, model);
	}
	
	/**
	 * Changes the location of the mapping file for a persistent entity in a hibernate cfg file by
	 * replacing the resource attribute with a file attribute.
	 * 
	 * @param in the {@link InputStream} of the hibernate cfg file
	 * @param resourceName the name of the resource for the entity i.e. the resource attribute value of
	 *            the mapping tag for the entity class
	 * @param resourceFilename the absolute path of the file to switch to
	 * @param out the {@link OutputStream} to which to write the new hibernate cfg bytes after switching
	 *            the mapping location
	 */
	public static void updateResourceLocation(InputStream in, String resourceName, String resourceFilename,
	        OutputStream out) {
		
		Map<String, String> model = new HashMap<>();
		model.put("resourceName", resourceName);
		model.put("resourceFilename", resourceFilename);
		
		applyXslt(in, updateMappingLocXsltTemplate, out, model);
	}
	
	/**
	 * Creates a new mapping file after adding the filters
	 * 
	 * @param hbmResourceName the name of the original mapping resource
	 * @param filterRegistrations list of FilterRegistrations to add to the mapping file
	 * @param outputDir the directory where to create the new files
	 * @return the new mapping file
	 * @throws IOException
	 */
	protected static File createNewMappingFile(String hbmResourceName, List<HibernateFilterRegistration> filterRegistrations,
	        File outputDir) throws IOException {
		
		InputStream in = OpenmrsClassLoader.getInstance().getResourceAsStream(hbmResourceName);
		ByteArrayOutputStream outFinal = null;
		
		for (HibernateFilterRegistration filterReg : filterRegistrations) {
			if (outFinal != null) {
				in = new ByteArrayInputStream(outFinal.toByteArray());
			}
			
			ByteArrayOutputStream outTemp = new ByteArrayOutputStream();
			Util.addFilterToMappingResource(in, outTemp, filterReg);
			outFinal = outTemp;
		}
		
		String hbmFilename = hbmResourceName;
		if (hbmFilename.indexOf("/") > 0) {
			hbmFilename = hbmFilename.substring(hbmFilename.lastIndexOf("/"));
		}
		
		File newMappingFile = FileUtils.getFile(outputDir, hbmFilename);
		
		FileUtils.writeByteArrayToFile(newMappingFile, outFinal.toByteArray());
		
		return newMappingFile;
	}
	
	/**
	 * Get all the classes mapped via hbm files that we need to add filters to along with their filter
	 * registrations
	 *
	 * @return a map of classes and their filter registrations
	 */
	protected static Map<Class<?>, List<HibernateFilterRegistration>> getClassFiltersMap() {
		Map<Class<?>, List<HibernateFilterRegistration>> classFiltersMap = new HashMap();
		
		List<HibernateFilterRegistration> filterRegistrations = Util.getHibernateFilterRegistrations();
		if (filterRegistrations.isEmpty()) {
			return classFiltersMap;
		}
		
		for (HibernateFilterRegistration filterReg : filterRegistrations) {
			for (Class<?> clazz : filterReg.getTargetClasses()) {
				if (!clazz.isAnnotationPresent(Entity.class)) {
					classFiltersMap.computeIfAbsent(clazz, k -> new ArrayList<>()).add(filterReg);
				}
			}
		}
		
		return classFiltersMap;
	}
	
	/**
	 * Checks if the specified filter should be skipped for the authenticated user.
	 *
	 * @param filterName the name of the filter to check
	 * @return true if the filter should be skipped for the user otherwise false
	 */
	public static boolean skipFilter(String filterName) {
		boolean hasByPassPriv = false;
		if (Context.isAuthenticated()) {
			hasByPassPriv = Context.hasPrivilege(filterName + DataFilterConstants.BYPASS_PRIV_SUFFIX);
		}
		
		return isFilterDisabled(filterName) || hasByPassPriv;
	}
	
}
