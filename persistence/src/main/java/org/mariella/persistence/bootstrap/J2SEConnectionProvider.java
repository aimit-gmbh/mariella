package org.mariella.persistence.bootstrap;

import jakarta.persistence.PersistenceException;
import org.mariella.persistence.mapping.UnitInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * @author aim
 */
public class J2SEConnectionProvider implements ConnectionProvider {
    private final String connectString;
    private Connection connection = null;
    private String dbUser = null;
    private String dbPassword = null;

    public J2SEConnectionProvider(UnitInfo unitInfo) {
        super();
        // TODO check for missing configuration
        this.connectString = unitInfo.getProperties().getProperty(CONNECT_PROPERTY_NAME);
        this.dbUser = unitInfo.getProperties().getProperty(DBUSER_PROPERTY_NAME);
        this.dbPassword = unitInfo.getProperties().getProperty(DBPASSWORD_PROPERTY_NAME);
    }

    public J2SEConnectionProvider(String connectString) {
        super();
        this.connectString = connectString;
    }

    public J2SEConnectionProvider(String connectString, String dbUser, String dbPassword) {
        super();
        this.connectString = connectString;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new PersistenceException(e);
            }
        }
    }

    public Connection getConnection() {
        if (connection == null) {
            if (connectString == null) {
                throw new IllegalStateException(
                        "No connect string specified. Please specify with the " + CONNECT_PROPERTY_NAME + " property in your " +
                                "persistence.xml");
            }
            try {
                if (dbUser == null) {
                    connection = DriverManager.getConnection(connectString);
                } else {
                    connection = DriverManager.getConnection(connectString, dbUser, dbPassword);
                }
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                throw new PersistenceException(e);
            }
        }
        return connection;
    }

}
