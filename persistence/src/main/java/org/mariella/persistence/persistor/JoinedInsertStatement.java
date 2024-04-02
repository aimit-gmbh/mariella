package org.mariella.persistence.persistor;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.ParameterValues;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.mapping.AbstractPersistorStatement;
import org.mariella.persistence.mapping.JoinedClassMapping;
import org.mariella.persistence.mapping.PrimaryKeyJoinColumn;

import java.util.ArrayList;
import java.util.List;

public class JoinedInsertStatement extends AbstractPersistorStatement {
    private final JoinedClassMapping classMapping;
    private final List<Column> columns;

    public JoinedInsertStatement(JoinedClassMapping classMapping, Table table, List<Column> columns) {
        super(classMapping.getSchemaMapping().getSchema(), table);
        this.classMapping = classMapping;
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
            column.setObject(parameterValues, index++, row.getProperty(column));
        }
    }

    @Override
    protected String getSqlString(BuildCallback buildCallback) {
        boolean first;

        List<Column> requiredNotSetColumns = new ArrayList<>();
        StringBuilder b = new StringBuilder();
        b.append(" INSERT INTO ").append(table.getName()).append(" (");
        first = true;
        for (Column column : columns) {
            if (first)
                first = false;
            else
                b.append(", ");
            b.append(column.name());
        }
        for (PrimaryKeyJoinColumn primaryKeyJoinColumn : classMapping.getPrimaryKeyJoinColumns().getPrimaryKeyJoinColumns()) {
            if (!columns.contains(primaryKeyJoinColumn.getJoinTableColumn())) {
                if (first)
                    first = false;
                else
                    b.append(", ");
                b.append(primaryKeyJoinColumn.getJoinTableColumn().name());
                requiredNotSetColumns.add(primaryKeyJoinColumn.getJoinTableColumn());
            }
        }
        b.append(") ");

        b.append(" VALUES (");
        first = true;
        for (Column column : columns) {
            if (first)
                first = false;
            else
                b.append(", ");
            buildCallback.columnValue(b, column);
        }

        for (Column column : requiredNotSetColumns) {
            if (first)
                first = false;
            else
                b.append(", ");
            buildCallback.columnValue(b, column);
        }

        b.append(")");

        return b.toString();
    }

}
