package org.mariella.persistence.oracle;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.ParameterValues;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.mapping.AbstractPersistorStatement;
import org.mariella.persistence.mapping.JoinedClassMapping;
import org.mariella.persistence.persistor.Persistor;
import org.mariella.persistence.persistor.Row;

import java.util.ArrayList;
import java.util.List;

public class OracleJoinedUpsertStatement extends AbstractPersistorStatement {

    private final List<Column> columns;

    List<Column> parameters;

    public OracleJoinedUpsertStatement(JoinedClassMapping classMapping, List<Column> columns) {
        super(classMapping.getSchemaMapping().getSchema(), classMapping.getJoinUpdateTable());
        this.columns = columns;
    }

    @Override
    public void setParameters(ParameterValues parameterValues, Row row) {
        int index = 1;
        for (Column column : parameters) {
            column.setObject(parameterValues, index++, row.getProperty(column));
        }
    }

    @Override
    public <T extends PreparedPersistorStatement> T prepare(Persistor<T> persistor) {
        nextParameterIndex = 1;
        parameters = new ArrayList<>();
        String sql = getSqlString(
                (b, column) -> {
                    createParameter().print(b);
                    parameters.add(column);
                });

        return persistor.prepareStatement(this, sql);
    }

    @Override
    protected String getSqlString(BuildCallback buildCallback) {
        boolean first;
        StringBuilder b = new StringBuilder();
        b.append("MERGE INTO ").append(table.getName());
        b.append("\n\tUSING DUAL ON (");

        first = true;
        for (Column pk : table.getPrimaryKey()) {
            if (first)
                first = false;
            else
                b.append(" AND ");
            b.append(pk.name());
            b.append(" = ");
            buildCallback.columnValue(b, pk);
        }
        b.append(")");
        b.append("\nWHEN NOT MATCHED THEN");
        b.append("\n\tINSERT (");

        first = true;
        for (Column column : columns) {
            if (first) {
                first = false;
            } else {
                b.append(", ");
            }
            b.append(column.name());
        }
        b.append(") VALUES (");
        first = true;
        for (Column column : columns) {
            if (first) {
                first = false;
            } else {
                b.append(", ");
            }
            buildCallback.columnValue(b, column);
        }
        b.append(")");
        List<Column> columnsToUpdate = new ArrayList<>();
        for (Column column : columns) {
            if (!table.getPrimaryKey().contains(column)) {
                columnsToUpdate.add(column);
            }
        }
        if (!columnsToUpdate.isEmpty()) {
            b.append("\nWHEN MATCHED THEN");
            b.append("\n\tUPDATE SET ");
            first = true;
            for (Column column : columnsToUpdate) {
                if (first) {
                    first = false;
                } else {
                    b.append(", ");
                }
                b.append(column.name());
                b.append(" = ");
                buildCallback.columnValue(b, column);
            }
        }
        return b.toString();
    }
}
