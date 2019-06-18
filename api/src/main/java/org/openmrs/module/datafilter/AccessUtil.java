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

import static org.openmrs.module.datafilter.DataFilterConstants.GP_PERSON_ATTRIBUTE_TYPE_UUIDS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;

/**
 * This class provides a facade for determining the list of person ids that the authenticated user
 * is granted to access to based on some basis, the basic could be something like a Location or a
 * Program.
 *
 * <pre>
 *     TODO This is a very simple implementation that should be replaced with a better one
 *     that doesn't use raw sql queries, supports caching of person ids, used global properties,
 *     basis ids and other cool other features.
 * </pre>
 */
public class AccessUtil {
	
	private static final Log log = LogFactory.getLog(AccessUtil.class);
	
	private final static String ID_PLACEHOLDER = "@id";
	
	private final static String BASIS_TYPE_PLACEHOLDER = "@basis";
	
	private final static String UUIDS_PLACEHOLDER = "@uuids";
	
	private final static String BASIS_QUERY = "select basis_id from " + DataFilterConstants.MODULE_ID
	        + "_user_basis_map where user_id = " + ID_PLACEHOLDER + " and basis_type = '" + BASIS_TYPE_PLACEHOLDER + "'";
	
	private final static String GP_QUERY = "select property_value from global_property where property = '"
	        + GP_PERSON_ATTRIBUTE_TYPE_UUIDS + "'";
	
	private final static String ATTRIBUTE_TYPE_QUERY = "select person_attribute_type_id, format from person_attribute_type where uuid in ("
	        + UUIDS_PLACEHOLDER + ")";
	
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
		
		Integer attributeTypeId = getPersonAttributeTypeId(basisType);
		
		if (attributeTypeId == null) {
			throw new APIException("No person attribute type is configured to support filtering by " + basisType.getName());
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Filtering by person attribute type with id " + attributeTypeId);
		}
		
		Collection<String> accessibleBasisIds = getAssignedBasisIds(basisType);
		if (!accessibleBasisIds.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug(
				    "Filtering on " + basisType.getSimpleName() + "(s) with id(s): " + String.join(",", accessibleBasisIds));
			}
			
			if (Location.class.isAssignableFrom(basisType)) {
				Set<String> descendantIds = new HashSet();
				for (String id : accessibleBasisIds) {
					descendantIds.addAll(getAllDescendantLocationIds(id));
				}
				accessibleBasisIds.addAll(descendantIds);
			}
			
			String personQuery = DataFilterConstants.PERSON_ID_QUERY
			        .replace(DataFilterConstants.ATTRIB_TYPE_ID_PLACEHOLDER, attributeTypeId.toString())
			        .replace(DataFilterConstants.BASIS_IDS_PLACEHOLDER, String.join(",", accessibleBasisIds));
			List<List<Object>> personRows = runQueryWithElevatedPrivileges(personQuery);
			Set<String> personIds = new HashSet();
			personRows.forEach((List<Object> personRow) -> personIds.add(personRow.get(0).toString()));
			
			return personIds;
		}
		
		return Collections.EMPTY_SET;
	}
	
	/**
	 * Gets the collection of basis ids in single quotes for all the bases the authenticated user is
	 * granted access to that match the specified basis type.
	 *
	 * @param basisType the type to base on
	 * @return a collection of basis ids
	 */
	public static Collection<String> getAssignedBasisIds(Class<? extends BaseOpenmrsObject> basisType) {
		if (log.isDebugEnabled()) {
			log.debug("Looking up assigned bases for the authenticated user");
		}
		
		String userId = Context.getAuthenticatedUser().getUserId().toString();
		String query = BASIS_QUERY.replace(ID_PLACEHOLDER, userId).replace(BASIS_TYPE_PLACEHOLDER, basisType.getName());
		
		List<List<Object>> rows = runQueryWithElevatedPrivileges(query);
		Set<String> basisIds = new HashSet();
		rows.forEach((List<Object> row) -> basisIds.add(row.get(0).toString()));
		
		return basisIds;
	}
	
	/**
	 * Runs the specified query with PrivilegeConstants.SQL_LEVEL_ACCESS enabled.
	 * 
	 * <pre>
	 * TODO Use Daemon.runInDaemonThread instead, probably when this class is reimplemented
	 * </pre>
	 * 
	 * @param query the query to execute
	 * @return A list of matching rows
	 */
	private static List<List<Object>> runQueryWithElevatedPrivileges(String query) {
		try {
			//TODO Use Daemon.runInDaemonThread instead, probably when this class is reimplemented
			Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
			return Context.getAdministrationService().executeSQL(query, true);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		}
	}
	
	/**
	 * Gets the id of the person attribute type that matches any of the uuids configured via the
	 * {@link DataFilterConstants#GP_PERSON_ATTRIBUTE_TYPE_UUIDS} for the specified basis type.
	 * 
	 * @param basisType the basis type to match
	 * @return the if of the person attribute type
	 */
	public static Integer getPersonAttributeTypeId(Class<?> basisType) {
		//TODO This method should be moved to a GlobalPropertyListener so that we can cache the ids
		List<List<Object>> rows = runQueryWithElevatedPrivileges(GP_QUERY);
		String attribTypeUuids = null;
		if (!rows.isEmpty()) {
			if (rows.get(0).get(0) == null) {
				log.warn("The value for the " + GP_PERSON_ATTRIBUTE_TYPE_UUIDS + " global property is not yet set");
				throw new APIException("Failed to load accessible persons");
			}
			attribTypeUuids = rows.get(0).get(0).toString();
		}
		
		if (StringUtils.isNotBlank(attribTypeUuids)) {
			List<String> quotedUuids = new ArrayList();
			for (String uuid : StringUtils.split(attribTypeUuids, ",")) {
				if (StringUtils.isNotBlank(uuid)) {
					quotedUuids.add("'" + uuid.trim() + "'");
				}
			}
			String attributeQuery = ATTRIBUTE_TYPE_QUERY.replace(UUIDS_PLACEHOLDER, String.join(",", quotedUuids));
			List<List<Object>> attributeRows = runQueryWithElevatedPrivileges(attributeQuery);
			for (List<Object> row : attributeRows) {
				if (basisType.getName().equals(row.get(1))) {
					return Integer.valueOf(row.get(0).toString());
				}
			}
		}
		
		return null;
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
		Location location;
		try {
			//TODO Use Daemon.runInDaemonThread instead, probably when this class is reimplemented
			Context.addProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
			location = Context.getLocationService().getLocation(Integer.valueOf(locationId));
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_LOCATIONS);
		}
		
		for (Location l : location.getDescendantLocations(true)) {
			ids.add(l.getId().toString());
		}
		
		return ids;
	}
	
}
