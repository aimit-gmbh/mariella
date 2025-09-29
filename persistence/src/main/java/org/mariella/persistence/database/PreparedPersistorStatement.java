package org.mariella.persistence.database;

import org.mariella.persistence.persistor.Row;

public interface PreparedPersistorStatement {

    void addBatch(Row parameters, Object entity);

    void close();

}
