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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.api.db.LocationDAO;
import org.openmrs.module.datafilter.DataFilterConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a facade for determining the list of person ids that the authenticated user
 * is granted to access to based on some basis, the basic could be something like a Location or a
 * Program.
 *
 * <pre>
 *     TODO This is a very simple implementation that should be replaced with a better one
 *     that doesn't use raw sql queries, supports caching of person ids, uses global properties,
 *     basis ids and other cool features.
 * </pre>
 */
public class AccessUtil {
	
	private static final Logger log = LoggerFactory.getLogger(AccessUtil.class);
	
	private final static String ENTITY_ID_PLACEHOLDER = "@id";
	
	private final static String ENTITY_TYPE_PLACEHOLDER = "@entityType";
	
	private final static String BASIS_TYPE_PLACEHOLDER = "@basisType";
	
	private final static String UUIDS_PLACEHOLDER = "@uuids";
	
	private final static String BASIS_QUERY = "SELECT DISTINCT basis_identifier FROM " + DataFilterConstants.MODULE_ID
	        + "_entity_basis_map WHERE entity_identifier = '" + ENTITY_ID_PLACEHOLDER + "' AND entity_type = '"
	        + ENTITY_TYPE_PLACEHOLDER + "' AND basis_type = '" + BASIS_TYPE_PLACEHOLDER + "'";
	
	/**
	 * Gets the collection of person ids for all the persons associated to the bases of the specified
	 * type, the basis could be something like Location, Program etc.
	 * 
	 * @param basisType the type to base on
	 * @return a set of patient ids
	 */
	public static Collection<String> getAccessiblePersonIds(Class<? extends BaseOpenmrsObject> basisType) {
		if (log.isDebugEnabled()) {
			log.debug("Looking up accessible persons for user with Id: " + Context.getAuthenticatedUser().getId());
		}
		
		Collection<String> accessibleBasisIds = getAssignedBasisIds(basisType);
		if (!accessibleBasisIds.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug(
				    "Filtering on " + basisType.getSimpleName() + "(s) with id(s): " + String.join(",", accessibleBasisIds));
			}
			
			String personQuery = ImplConstants.PERSON_ID_QUERY.replace(ImplConstants.BASIS_IDS_PLACEHOLDER,
			    String.join(",", accessibleBasisIds));
			List<List<Object>> personRows = executeQuery(personQuery);
			Set<String> personIds = new HashSet();
			personRows.forEach((List<Object> personRow) -> personIds.add(personRow.get(0).toString()));
			
			return personIds;
		}
		
		return Collections.EMPTY_SET;
	}
	
	/**
	 * Gets the collection of basis ids for all the bases the authenticated user is granted access to
	 * that match the specified basis type.
	 *
	 * @param basisType the type to base on
	 * @return a collection of basis ids
	 */
	protected static Collection<String> getAssignedBasisIds(Class<? extends BaseOpenmrsObject> basisType) {
		if (log.isDebugEnabled()) {
			log.debug("Looking up assigned bases for the authenticated user");
		}
		
		String userId = Context.getAuthenticatedUser().getUserId().toString();
		String query = BASIS_QUERY.replace(ENTITY_ID_PLACEHOLDER, userId);
		query = query.replace(ENTITY_TYPE_PLACEHOLDER, User.class.getName());
		query = query.replace(BASIS_TYPE_PLACEHOLDER, basisType.getName());
		
		List<List<Object>> rows = executeQuery(query);
		Set<String> basisIds = new HashSet();
		rows.forEach((List<Object> row) -> basisIds.add(row.get(0).toString()));
		
		//Include child locations in case of locations
		if (Location.class.isAssignableFrom(basisType)) {
			Set<String> descendantIds = new HashSet();
			for (String id : basisIds) {
				descendantIds.addAll(getAllDescendantLocationIds(id));
			}
			basisIds.addAll(descendantIds);
		}
		
		return basisIds;
	}
	
	/**
	 * Runs the specified query
	 * 
	 * @param query the query to execute
	 * @return A list of matching rows
	 */
	private static List<List<Object>> executeQuery(String query) {
		AdministrationDAO adminDAO = Context.getRegisteredComponent("adminDAO", AdministrationDAO.class);
		return adminDAO.executeSQL(query, true);
	}
	
	/**
	 * Gets all the child location ids for the location with the specified location id including those
	 * of nested child location at all levels.
	 * 
	 * @param locationId the location whose descendants location ids to return
	 * @return a collection of location ids
	 */
	private static Set<String> getAllDescendantLocationIds(String locationId) {
		Set<String> ids = new HashSet();
		LocationDAO dao = Context.getRegisteredComponent("locationDAO", LocationDAO.class);
		Location location = dao.getLocation(Integer.valueOf(locationId));
		for (Location l : location.getDescendantLocations(true)) {
			ids.add(l.getId().toString());
		}
		
		return ids;
	}
	
	/**
	 * Gets the view privilege for the encounter type matching the specified encounter type id
	 * 
	 * @param encounterTypeId the encounter type id to match
	 * @return the view privilege for the matched encounter type otherwise null
	 */
	public static String getViewPrivilege(Integer encounterTypeId) {
		if (encounterTypeId == null) {
			throw new APIException("Encounter type id is required");
		}
		final String query = "SELECT view_privilege FROM encounter_type WHERE encounter_type_id = " + encounterTypeId;
		List<List<Object>> rows = executeQuery(query);
		if (rows.isEmpty() || rows.get(0).isEmpty() || rows.get(0).get(0) == null) {
			return null;
		}
		return rows.get(0).get(0).toString();
	}
	
	/**
	 * Gets encounter type id for the encounter matching the specified encounter id
	 *
	 * @param encounterId encounter id to match
	 * @return the encounter type id for the matched encounter
	 */
	public static Integer getEncounterTypeId(Integer encounterId) {
		if (encounterId == null) {
			throw new APIException("Encounter id is required");
		}
		final String query = "SELECT encounter_type FROM encounter WHERE encounter_id = " + encounterId;
		return Integer.valueOf(executeQuery(query).get(0).get(0).toString());
	}
	
	/**
	 * Gets the list of all roles mapped to a program
	 * 
	 * @return a list of all program role names
	 */
	protected static Collection<String> getAllProgramRoles() {
		final String query = "SELECT DISTINCT entity_identifier FROM datafilter_entity_basis_map WHERE entity_type = '"
		        + Role.class.getName() + "' AND basis_type = '" + Program.class.getName() + "'";
		List<List<Object>> rows = executeQuery(query);
		List<String> roles = new ArrayList();
		for (List<Object> row : rows) {
			roles.add(row.get(0).toString());
		}
		
		return roles;
	}
	
}
