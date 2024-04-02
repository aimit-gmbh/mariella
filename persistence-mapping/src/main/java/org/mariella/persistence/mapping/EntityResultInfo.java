package org.mariella.persistence.mapping;


public class EntityResultInfo {

    private Class<?> entityClass;
    private FieldResultInfo[] fieldResultInfos;
    private String discriminatorColumn;

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public FieldResultInfo[] getFieldResultInfos() {
        return fieldResultInfos;
    }

    public void setFieldResultInfos(FieldResultInfo[] fieldResultInfos) {
        this.fieldResultInfos = fieldResultInfos;
    }

    public String getDiscriminatorColumn() {
        return discriminatorColumn;
    }

    public void setDiscriminatorColumn(String discriminatorColumn) {
        this.discriminatorColumn = discriminatorColumn;
    }


}
