package org.mariella.persistence.postgres;

import java.util.ArrayList;
import java.util.List;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Converter;
import org.mariella.persistence.database.ParameterValues;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.mapping.AbstractPersistorStatement;
import org.mariella.persistence.mapping.ColumnMapping;
import org.mariella.persistence.mapping.JoinedClassMapping;
import org.mariella.persistence.mapping.PrimaryKeyJoinColumn;
import org.mariella.persistence.mapping.PropertyMapping;
import org.mariella.persistence.persistor.ObjectPersistor;
import org.mariella.persistence.persistor.Persistor;
import org.mariella.persistence.persistor.Row;
import org.mariella.persistence.runtime.ModifiableAccessor;

public class PostgresJoinedUpsertStatement extends AbstractPersistorStatement {
	private final ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor;
    private final JoinedClassMapping classMapping;
    private final List<Column> columns;

    private List<Column> parameters;

    public PostgresJoinedUpsertStatement(ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor, JoinedClassMapping classMapping, List<Column> columns) {
        super(classMapping.getSchemaMapping().getSchema(), classMapping.getJoinUpdateTable());
        this.objectPersistor = objectPersistor;
        this.classMapping = classMapping;
        this.columns = columns;
    }

    @Override
    public void setParameters(ParameterValues parameterValues, Row row) {
        int index = 1;
        for (Column column : parameters) {
            Object value = getColumnValue(row, column);
            column.setObject(parameterValues, index++, value);
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
    public String getSqlDebugString(Row parameters) {
        return getSqlString(
            (b, column) -> {
                @SuppressWarnings("unchecked")
                Converter<Object> converter = (Converter<Object>) column.converter();
                Object value = getColumnValue(parameters, column);
                b.append(converter.toString(value));
            });
    }

    private Object getColumnValue(Row row, Column column) {
    	if(row.getSetColumns().contains(column)) {
    		return row.getProperty(column);
    	} else {
    		for(PropertyMapping pm : classMapping.getPropertyMappings()) {
    			if(pm instanceof ColumnMapping) {
    				ColumnMapping cm = (ColumnMapping)pm;
    				if(cm.getUpdateColumn() == column) {
    					return ModifiableAccessor.Singleton.getValue(objectPersistor.getModificationInfo().getObject(), cm.getPropertyDescription());
    				}
    			}
    		}
    		throw new IllegalArgumentException("No value available for column " + table.getName() + "." + column.name());
    	}
    }
    
    
    @Override
    protected String getSqlString(BuildCallback buildCallback) {
        boolean first;
        List<Column> requiredNotSetColumns = new ArrayList<>();
        List<Column> insertColumns = new ArrayList<>();
        StringBuilder b = new StringBuilder();
        b.append(" INSERT INTO ").append(classMapping.getJoinUpdateTable().getName()).append(" (");
        first = true;

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

        for (Column column : classMapping.getJoinUpdateTable().getColumns()) {
            if (columns.contains(column)) {
                if (first)
                    first = false;
                else
                    b.append(", ");
                b.append(column.name());
                insertColumns.add(column);
            } else if (!requiredNotSetColumns.contains(column) && !column.nullable()) {
                if (first)
                    first = false;
                else
                    b.append(", ");
                b.append(column.name());
                insertColumns.add(column);
            }
        }


        b.append(") ");

        b.append(" VALUES (");
        first = true;

        for (Column column : requiredNotSetColumns) {
            if (first)
                first = false;
            else
                b.append(", ");
            buildCallback.columnValue(b, column);
        }

        for (Column column : insertColumns) {
            if (first)
                first = false;
            else
                b.append(", ");
            buildCallback.columnValue(b, column);
        }


        b.append(") ON CONFLICT (");
        first = true;
        for (PrimaryKeyJoinColumn primaryKeyJoinColumn : classMapping.getPrimaryKeyJoinColumns().getPrimaryKeyJoinColumns()) {
            if (first)
                first = false;
            else
                b.append(", ");
            b.append(primaryKeyJoinColumn.getJoinTableColumn().name());
        }
        b.append(") DO UPDATE SET ");
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
        b.append(" WHERE ");
        first = true;
        for (PrimaryKeyJoinColumn primaryKeyJoinColumn : classMapping.getPrimaryKeyJoinColumns().getPrimaryKeyJoinColumns()) {
            if (first)
                first = false;
            else
                b.append(" AND ");
            b.append(classMapping.getJoinUpdateTable().getName());
            b.append(".");
            b.append(primaryKeyJoinColumn.getJoinTableColumn().name());
            b.append("=");
            buildCallback.columnValue(b, primaryKeyJoinColumn.getJoinTableColumn());
        }

        return b.toString();
    }
}
