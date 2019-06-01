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

public class DataFilterConstants {
	
	public static final String MODULE_ID = "datafilter";
	
	public static final String FILTER_PARAM_PATIENT_IDS = "patientIds";
	
	public static final String FILTER_NAME_ENCOUNTER = "encounterFilter";
	
	public static final String FILTER_CONDITION_PATIENT_ID = "patient_id in (:" + FILTER_PARAM_PATIENT_IDS + ")";
	
	public static final String GP_PERSON_ATTRIBUTE_TYPE_UUIDS = MODULE_ID + ".personAttributeTypeUuids";
	
}
