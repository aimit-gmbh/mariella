package org.mariella.persistence.mapping;

import org.mariella.persistence.database.*;
import org.mariella.persistence.persistor.Persistor;
import org.mariella.persistence.persistor.Row;

import java.util.List;

public class InsertStatement extends AbstractPersistorStatement {
    private final List<Column> columns;

    public InsertStatement(Schema schema, Table table, List<Column> columns) {
        super(schema, table);
        this.columns = columns;
    }

    @Override
    public <T extends PreparedPersistorStatement> T prepare(Persistor<T> persistor) {
        return persistor.prepareStatement(this, getSqlString());
    }

    @Override
    protected String getSqlString(BuildCallback buildCallback) {
        StringBuilder b = new StringBuilder();
        b.append("INSERT INTO ");
        b.append(table.getName());
        b.append(" (");
        boolean first = true;
        for (Column column : columns) {
            if (first)
                first = false;
            else
                b.append(", ");
            b.append(column.name());
        }
        b.append(") VALUES (");
        first = true;
        for (Column column : columns) {
            if (first)
                first = false;
            else
                b.append(", ");
            buildCallback.columnValue(b, column);
        }
        b.append(")");
        return b.toString();
    }

    @Override
    public void setParameters(ParameterValues parameterValues, Row row) {
        int index = 1;
        for (Column column : row.getSetColumns()) {
            column.setObject(parameterValues, index, row.getProperty(column));
            index++;
        }
    }
}
