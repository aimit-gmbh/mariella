package org.mariella.persistence.mapping;


public class SqlResultSetMappingInfo {

    private String name;
    private EntityResultInfo[] entityResultInfos;
    private ColumnResultInfo[] columnResultInfos;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityResultInfo[] getEntityResultInfos() {
        return entityResultInfos;
    }

    public void setEntityResultInfos(EntityResultInfo[] entityResultInfos) {
        this.entityResultInfos = entityResultInfos;
    }

    public ColumnResultInfo[] getColumnResultInfos() {
        return columnResultInfos;
    }

    public void setColumnResultInfos(ColumnResultInfo[] columnResultInfos) {
        this.columnResultInfos = columnResultInfos;
    }


}
