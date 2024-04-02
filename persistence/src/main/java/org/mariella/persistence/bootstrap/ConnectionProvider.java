package org.mariella.persistence.bootstrap;

import java.sql.Connection;

/**
 * @author aim
 */
public interface ConnectionProvider {
    String JDBC_DRIVER_PROPERTY_NAME = "org.mariella.persistence.jdbcdriver";
    String CONNECT_PROPERTY_NAME = "org.mariella.persistence.connectstring";
    String DBUSER_PROPERTY_NAME = "org.mariella.persistence.dbuser";
    String DBPASSWORD_PROPERTY_NAME = "org.mariella.persistence.dbpassword";

    Connection getConnection();

    void close();
}
