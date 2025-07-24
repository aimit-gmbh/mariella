package org.mariella.persistence.persistor;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Converter;
import org.mariella.persistence.database.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Row {
    private final Table table;
    private final Map<Column, Object> valueMap = new HashMap<>();
    private final List<Column> setColumns = new ArrayList<>();

    public Row(Table table) {
        super();
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    public List<Column> getSetColumns() {
        return setColumns;
    }

    public Object getProperty(Column column) {
        if (!setColumns.contains(column)) {
            throw new IllegalArgumentException("No value available for column");
        }
        return valueMap.get(column);
    }

    public void setProperty(Column column, Object value) {
        valueMap.put(column, value);
        if (!setColumns.contains(column)) {
            setColumns.add(column);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(table.getQualifiedName());
        b.append("(");
        boolean first = true;
        for (Column column : setColumns) {
            if (first) {
                first = false;
            } else {
                b.append(", ");
            }
            b.append(column.name()).append(":")
                    .append(((Converter<Object>) column.converter()).toString(valueMap.get(column)));
        }
        b.append(")");
        return b.toString();
    }

}
