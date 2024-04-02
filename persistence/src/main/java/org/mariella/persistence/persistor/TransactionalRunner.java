package org.mariella.persistence.persistor;

import java.sql.Connection;

public interface TransactionalRunner {
    void run(Connection connection);
}
