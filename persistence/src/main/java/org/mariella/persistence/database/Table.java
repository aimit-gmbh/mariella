package org.mariella.persistence.database;

import java.util.*;

public class Table {
    private final String name;
    private final String schema;
    private final String catalog;
    private final Map<String, Column> columns = new HashMap<>();
    private final List<Column> primaryKey = new ArrayList<>();
    
    private boolean hasMandatoryNonPrimaryKeyColumns = false;

    public Table(String catalog, String schema, String name) {
        super();
        this.name = name;
        this.schema = schema;
        this.catalog = catalog;
    }

    public String getName() {
        return name;
    }

    public String getQualifiedName() {
    	if(schema != null) {
    		return schema + "." + name;
    	} else {
    		return name;
    	}
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
        if(!column.nullable() && !primaryKey.contains(column)) {
        	hasMandatoryNonPrimaryKeyColumns = true;
        }
    }

    public void addPrimaryKeyColumn(Column column) {
        primaryKey.add(column);
        addColumn(column);
    }

    public Column getColumn(String name) {
        return columns.get(name);
    }

    public List<Column> getPrimaryKey() {
        return primaryKey;
    }

    public boolean hasMandatoryNonPrimaryKeyColumns() {
    	return hasMandatoryNonPrimaryKeyColumns;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
