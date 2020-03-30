/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.datafilter.impl.api.db.hibernate;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.type.Type;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.datafilter.impl.EntityBasisMap;
import org.openmrs.module.datafilter.impl.ImplConstants;
import org.openmrs.module.datafilter.impl.api.db.DataFilterDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This interceptor links every newly created patient to the current user's session location This is
 * a very basic implementation that makes the assumptions below:
 * 
 * <pre>
 * <ul>
 * <li>No nested transactions</li>
 * <li>Exactly one patient is created in a session</li>
 * <li>There is a session location set on the user context</li>
 * <ul/>
 * 
 * <pre/>
 */
@Component("patientLocationLinkingInterceptor")
public class PatientLocationLinkingInterceptor extends EmptyInterceptor {
	
	private static final Logger log = LoggerFactory.getLogger(PatientLocationLinkingInterceptor.class);
	
	private static ThreadLocal<SessionData> sessionDataHolder = new ThreadLocal();
	
	/**
	 * Should link the patient to the current user's session location
	 *
	 * @see EmptyInterceptor#onSave(Object, Serializable, Object[], String[], Type[])
	 */
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		//TODO: The below line is needed to show the type of entities for which this method has been called.
		// Should be removed once the patient creation with existing person is removed.
		System.out.println("Fired onSave for "+entity.getClass());
		if (!(entity instanceof Patient)) {
			return super.onSave(entity, id, state, propertyNames, types);
		}
		
		//Hibernate will flush any changes in the current session before querying the DB when fetching
		//the GP value below and we end up in this method again, therefore we need to disable auto flush
		if (!"true".equalsIgnoreCase(InterceptorUtil.getGpValueNoFlush(ImplConstants.GP_PAT_LOC_INTERCEPTOR_ENABLED))) {
			
			if (log.isTraceEnabled()) {
				log.trace("Skipping PatientLocationLinkingInterceptor because is it disabled");
			}
			
			return super.onSave(entity, id, state, propertyNames, types);
		}
		
		SessionData sessionData = new SessionData();
		sessionData.patient = (Patient) entity;
		sessionDataHolder.set(sessionData);
		
		//See SessionLocationDetector class to understand why we have the lines below otherwise if we detect 
		//the location from beforeTransactionCompletion method and throw an exception, hibernate will swallow it
		//whereas it bubbles out if we throw it from a BeforeTransactionCompletionProcess instance.
		SessionFactory sessionFactory = Context.getRegisteredComponents(SessionFactory.class).get(0);
		EventSource eventSource = (EventSource) sessionFactory.getCurrentSession();
		eventSource.getActionQueue().registerProcess(new SessionLocationDetector());
		
		return super.onSave(entity, id, state, propertyNames, types);
	}
	
	/**
	 * @see EmptyInterceptor#beforeTransactionCompletion(Transaction)
	 */
	@Override
	public void beforeTransactionCompletion(Transaction tx) {
		SessionData sessionData = sessionDataHolder.get();
		if (sessionData == null) {
			//No new patients created in this transaction
			super.beforeTransactionCompletion(tx);
			return;
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Linking new Patient with id: " + sessionData.locationId + " to location with id: "
			        + sessionData.locationId);
		}
		
		EntityBasisMap map = new EntityBasisMap();
		map.setEntityIdentifier(sessionData.patient.getId().toString());
		map.setEntityType(Patient.class.getName());
		map.setBasisIdentifier(sessionData.locationId.toString());
		map.setBasisType(Location.class.getName());
		
		Context.getRegisteredComponents(DataFilterDAO.class).get(0).saveEntityBasisMap(map);
	}
	
	@Override
	public void afterTransactionCompletion(Transaction tx) {
		if (sessionDataHolder.get() != null) {
			sessionDataHolder.remove();
		}
	}
	
	/**
	 * An instance of this class is used to check if there is a location set on the current user
	 * context, if none exists it throws an exception otherwise sets it in the {@link SessionData}
	 * instance for the current session.
	 */
	private class SessionLocationDetector implements BeforeTransactionCompletionProcess {
		
		@Override
		public void doBeforeTransactionCompletion(SessionImplementor session) {
			if (sessionDataHolder.get() != null) {
				Integer locationId = null;
				if (Context.isAuthenticated()) {
					locationId = Context.getUserContext().getLocationId();
					if (locationId == null && Context.getUserContext().getLocation() != null) {
						locationId = Context.getUserContext().getLocation().getId();
					}
				}
				
				if (locationId == null) {
					sessionDataHolder.remove();
					throw new DAOException(
					        "Failed to link a new patient to any location, no session location found on the user context");
				}
				
				sessionDataHolder.get().locationId = locationId;
			}
		}
		
	}
	
	private class SessionData {
		
		private Patient patient;
		
		private Integer locationId;
		
	}
	
}
