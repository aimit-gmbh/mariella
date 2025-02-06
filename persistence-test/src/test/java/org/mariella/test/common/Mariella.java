package org.mariella.test.common;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import javax.naming.ConfigurationException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;
import org.flywaydb.core.api.output.MigrateResult;
import org.mariella.persistence.bootstrap.DataSourceConnectionProvider;
import org.mariella.persistence.bootstrap.StandaloneEnvironment;
import org.mariella.persistence.mapping.SchemaMapping;
import org.mariella.persistence.runtime.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mariella {
	private Logger logger = LoggerFactory.getLogger(Mariella.class.getName());

	public static interface ConnectionCallback<T> {
		public T doInConnection(Connection connection) throws SQLException;
	}
	private final String flywayLocation;
	private final String persistenceUnit;
	private final BasicDataSource dataSource;

	private StandaloneEnvironment mariellaEnvironment;
	
public Mariella(String persistenceUnit, String flywayLocation, BasicDataSource dataSource) {
	this.flywayLocation = flywayLocation;
	this.persistenceUnit = persistenceUnit;
	this.dataSource = dataSource;
}

public void destroy() {
	try {
		dataSource.close();
	} catch(SQLException e) {
		logger.atError()
			.setCause(e)
			.log("error closing connection pool");
	}
}

public MigrateResult migrateDatabase() throws ConfigurationException {
	return Flyway.configure()
			.loggers("slf4j")
			.dataSource(dataSource)
			.validateMigrationNaming(false)
			.locations(new Location(flywayLocation)).load().migrate();
}

public void setupPersistence() {
	mariellaEnvironment = new StandaloneEnvironment();
	mariellaEnvironment.setConnectionProvider(new DataSourceConnectionProvider(dataSource));
	mariellaEnvironment.createUnitInfo(persistenceUnit, new HashMap<>());
	mariellaEnvironment.createSchemaMapping();
}

public SchemaMapping getSchemaMapping() {
	return mariellaEnvironment.getSchemaMapping();
}

public <T> T doInConnection(ConnectionCallback<T> callback) {
	try {
		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(false);
		connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		try {
			T result = callback.doInConnection(connection);
			connection.commit();
			return result;
		} catch (PersistenceException e1) {
			connection.rollback();
			throw e1;
		} catch (Exception e) {
			connection.rollback();
			throw new PersistenceException("Unexpected Error", e);
		} finally {
			connection.close();
		}
	} catch (SQLException e) {
		throw new PersistenceException("Unexpected Error", e);
	}
}

}
