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
	
	public static final String FILTER_NAME_ENCOUNTER = MODULE_ID + "_encounterFilter";
	
	public static final String FILTER_NAME_VISIT = MODULE_ID + "_visitFilter";
	
	public static final String FILTER_NAME_OBS = MODULE_ID + "_obsFilter";
	
	public static final String FILTER_NAME_PATIENT = MODULE_ID + "_patientFilter";
	
	public static final Set<String> FILTER_NAMES = Stream
	        .of(FILTER_NAME_PATIENT, FILTER_NAME_VISIT, FILTER_NAME_ENCOUNTER, FILTER_NAME_OBS).collect(Collectors.toSet());
	
	public static final String FULL_TEXT_FILTER_NAME_PATIENT = MODULE_ID + "_patientFullTextFilter";
	
	public static final String PARAM_NAME_ATTRIB_TYPE_ID = "attributeTypeId";
	
	public static final String PARAM_NAME_BASIS_IDS = "basisIds";
	
	public final static String ATTRIB_TYPE_ID_PLACEHOLDER = ":" + PARAM_NAME_ATTRIB_TYPE_ID;
	
	public final static String BASIS_IDS_PLACEHOLDER = ":" + PARAM_NAME_BASIS_IDS;
	
	public static final String PERSON_ID_QUERY = "SELECT DISTINCT pa.person_id FROM person_attribute pa WHERE "
	        + "pa.person_attribute_type_id = :" + PARAM_NAME_ATTRIB_TYPE_ID + " AND pa.value IN (:" + PARAM_NAME_BASIS_IDS
	        + ") AND pa.voided = 0";
	
	public static final String FILTER_CONDITION_PATIENT_ID = "patient_id in (" + PERSON_ID_QUERY + ")";
	
	public static final String GP_PERSON_ATTRIBUTE_TYPE_UUIDS = MODULE_ID + ".personAttributeTypeUuids";
	
	public static final String GP_RUN_IN_STRICT_MODE = MODULE_ID + ".runInStrictMode";
	
	public static final String ILLEGAL_RECORD_ACCESS_MESSAGE = "Illegal Record Access";
	
}
