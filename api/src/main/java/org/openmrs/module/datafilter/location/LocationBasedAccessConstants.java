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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openmrs.module.datafilter.DataFilterConstants;

final class LocationBasedAccessConstants {
	
	public static final String LOCATION_BASED_FILTER_NAME_PREFIX = DataFilterConstants.MODULE_ID + "_locationBased";
	
	public static final String ENC_TYPE_PRIV_BASED_FILTER_NAME_PREFIX = DataFilterConstants.MODULE_ID + "_encTypePrivBased";
	
	public static final String PARAM_NAME_ATTRIB_TYPE_ID = "attributeTypeId";
	
	public static final String PARAM_NAME_BASIS_IDS = "basisIds";
	
	public static final String PARAM_NAME_ROLES = "roles";
	
	public static final String LOCATION_BASED_FILTER_NAME_ENCOUNTER = LOCATION_BASED_FILTER_NAME_PREFIX + "EncounterFilter";
	
	public static final String LOCATION_BASED_FILTER_NAME_VISIT = LOCATION_BASED_FILTER_NAME_PREFIX + "VisitFilter";
	
	public static final String LOCATION_BASED_FILTER_NAME_OBS = LOCATION_BASED_FILTER_NAME_PREFIX + "ObsFilter";
	
	public static final String LOCATION_BASED_FILTER_NAME_PATIENT = LOCATION_BASED_FILTER_NAME_PREFIX + "PatientFilter";
	
	public static final String ENC_TYPE_PRIV_BASED_FILTER_NAME_ENCOUNTER = ENC_TYPE_PRIV_BASED_FILTER_NAME_PREFIX
	        + "EncounterFilter";
	
	public static final String ENC_TYPE_PRIV_BASED_FILTER_NAME_OBS = ENC_TYPE_PRIV_BASED_FILTER_NAME_PREFIX + "ObsFilter";
	
	public static final Set<String> FILTER_NAMES = Stream.of(LOCATION_BASED_FILTER_NAME_PATIENT,
	    LOCATION_BASED_FILTER_NAME_VISIT, LOCATION_BASED_FILTER_NAME_ENCOUNTER, LOCATION_BASED_FILTER_NAME_OBS,
	    ENC_TYPE_PRIV_BASED_FILTER_NAME_ENCOUNTER, ENC_TYPE_PRIV_BASED_FILTER_NAME_OBS).collect(Collectors.toSet());
	
}
