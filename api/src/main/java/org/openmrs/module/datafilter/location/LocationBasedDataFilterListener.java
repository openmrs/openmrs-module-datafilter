/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.location;

import static org.openmrs.module.datafilter.DataFilterConstants.ENC_TYPE_PRIV_BASED_FILTER_NAME_ENCOUNTER;
import static org.openmrs.module.datafilter.DataFilterConstants.ENC_TYPE_PRIV_BASED_FILTER_NAME_OBS;
import static org.openmrs.module.datafilter.DataFilterConstants.LOCATION_BASED_FILTER_NAME_ENCOUNTER;
import static org.openmrs.module.datafilter.DataFilterConstants.LOCATION_BASED_FILTER_NAME_OBS;
import static org.openmrs.module.datafilter.DataFilterConstants.LOCATION_BASED_FILTER_NAME_PATIENT;
import static org.openmrs.module.datafilter.DataFilterConstants.LOCATION_BASED_FILTER_NAME_VISIT;
import static org.openmrs.module.datafilter.DataFilterConstants.MODULE_ID;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.registration.DataFilterContext;
import org.openmrs.module.datafilter.registration.DataFilterListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LocationBasedDataFilterListener implements DataFilterListener {
	
	private static final Logger log = LoggerFactory.getLogger(LocationBasedDataFilterListener.class);
	
	public static final String LOCATION_BASED_FILTER_NAME_PREFIX = MODULE_ID + "_locationBased";
	
	public static final String ENC_TYPE_PRIV_BASED_FILTER_NAME_PREFIX = MODULE_ID + "_encTypePrivBased";
	
	public static final String PARAM_NAME_ATTRIB_TYPE_ID = "attributeTypeId";
	
	public static final String PARAM_NAME_BASIS_IDS = "basisIds";
	
	public static final String PARAM_NAME_ROLES = "roles";
	
	private static final Set<String> FILTER_NAMES = Stream.of(LOCATION_BASED_FILTER_NAME_PATIENT,
	    LOCATION_BASED_FILTER_NAME_VISIT, LOCATION_BASED_FILTER_NAME_ENCOUNTER, LOCATION_BASED_FILTER_NAME_OBS,
	    ENC_TYPE_PRIV_BASED_FILTER_NAME_ENCOUNTER, ENC_TYPE_PRIV_BASED_FILTER_NAME_OBS).collect(Collectors.toSet());
	
	@Override
	public boolean supports(String filterName) {
		return FILTER_NAMES.contains(filterName);
	}
	
	@Override
	public void onEnableFilter(DataFilterContext filterContext) {
		if (filterContext.getFilterName().startsWith(LOCATION_BASED_FILTER_NAME_PREFIX)) {
			Integer attributeTypeId = AccessUtil.getPersonAttributeTypeId(Location.class);
			//In tests, we can get here because test data is getting setup or flushed to the db
			if (attributeTypeId == null) {
				//In theory this should match no attribute type hence no patients
				attributeTypeId = -1;
			}
			
			Collection<String> basisIds = new HashSet();
			if (Context.isAuthenticated()) {
				basisIds.addAll(AccessUtil.getAssignedBasisIds(Location.class));
			}
			
			if (basisIds.isEmpty()) {
				//If the user isn't granted access to patients at any basis, we add -1 because ids are all > 0,
				//in theory the query will match no records if the user isn't granted access to any basis
				basisIds = Collections.singleton("-1");
			}
			
			filterContext.setParameter(PARAM_NAME_ATTRIB_TYPE_ID, attributeTypeId);
			filterContext.setParameter(PARAM_NAME_BASIS_IDS, basisIds);
		} else if (filterContext.getFilterName().startsWith(ENC_TYPE_PRIV_BASED_FILTER_NAME_PREFIX)) {
			Collection<String> roles = new HashSet();
			if (Context.isAuthenticated()) {
				Collection<String> allRoles = Context.getAuthenticatedUser().getAllRoles().stream().map(r -> r.getName())
				        .collect(Collectors.toSet());
				roles.addAll(allRoles);
			}
			
			filterContext.setParameter(PARAM_NAME_ROLES, roles);
		}
	}
	
}
