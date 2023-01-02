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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dbunit.DatabaseUnitException;
import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.MySQL5Dialect;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.liquibase.ChangeLogVersionFinder;
import org.openmrs.liquibase.ChangeSetExecutorCallback;
import org.openmrs.module.datafilter.TestConstants;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.TestUtil;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.PrivilegeConstants;
import org.testcontainers.containers.MySQLContainer;
import liquibase.changelog.ChangeSet;

public abstract class BaseFilterMySQLBackedTest extends BaseModuleContextSensitiveTest {
	
	private static final Logger log = LoggerFactory.getLogger(BaseFilterMySQLBackedTest.class);
	
	private static MySQLContainer<?> mysqlContainer = new MySQLContainer("mysql:5.7.31");
	
	private static String databaseUrl = "jdbc:mysql://localhost:DATABASE_PORT/openmrs?autoReconnect=true&sessionVariables=default_storage_engine%3DInnoDB&useUnicode=true&characterEncoding=UTF-8";
	
	private static String databaseUsername = "user";
	
	private static String databaseUserPasswword = "12345678";
	
	private static String databaseDialect = MySQL5Dialect.class.getName();
	
	private static String databaseDriver = "com.mysql.jdbc.Driver";
	
	@BeforeClass
	public static void setupMySqlDb() throws IOException {
		
		mysqlContainer.withDatabaseName("openmrs");
		mysqlContainer.withUsername(databaseUsername);
		mysqlContainer.withPassword(databaseUserPasswword);
		mysqlContainer.start();
		System.setProperty("useInMemoryDatabase", "false");
	}
	
	@Override
	public void baseSetupWithStandardDataAndAuthentication() throws SQLException {
		// Open a session if needed
		if (!Context.isSessionOpen()) {
			Context.openSession();
		}
		
		ChangeLogVersionFinder changeLogVersionFinder = new ChangeLogVersionFinder();
		
		String liquibaseSchemaFileName = changeLogVersionFinder.getLatestSchemaSnapshotFilename().get();
		//		String liquibaseCoreDataFileName = changeLogVersionFinder.getLatestCoreDataSnapshotFilename().get();
		
		try {
			DatabaseUpdater.executeChangelog(liquibaseSchemaFileName,
			    new PrintingChangeSetExecutorCallback("OpenMRS schema file"));
			String datafilterLiquibase = getClass().getClassLoader().getResource("liquibase.xml").toURI().getPath();
			DatabaseUpdater.executeChangelog("liquibase.xml",
			    new PrintingChangeSetExecutorCallback("Datafilter schema file"));
		}
		catch (Exception ex) {
			throw new RuntimeException("Could not update database.", ex);
		}
		executeDataSet(INITIAL_XML_DATASET_PACKAGE_PATH);
		
		//Commit so that it is not rolled back after a test.
		getConnection().commit();
		
		authenticate();
		Context.clearSession();
	}
	
	@Override
	public Properties getRuntimeProperties() {
		if (runtimeProperties == null) {
			runtimeProperties = TestUtil.getRuntimeProperties(getWebappName());
		}
		runtimeProperties.setProperty(Environment.URL,
		    databaseUrl.replaceAll("DATABASE_PORT", String.valueOf(mysqlContainer.getMappedPort(3306))));
		
		runtimeProperties.setProperty(Environment.USER, "user");
		runtimeProperties.setProperty(Environment.PASS, "12345678");
		runtimeProperties.setProperty("connection.username", "user");
		runtimeProperties.setProperty("connection.password", "12345678");
		runtimeProperties.setProperty("connection.url",
		    databaseUrl.replaceAll("DATABASE_PORT", String.valueOf(mysqlContainer.getMappedPort(3306))));
		runtimeProperties.setProperty(Environment.HBM2DDL_AUTO, "none");
		runtimeProperties.setProperty(Environment.DIALECT, databaseDialect);
		runtimeProperties.setProperty(Environment.DRIVER, databaseDialect);
		
		return runtimeProperties;
	}
	
	@Override
	public void executeDataSet(String dataset) {
		try {
			turnOffDBConstraints(getConnection());
			super.executeDataSet(dataset);
			turnOnDBConstraints(getConnection());
		}
		catch (SQLException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	@Override
	public IDatabaseConnection setupDatabaseConnection(Connection connection) throws DatabaseUnitException {
		IDatabaseConnection dbUnitConn = new DatabaseConnection(connection);
		
		DatabaseConfig config = dbUnitConn.getConfig();
		config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());
		
		return dbUnitConn;
	}
	
	@Before
	public void beforeTestMethod() throws SQLException {
		executeDataSet(TestConstants.MODULE_TEST_DATASET_XML);
	}
	
	protected void reloginAs(String username, String password) {
		Context.logout();
		Context.authenticate(new UsernamePasswordCredentials(username, password));
	}
	
	@Override
	public void updateSearchIndex() {
		//Disable the interceptor so we can update the search index
		String originalValue = null;
		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		AdministrationService as = Context.getAdministrationService();
		GlobalProperty gp = as.getGlobalPropertyObject(ImplConstants.GP_RUN_IN_STRICT_MODE);
		if (gp == null) {
			gp = new GlobalProperty(ImplConstants.GP_RUN_IN_STRICT_MODE);
		} else {
			originalValue = gp.getPropertyValue();
		}
		
		gp.setPropertyValue("false");
		as.saveGlobalProperty(gp);
		Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		Context.flushSession();
		
		try {
			super.updateSearchIndex();
		}
		finally {
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
			gp.setPropertyValue(originalValue == null ? "" : originalValue);
			as.saveGlobalProperty(gp);
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
			Context.flushSession();
		}
	}
	
	private static class PrintingChangeSetExecutorCallback implements ChangeSetExecutorCallback {
		
		private int i = 1;
		
		private String message;
		
		public PrintingChangeSetExecutorCallback(String message) {
			this.message = message;
		}
		
		/**
		 * @see ChangeSetExecutorCallback#executing(liquibase.changelog.ChangeSet, int)
		 */
		@Override
		public void executing(ChangeSet changeSet, int numChangeSetsToRun) {
			log.info(message + " (" + i++ + "/" + numChangeSetsToRun + "): Author: " + changeSet.getAuthor() + " Comments: "
			        + changeSet.getComments() + " Description: " + changeSet.getDescription());
		}
	}
	
}
