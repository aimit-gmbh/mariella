package org.mariella.persistence.bootstrap;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceConnectionProvider implements ConnectionProvider {
    private final DataSource dataSource;
    private Connection connection;

    public DataSourceConnectionProvider(DataSource dataSource) {
        super();
        this.dataSource = dataSource;
    }

    public Connection getConnection() {
        if (connection == null) {
            try {
                connection = dataSource.getConnection();
            } catch (SQLException e) {
                throw new PersistenceException(e);
            }
        }
        return connection;

    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new PersistenceException(e);
            } finally {
                connection = null;
            }
        }
    }

}
