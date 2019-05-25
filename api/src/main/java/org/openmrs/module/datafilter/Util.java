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

import java.lang.management.ManagementFactory;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.weaver.loadtime.Agent;

import com.sun.tools.attach.VirtualMachine;

public class Util {
	
	private static final Log log = LogFactory.getLog(Util.class);
	
	/**
	 * Registers aspectj weaver's java agent with the current jvm instance
	 * 
	 * @throws Exception
	 */
	protected static void registerJavaAgent() throws Exception {
		String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		String pid = jvmName.substring(0, jvmName.indexOf('@'));
		VirtualMachine vm = VirtualMachine.attach(pid);
		URL aspectjWeaverJar = Agent.class.getProtectionDomain().getCodeSource().getLocation();
		vm.loadAgent(aspectjWeaverJar.getFile());
		vm.detach();
	}
	
}
