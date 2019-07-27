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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataFilterConstants {
	
	public static final String MODULE_ID = "datafilter";
	
	public static final String LOCATION_BASED_FILTER_NAME_ENCOUNTER = MODULE_ID + "_locationBasedEncounterFilter";
	
	public static final String LOCATION_BASED_FILTER_NAME_VISIT = MODULE_ID + "_locationBasedVisitFilter";
	
	public static final String LOCATION_BASED_FILTER_NAME_OBS = MODULE_ID + "_locationBasedObsFilter";
	
	public static final String LOCATION_BASED_FILTER_NAME_PATIENT = MODULE_ID + "_locationBasedPatientFilter";
	
	public static final String ENC_TYPE_PRIV_BASED_FILTER_NAME_ENCOUNTER = MODULE_ID + "_encTypePrivBasedEncounterFilter";
	
	public static final String ENC_TYPE_PRIV_BASED_FILTER_NAME_OBS = MODULE_ID + "_encTypePrivBasedObsFilter";
	
	public static final Set<String> FILTER_NAMES = Stream.of(LOCATION_BASED_FILTER_NAME_PATIENT,
	    LOCATION_BASED_FILTER_NAME_VISIT, LOCATION_BASED_FILTER_NAME_ENCOUNTER, LOCATION_BASED_FILTER_NAME_OBS,
	    ENC_TYPE_PRIV_BASED_FILTER_NAME_ENCOUNTER, ENC_TYPE_PRIV_BASED_FILTER_NAME_OBS).collect(Collectors.toSet());
	
	public static final String LOCATION_BASED_FULL_TEXT_FILTER_NAME_PATIENT = MODULE_ID
	        + "_locationBasedPatientFullTextFilter";
	
	public static final String PARAM_NAME_ATTRIB_TYPE_ID = "attributeTypeId";
	
	public static final String PARAM_NAME_BASIS_IDS = "basisIds";
	
	public final static String ATTRIB_TYPE_ID_PLACEHOLDER = ":" + PARAM_NAME_ATTRIB_TYPE_ID;
	
	public final static String BASIS_IDS_PLACEHOLDER = ":" + PARAM_NAME_BASIS_IDS;
	
	public static final String PARAM_NAME_ROLES = "roles";
	
	public static final String PARAM_NAME_ROLES_PLACEHOLDER = ":" + PARAM_NAME_ROLES;
	
	public static final String PERSON_ID_QUERY = "SELECT DISTINCT pa.person_id FROM person_attribute pa WHERE "
	        + "pa.person_attribute_type_id = " + ATTRIB_TYPE_ID_PLACEHOLDER + " AND pa.value IN (" + BASIS_IDS_PLACEHOLDER
	        + ") AND pa.voided = 0";
	
	public static final String ENCOUNTER_ID_SUBQUERY = "SELECT DISTINCT e.encounter_id FROM encounter e INNER JOIN "
	        + "encounter_type et ON e.encounter_type = et.encounter_type_id WHERE et.view_privilege IS NULL OR "
	        + "et.view_privilege IN (SELECT DISTINCT rp.privilege FROM role_privilege rp WHERE rp.role IN ("
	        + PARAM_NAME_ROLES_PLACEHOLDER + "))";
	
	public static final String FILTER_CONDITION_PATIENT_ID = "patient_id IN (" + PERSON_ID_QUERY + ")";
	
	public static final String FILTER_CONDITION_ENCOUNTER_ID = "encounter_id IN (" + ENCOUNTER_ID_SUBQUERY + ")";
	
	public static final String GP_PERSON_ATTRIBUTE_TYPE_UUIDS = MODULE_ID + ".personAttributeTypeUuids";
	
	public static final String GP_RUN_IN_STRICT_MODE = MODULE_ID + ".runInStrictMode";
	
	public static final String DISABLED = "disabled";
	
	public static final String GP_LOCATION_BASED_FILTER_NAME_ENCOUNTER = LOCATION_BASED_FILTER_NAME_ENCOUNTER + "_"
	        + DISABLED;
	
	public static final String GP_LOCATION_BASED_FILTER_NAME_VISIT = LOCATION_BASED_FILTER_NAME_VISIT + "_" + DISABLED;
	
	public static final String GP_LOCATION_BASED_FILTER_NAME_OBS = LOCATION_BASED_FILTER_NAME_OBS + "_" + DISABLED;
	
	public static final String GP_LOCATION_BASED_FILTER_NAME_PATIENT = LOCATION_BASED_FILTER_NAME_PATIENT + "_" + DISABLED;
	
	public static final String GP_ENC_TYPE_PRIV_BASED_FILTER_NAME_ENCOUNTER = ENC_TYPE_PRIV_BASED_FILTER_NAME_ENCOUNTER + "_"
	        + DISABLED;
	
	public static final String GP_ENC_TYPE_PRIV_BASED_FILTER_NAME_OBS = ENC_TYPE_PRIV_BASED_FILTER_NAME_OBS + "_" + DISABLED;
	
	public static final String ILLEGAL_RECORD_ACCESS_MESSAGE = "Illegal Record Access";
	
}
