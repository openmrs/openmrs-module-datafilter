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

import java.util.Collections;
import java.util.List;

import org.openmrs.BaseOpenmrsObject;

public class AccessUtil {
	
	private final static String ID_PLACEHOLDER = "$id";
	
	private final static String BASIS_TYPE_PLACEHOLDER = "$basis";
	
	private final static String ACCESSIBLE_PLACEHOLDER = "@accessibles";
	
	private final static String BASIC_QUERY = "select basis_id from " + DataFilterConstants.MODULE_ID
	        + "_user_basis_map where user_id = " + ID_PLACEHOLDER + " and basis_type = '" + BASIS_TYPE_PLACEHOLDER + "'";
	
	private final static String PERSON_QUERY = "select person_id from person_attribute where person_attribute_type_id = "
	        + ID_PLACEHOLDER + " and value in (" + ACCESSIBLE_PLACEHOLDER + ")";
	
	/**
	 * Gets the list of person ids for all the persons associated to the bases on the specified basis
	 * type, the basis could be of something like Location, Program etc.
	 * 
	 * @param basisType the type to base on
	 * @return a list of patient ids
	 */
	public static List<Integer> getAccessiblePatientIds(Class<? extends BaseOpenmrsObject> basisType) {
		return Collections.emptyList();
	}
	
	/**
	 * Gets the list of basis ids for all the bases the authenticated user is granted access to that
	 * match the specified basis type, the basis could be of something like Location, Program etc.
	 *
	 * @param basisType the type to base on
	 * @return a list of patient ids
	 */
	private static List<Integer> getAssignedBasisIds(Class<? extends BaseOpenmrsObject> basisType) {
		return Collections.emptyList();
	}
	
}
