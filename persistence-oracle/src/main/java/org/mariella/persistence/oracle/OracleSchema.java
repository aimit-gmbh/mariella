package org.mariella.persistence.oracle;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Schema;
import org.mariella.persistence.mapping.JoinedClassMapping;
import org.mariella.persistence.mapping.PersistorStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class OracleSchema extends Schema {
    @Override
    public PersistorStatement createJoinedUpsertStatement(JoinedClassMapping joinedClassMapping, List<Column> columns) {
        return new OracleJoinedUpsertStatement(joinedClassMapping, columns);
    }

    @Override
    public void addBatch(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.execute(null);
    }
}
