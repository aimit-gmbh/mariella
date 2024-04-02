package org.mariella.persistence.database;

import java.util.*;

public class Table {
    private final String name;
    private final String schema;
    private final String catalog;
    private final Map<String, Column> columns = new HashMap<>();
    private final List<Column> primaryKey = new ArrayList<>();

    public Table(String catalog, String schema, String name) {
        super();
        this.name = name;
        this.schema = schema;
        this.catalog = catalog;
    }

    public String getName() {
        return name;
    }

    public String getSchema() {
        return schema;
    }

    public String getCatalog() {
        return catalog;
    }

    public Collection<Column> getColumns() {
        return columns.values();
    }

    public void addColumn(Column column) {
        columns.put(column.name(), column);
    }

    public void addPrimaryKeyColumn(Column column) {
        addColumn(column);
        primaryKey.add(column);
    }

    public Column getColumn(String name) {
        return columns.get(name);
    }

    public List<Column> getPrimaryKey() {
        return primaryKey;
    }

    @Override
    public String toString() {
        return name;
    }
}
