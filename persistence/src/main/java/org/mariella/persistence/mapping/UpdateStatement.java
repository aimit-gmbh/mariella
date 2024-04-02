package org.mariella.persistence.mapping;

import org.mariella.persistence.database.*;
import org.mariella.persistence.persistor.Persistor;
import org.mariella.persistence.persistor.Row;

import java.util.List;

public class UpdateStatement extends AbstractPersistorStatement {

    private final List<Column> columns;

    public UpdateStatement(Schema schema, Table table, List<Column> columns) {
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
        for (Column column : row.getSetColumns()) {
            if (!row.getTable().getPrimaryKey().contains(column)) {
                column.setObject(parameterValues, index, row.getProperty(column));
                index++;
            }
        }
        for (Column pk : row.getTable().getPrimaryKey()) {
            pk.setObject(parameterValues, index, row.getProperty(pk));
            index++;
        }
    }


    @Override
    public String getSqlString(BuildCallback buildCallback) {
        StringBuilder b = new StringBuilder();
        b.append("UPDATE ");
        b.append(table.getName());
        b.append(" SET ");
        boolean first = true;
        for (Column column : columns) {
            if (!table.getPrimaryKey().contains(column)) {
                if (first)
                    first = false;
                else
                    b.append(", ");
                b.append(column.name());
                b.append(" = ");
                buildCallback.columnValue(b, column);
            }
        }
        b.append(" WHERE ");

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

        return b.toString();
    }
}
