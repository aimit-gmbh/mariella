package org.mariella.persistence.mapping_builder;

import java.io.Serializable;
import java.util.*;

public class DatabaseTableInfo implements Serializable {
    private final Map<String, DatabaseColumnInfo> columnInfos = new HashMap<>();
    private final List<DatabaseColumnInfo> primaryKey = new ArrayList<>();
    private String name;
    private String catalog;
    private String schema;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void addColumnInfo(DatabaseColumnInfo columnInfo) {
        columnInfos.put(columnInfo.getName(), columnInfo);
    }

    public Collection<DatabaseColumnInfo> getColumnInfos() {
        return columnInfos.values();
    }

    public DatabaseColumnInfo getColumnInfo(String columnName) {
        return columnInfos.get(columnName);
    }

    public List<DatabaseColumnInfo> getPrimaryKey() {
        return primaryKey;
    }

}
