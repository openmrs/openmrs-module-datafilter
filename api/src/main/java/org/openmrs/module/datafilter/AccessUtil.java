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
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsObject;
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
	
	private final static String BASIS_IDS_PLACEHOLDER = "@basisIds";
	
	private final static String UUIDS_PLACEHOLDER = "@uuids";
	
	private final static String BASIS_QUERY = "select basis_id from " + DataFilterConstants.MODULE_ID
	        + "_user_basis_map where user_id = " + ID_PLACEHOLDER + " and basis_type = '" + BASIS_TYPE_PLACEHOLDER + "'";
	
	private final static String PERSON_QUERY = "select person_id from person_attribute where person_attribute_type_id = "
	        + ID_PLACEHOLDER + " and value in (" + BASIS_IDS_PLACEHOLDER + ") and voided = 0";
	
	private final static String GP_QUERY = "select property_value from global_property where property = '"
	        + GP_PERSON_ATTRIBUTE_TYPE_UUIDS + "'";
	
	private final static String ATTRIBUTE_TYPE_QUERY = "select person_attribute_type_id, format from person_attribute_type where uuid in ("
	        + UUIDS_PLACEHOLDER + ")";
	
	/**
	 * Gets the list of person ids for all the persons associated to the bases of the specified type,
	 * the basis could be something like Location, Program etc.
	 * 
	 * @param basisType the type to base on
	 * @return a list of patient ids
	 */
	public static List<String> getAccessiblePersonIds(Class<? extends BaseOpenmrsObject> basisType) {
		if (log.isDebugEnabled()) {
			log.debug("Looking up accessible persons for user with Id: " + Context.getAuthenticatedUser().getId());
		}
		
		List<List<Object>> rows = runQueryWithElevatedPrivileges(GP_QUERY);
		String attribTypeUuids = null;
		if (!rows.isEmpty()) {
			if (rows.get(0).get(0) == null) {
				log.warn("The value for the " + GP_PERSON_ATTRIBUTE_TYPE_UUIDS + " global property is not yet set");
				throw new APIException("Failed to load accessible persons");
			}
			attribTypeUuids = rows.get(0).get(0).toString();
		}
		if (StringUtils.isBlank(attribTypeUuids)) {
			return Collections.EMPTY_LIST;
		}
		
		List<String> quotedUuids = new ArrayList();
		for (String uuid : StringUtils.split(attribTypeUuids, ",")) {
			if (StringUtils.isNotBlank(uuid)) {
				quotedUuids.add("'" + uuid.trim() + "'");
			}
		}
		
		String attributeQuery = ATTRIBUTE_TYPE_QUERY.replace(UUIDS_PLACEHOLDER, String.join(",", quotedUuids));
		List<List<Object>> attributeRows = runQueryWithElevatedPrivileges(attributeQuery);
		String attributeTypeId = null;
		for (List<Object> row : attributeRows) {
			if (basisType.getName().equals(row.get(1))) {
				attributeTypeId = row.get(0).toString();
				break;
			}
		}
		
		if (attributeTypeId == null) {
			throw new APIException("No person attribute type is configured to support filtering by " + basisType.getName());
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Filtering by person attribute type with id " + attributeTypeId);
		}
		
		List<String> accessibleBasisIds = getAssignedBasisIds(basisType);
		if (!accessibleBasisIds.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug(
				    "Filtering on " + basisType.getSimpleName() + "(s) with id(s): " + String.join(",", accessibleBasisIds));
			}
			
			String personQuery = PERSON_QUERY.replace(ID_PLACEHOLDER, attributeTypeId).replace(BASIS_IDS_PLACEHOLDER,
			    String.join(",", accessibleBasisIds));
			List<List<Object>> personRows = runQueryWithElevatedPrivileges(personQuery);
			List<String> personIds = new ArrayList<>();
			personRows.forEach((List<Object> personRow) -> personIds.add(personRow.get(0).toString()));
			
			return personIds;
		}
		
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * Gets the list of basis ids in single quotes for all the bases the authenticated user is granted
	 * access to that match the specified basis type.
	 *
	 * @param basisType the type to base on
	 * @return a list of basis ids
	 */
	public static List<String> getAssignedBasisIds(Class<? extends BaseOpenmrsObject> basisType) {
		if (log.isDebugEnabled()) {
			log.debug("Looking up assigned bases for the authenticated user");
		}
		
		String userId = Context.getAuthenticatedUser().getUserId().toString();
		String query = BASIS_QUERY.replace(ID_PLACEHOLDER, userId).replace(BASIS_TYPE_PLACEHOLDER, basisType.getName());
		
		List<List<Object>> rows = runQueryWithElevatedPrivileges(query);
		List<String> basisIds = new ArrayList<>();
		rows.forEach((List<Object> row) -> basisIds.add("'" + row.get(0).toString() + "'"));
		
		return basisIds;
	}
	
	/**
	 * Runs the speficied query with PrivilegeConstants.SQL_LEVEL_ACCESS enabled.
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
			//TODO Use Daeamon.runInDaemonThread instead, probably when this class is reimplemented
			Context.addProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
			return Context.getAdministrationService().executeSQL(query, true);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.SQL_LEVEL_ACCESS);
		}
	}
	
}
