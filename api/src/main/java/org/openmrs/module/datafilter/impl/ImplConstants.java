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

import static org.openmrs.module.datafilter.DataFilterConstants.DISABLED;
import static org.openmrs.module.datafilter.DataFilterConstants.MODULE_ID;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class ImplConstants {
	
	public static final String LOCATION_BASED_FILTER_NAME_PREFIX = MODULE_ID + "_locationBased";
	
	public static final String ENC_TYPE_PRIV_BASED_FILTER_NAME_PREFIX = MODULE_ID + "_encTypePrivBased";
	
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
	
	public static final Set<String> LOCATION_BASED_FILTER_NAMES = Stream.of(LOCATION_BASED_FILTER_NAME_PATIENT,
	    LOCATION_BASED_FILTER_NAME_VISIT, LOCATION_BASED_FILTER_NAME_ENCOUNTER, LOCATION_BASED_FILTER_NAME_OBS)
	        .collect(Collectors.toSet());
	
	public static final Set<String> ENC_TYPE_VIEW_PRIV_FILTER_NAMES = Stream
	        .of(ENC_TYPE_PRIV_BASED_FILTER_NAME_ENCOUNTER, ENC_TYPE_PRIV_BASED_FILTER_NAME_OBS).collect(Collectors.toSet());
	
	public static final Set<String> FILTER_NAMES;
	
	static {
		FILTER_NAMES = new HashSet();
		FILTER_NAMES.addAll(LOCATION_BASED_FILTER_NAMES);
		FILTER_NAMES.addAll(ENC_TYPE_VIEW_PRIV_FILTER_NAMES);
	}
	
	public static final String LOCATION_BASED_FULL_TEXT_FILTER_NAME_PATIENT = MODULE_ID
	        + "_locationBasedPatientFullTextFilter";
	
	public final static String ATTRIB_TYPE_ID_PLACEHOLDER = ":" + PARAM_NAME_ATTRIB_TYPE_ID;
	
	public final static String BASIS_IDS_PLACEHOLDER = ":" + PARAM_NAME_BASIS_IDS;
	
	public static final String PERSON_ID_QUERY = "SELECT DISTINCT pa.person_id FROM person_attribute pa WHERE "
	        + "pa.person_attribute_type_id = " + ATTRIB_TYPE_ID_PLACEHOLDER + " AND pa.value IN (" + BASIS_IDS_PLACEHOLDER
	        + ") AND pa.voided = 0";
	
	public static final String GP_PERSON_ATTRIBUTE_TYPE_UUIDS = MODULE_ID + ".personAttributeTypeUuids";
	
	public static final String GP_LOCATION_BASED_FILTER_NAME_ENCOUNTER = LOCATION_BASED_FILTER_NAME_ENCOUNTER + DISABLED;
	
	public static final String GP_LOCATION_BASED_FILTER_NAME_VISIT = LOCATION_BASED_FILTER_NAME_VISIT + DISABLED;
	
	public static final String GP_LOCATION_BASED_FILTER_NAME_OBS = LOCATION_BASED_FILTER_NAME_OBS + DISABLED;
	
	public static final String GP_LOCATION_BASED_FILTER_NAME_PATIENT = LOCATION_BASED_FILTER_NAME_PATIENT + DISABLED;
	
	public static final String GP_ENC_TYPE_PRIV_BASED_FILTER_NAME_ENCOUNTER = ENC_TYPE_PRIV_BASED_FILTER_NAME_ENCOUNTER
	        + DISABLED;
	
	public static final String GP_ENC_TYPE_PRIV_BASED_FILTER_NAME_OBS = ENC_TYPE_PRIV_BASED_FILTER_NAME_OBS + DISABLED;
	
	public static final String GP_RUN_IN_STRICT_MODE = MODULE_ID + ".runInStrictMode";
	
	public static final String ILLEGAL_RECORD_ACCESS_MESSAGE = "Illegal Record Access";
	
}
