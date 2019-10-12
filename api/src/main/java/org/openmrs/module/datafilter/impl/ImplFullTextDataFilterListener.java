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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.DataFilterContext;
import org.openmrs.module.datafilter.DataFilterListener;
import org.openmrs.module.datafilter.FullTextDataFilterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("implFullTextDataFilterListener")
public class ImplFullTextDataFilterListener implements DataFilterListener {
	
	private static final Logger log = LoggerFactory.getLogger(ImplFullTextDataFilterListener.class);
	
	private static final HashMap<Class<?>, String> CLASS_FIELD_MAP;
	
	static {
		CLASS_FIELD_MAP = new HashMap(3);
		CLASS_FIELD_MAP.put(PersonName.class, "person.personId");
		CLASS_FIELD_MAP.put(PersonAttribute.class, "person.personId");
		CLASS_FIELD_MAP.put(PatientIdentifier.class, "patient.personId");
	}
	
	@Override
	public boolean supports(String filterName) {
		return ImplConstants.LOCATION_BASED_FULL_TEXT_FILTER_NAME_PATIENT.equals(filterName);
	}
	
	@Override
	public boolean onEnableFilter(DataFilterContext filterContext) {
		if (Context.isAuthenticated() && Context.getAuthenticatedUser().isSuperUser()) {
			if (log.isTraceEnabled()) {
				log.trace("Skipping enabling of filters for super user");
			}
			
			return false;
		}
		
		Collection<String> personIds = AccessUtil.getAccessiblePersonIds(Location.class);
		if (personIds.isEmpty()) {
			//If the user isn't granted access to patients at any basis, we add -1 because ids are all > 0,
			//in theory the query will match no records if the user isn't granted access to any basis
			personIds = Collections.singleton("-1");
		}
		
		filterContext.setParameter("field",
		    CLASS_FIELD_MAP.get(((FullTextDataFilterContext) filterContext).getEntityClass()));
		
		filterContext.setParameter("patientIds", personIds);
		
		return true;
	}
	
}
