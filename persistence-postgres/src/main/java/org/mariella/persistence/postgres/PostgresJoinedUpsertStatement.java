package org.mariella.persistence.postgres;

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

public class PostgresJoinedUpsertStatement extends AbstractPersistorStatement {
    private final JoinedClassMapping classMapping;
    private final List<Column> columns;

    private List<Column> parameters;

    public PostgresJoinedUpsertStatement(JoinedClassMapping classMapping, List<Column> columns) {
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

        return persistor.prepareStatement(this, sql);
    }


    @Override
    protected String getSqlString(BuildCallback buildCallback) {
        boolean first = true;

        List<Column> requiredNotSetColumns = new ArrayList<>();
        StringBuilder b = new StringBuilder();
        b.append("MERGE INTO ").append(table.getQualifiedName()).append(" a using (select ");
        for (PrimaryKeyJoinColumn primaryKeyJoinColumn : classMapping.getPrimaryKeyJoinColumns().getPrimaryKeyJoinColumns()) {
            if (first)
                first = false;
            else
                b.append(", ");
            b.append(primaryKeyJoinColumn.getJoinTableColumn().name());
        }
        b.append(" from ").append(table.getQualifiedName()).append(" where ");
        first = true;
        for (PrimaryKeyJoinColumn primaryKeyJoinColumn : classMapping.getPrimaryKeyJoinColumns().getPrimaryKeyJoinColumns()) {
            if (first)
                first = false;
            else
                b.append(" AND ");
            b.append(primaryKeyJoinColumn.getJoinTableColumn().name());
            b.append("=");
            buildCallback.columnValue(b, primaryKeyJoinColumn.getJoinTableColumn());
        }
        b.append(") as b on ");
        first = true;
        for (PrimaryKeyJoinColumn primaryKeyJoinColumn : classMapping.getPrimaryKeyJoinColumns().getPrimaryKeyJoinColumns()) {
            if (first)
                first = false;
            else
                b.append(" AND ");
            b.append("a.");
            b.append(primaryKeyJoinColumn.getJoinTableColumn().name());
            b.append(" = b.");
            b.append(primaryKeyJoinColumn.getJoinTableColumn().name());
        }
        b.append(" when matched then UPDATE SET ");
        first = true;
        for (Column column : columns) {
            if (first)
                first = false;
            else
                b.append(", ");
            b.append(column.name());
            b.append("=");
            buildCallback.columnValue(b, column);
        }

        b.append(" when not matched then insert (");
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
        b.append(") VALUES (");
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
