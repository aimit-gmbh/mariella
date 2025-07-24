package org.mariella.persistence.mapping;

import org.mariella.persistence.database.*;
import org.mariella.persistence.persistor.Persistor;
import org.mariella.persistence.persistor.Row;

import java.util.List;

public class DeleteStatement extends AbstractPersistorStatement {
    private final List<Column> columns;

    public DeleteStatement(Schema schema, Table table, List<Column> columns) {
        super(schema, table);
        this.columns = columns;
    }

    @Override
    public <T extends PreparedPersistorStatement> T prepare(Persistor<T> persistor) {
        return persistor.prepareStatement(this, getSqlString());
    }

    @Override
    public void setParameters(ParameterValues parameterValues, Row row) {
        int index = 1;
        for (Column column : columns) {
            column.setObject(parameterValues, index, row.getProperty(column));
            index++;
        }
    }

    @Override
    protected String getSqlString(BuildCallback buildCallback) {
        StringBuilder b = new StringBuilder();
        b.append("DELETE FROM ");
        b.append(table.getQualifiedName());
        b.append(" WHERE ");

        boolean first = true;
        for (Column column : columns) {
            if (first)
                first = false;
            else
                b.append(" AND ");
            b.append(column.name());
            b.append(" = ");
            buildCallback.columnValue(b, column);
        }
        return b.toString();
    }
}
