package org.mariella.persistence.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionCallback {
    Object doInConnection(Connection connection) throws SQLException;
}
