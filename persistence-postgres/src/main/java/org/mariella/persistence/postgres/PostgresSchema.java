package org.mariella.persistence.postgres;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.database.Schema;
import org.mariella.persistence.mapping.JoinedClassMapping;
import org.mariella.persistence.mapping.PersistorStatement;
import org.mariella.persistence.persistor.ObjectPersistor;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PostgresSchema extends Schema {
    @Override
    public PersistorStatement createJoinedUpsertStatement(ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor, JoinedClassMapping joinedClassMapping, List<Column> columns) {
        return new PostgresJoinedUpsertStatement(objectPersistor, joinedClassMapping, columns);
    }

    @Override
    public void addBatch(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.execute();
    }
}
