package org.mariella.persistence.persistor;

import org.mariella.persistence.database.ConnectionCallback;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionDatabaseAccess extends DatabaseAccess {
    private final Connection connection;

    public ConnectionDatabaseAccess(Connection connection) {
        super();
        this.connection = connection;
    }

    @Override
    public Object doInConnection(ConnectionCallback callback) throws SQLException {
        return callback.doInConnection(connection);
    }

}
