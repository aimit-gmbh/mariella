package org.mariella.persistence.mapping;


public class TableGeneratorInfo {

    private int allocationSize;
    private String catalog;
    private int initialValue;
    private String name;
    private String pkColumnName;
    private String pkColumnValue;
    private String schema;
    private String table;
    private UniqueConstraintInfo[] uniqueConstraintInfos;

    public int getAllocationSize() {
        return allocationSize;
    }

    public void setAllocationSize(int allocationSize) {
        this.allocationSize = allocationSize;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public int getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(int initialValue) {
        this.initialValue = initialValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPkColumnName() {
        return pkColumnName;
    }

    public void setPkColumnName(String pkColumnName) {
        this.pkColumnName = pkColumnName;
    }

    public String getPkColumnValue() {
        return pkColumnValue;
    }

    public void setPkColumnValue(String pkColumnValue) {
        this.pkColumnValue = pkColumnValue;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public UniqueConstraintInfo[] getUniqueConstraintInfos() {
        return uniqueConstraintInfos;
    }

    public void setUniqueConstraintInfos(
            UniqueConstraintInfo[] uniqueConstraintInfos) {
        this.uniqueConstraintInfos = uniqueConstraintInfos;
    }


}
