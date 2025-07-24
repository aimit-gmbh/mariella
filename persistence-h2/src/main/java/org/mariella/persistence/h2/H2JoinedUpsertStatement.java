package org.mariella.persistence.h2;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.ParameterValues;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.mapping.AbstractPersistorStatement;
import org.mariella.persistence.mapping.JoinedClassMapping;
import org.mariella.persistence.mapping.PrimaryKeyJoinColumn;
import org.mariella.persistence.persistor.Persistor;
import org.mariella.persistence.persistor.Row;

import java.util.ArrayList;
import java.util.List;

public class H2JoinedUpsertStatement extends AbstractPersistorStatement {
    private final JoinedClassMapping classMapping;
    private final List<Column> columns;
    private List<Column> parameters;

    public H2JoinedUpsertStatement(JoinedClassMapping classMapping, List<Column> columns) {
        super(classMapping.getSchemaMapping().getSchema(), classMapping.getJoinUpdateTable());
        this.classMapping = classMapping;
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

        int index = 1;
        return persistor.prepareStatement(this, getSqlString());
    }


    @Override
    protected String getSqlString(BuildCallback buildCallback) {
        boolean first;

        List<Column> requiredNotSetColumns = new ArrayList<>();
        StringBuilder key = new StringBuilder();
        key.append(" KEY (");
        first = true;
        for (PrimaryKeyJoinColumn primaryKeyJoinColumn : classMapping.getPrimaryKeyJoinColumns().getPrimaryKeyJoinColumns()) {
            if (first)
                first = false;
            else
                key.append(", ");
            key.append(primaryKeyJoinColumn.getJoinTableColumn().name());
            if (!columns.contains(primaryKeyJoinColumn.getJoinTableColumn())) {
                requiredNotSetColumns.add(primaryKeyJoinColumn.getJoinTableColumn());
            }
        }
        key.append(")");

        StringBuilder b = new StringBuilder();
        b.append("MERGE INTO ").append(table.getQualifiedName());
        b.append(" (");
        first = true;
        for (Column column : columns) {
            if (first)
                first = false;
            else
                b.append(", ");
            b.append(column.name());
        }
        for (Column column : requiredNotSetColumns) {
            if (first)
                first = false;
            else
                b.append(", ");
            b.append(column.name());
        }
        b.append(") ");

        b.append(key);

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
