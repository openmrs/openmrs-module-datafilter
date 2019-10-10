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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.BytesRef;
import org.hibernate.search.annotations.Factory;

/**
 * Full text Filter that can be applied to patient records and any domain objects that represent
 * patient clinical data e.g. Visits, Encounters, Obs, Orders etc.
 */
public class PatientIdFullTextFilter {
	
	private String field;
	
	private Set<String> patientIds;
	
	/**
	 * Sets the field
	 *
	 * @param field the field to set
	 */
	public void setField(String field) {
		this.field = field;
	}
	
	/**
	 * Sets the patientIds
	 *
	 * @param patientIds the patientIds to set
	 */
	public void setPatientIds(Set<String> patientIds) {
		this.patientIds = patientIds;
	}
	
	@Factory
	public Filter getFilter() {
		List<BytesRef> byteRefs = new ArrayList(patientIds.size());
		for (String id : patientIds) {
			byteRefs.add(new BytesRef(id));
		}
		
		return new CachingWrapperFilter(new TermsFilter(field, byteRefs));
	}
	
}
