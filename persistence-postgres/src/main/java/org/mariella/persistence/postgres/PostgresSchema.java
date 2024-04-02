package org.mariella.persistence.postgres;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Schema;
import org.mariella.persistence.mapping.JoinedClassMapping;
import org.mariella.persistence.mapping.PersistorStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PostgresSchema extends Schema {
    @Override
    public PersistorStatement createJoinedUpsertStatement(JoinedClassMapping joinedClassMapping, List<Column> columns) {
        return new PostgresJoinedUpsertStatement(joinedClassMapping, columns);
    }

    @Override
    public void addBatch(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.execute();
    }
}
