/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.lba;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.registration.DataFilterContext;
import org.openmrs.module.datafilter.registration.DataFilterListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("locationBasedDataFilterListener")
public class LocationBasedDataFilterListener implements DataFilterListener {
	
	private static final Logger log = LoggerFactory.getLogger(LocationBasedDataFilterListener.class);
	
	@Override
	public boolean supports(String filterName) {
		return LocationBasedAccessConstants.FILTER_NAMES.contains(filterName);
	}
	
	@Override
	public boolean onEnableFilter(DataFilterContext filterContext) {
		if (Context.isAuthenticated() && Context.getAuthenticatedUser().isSuperUser()) {
			if (log.isTraceEnabled()) {
				log.trace("Skipping enabling of filters for super user");
			}
			
			return false;
		}
		
		if (filterContext.getFilterName().startsWith(LocationBasedAccessConstants.LOCATION_BASED_FILTER_NAME_PREFIX)) {
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
			
			filterContext.setParameter(LocationBasedAccessConstants.PARAM_NAME_ATTRIB_TYPE_ID, attributeTypeId);
			filterContext.setParameter(LocationBasedAccessConstants.PARAM_NAME_BASIS_IDS, basisIds);
		} else if (filterContext.getFilterName()
		        .startsWith(LocationBasedAccessConstants.ENC_TYPE_PRIV_BASED_FILTER_NAME_PREFIX)) {
			Collection<String> roles = new HashSet();
			if (Context.isAuthenticated()) {
				Collection<String> allRoles = Context.getAuthenticatedUser().getAllRoles().stream().map(r -> r.getName())
				        .collect(Collectors.toSet());
				roles.addAll(allRoles);
			}
			
			filterContext.setParameter(LocationBasedAccessConstants.PARAM_NAME_ROLES, roles);
		}
		
		return true;
	}
	
}
