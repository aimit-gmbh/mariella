package org.mariella.persistence.mapping;

import java.util.ArrayList;
import java.util.List;

// TODO finally check if each EntityInfo has an id attribute
public class EntityInfo extends MappedClassInfo {

    private Class<?> compositeIdClass;
    private TableInfo tableInfo;
    private UpdateTableInfo updateTableInfo;
    private DiscriminatorColumnInfo discriminatorColumnInfo;
    private DiscriminatorValueInfo discriminatorValueInfo;
    private List<PrimaryKeyJoinColumnInfo> primaryKeyJoinColumnInfos = new ArrayList<>();

    public EntityInfo getSuperEntityInfo() {
        MappedClassInfo superInfo = getSuperclassInfo();
        while (superInfo != null) {
            if (superInfo instanceof EntityInfo)
                return (EntityInfo) superInfo;
            superInfo = superInfo.getSuperclassInfo();
        }
        return null;
    }

    public Class<?> getCompositeIdClass() {
        return compositeIdClass;
    }

    public void setCompositeIdClass(Class<?> compositeIdClass) {
        this.compositeIdClass = compositeIdClass;
    }

    public List<BasicAttributeInfo> getBasicAttributeInfos() {
        List<BasicAttributeInfo> result = new ArrayList<>();
        for (AttributeInfo ai : getAttributeInfos()) {
            if (ai instanceof BasicAttributeInfo)
                result.add((BasicAttributeInfo) ai);
        }
        return result;
    }

    public BasicAttributeInfo getAttributeInfoHavingColName(String referencedColumnName) {
        for (BasicAttributeInfo ai : getBasicAttributeInfos()) {
            if (ai != null) {
                if (ai.getColumnInfo() != null && ai.getColumnInfo().getName()
                        .equals(referencedColumnName))
                    return ai;
            }
        }
        if (getSuperEntityInfo() != null)
            return getSuperEntityInfo().getAttributeInfoHavingColName(referencedColumnName);
        return null;
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
    }

    public UpdateTableInfo getUpdateTableInfo() {
        return updateTableInfo;
    }

    public void setUpdateTableInfo(UpdateTableInfo updateTableInfo) {
        this.updateTableInfo = updateTableInfo;
    }

    public DiscriminatorColumnInfo getDiscriminatorColumnInfo() {
        return discriminatorColumnInfo;
    }

    public void setDiscriminatorColumnInfo(DiscriminatorColumnInfo discriminatorColumnInfo) {
        this.discriminatorColumnInfo = discriminatorColumnInfo;
    }

    public DiscriminatorValueInfo getDiscriminatorValueInfo() {
        return discriminatorValueInfo;
    }

    public void setDiscriminatorValueInfo(
            DiscriminatorValueInfo discriminatorValueInfo) {
        this.discriminatorValueInfo = discriminatorValueInfo;
    }

    public List<PrimaryKeyJoinColumnInfo> getPrimaryKeyJoinColumnInfos() {
        return primaryKeyJoinColumnInfos;
    }

    public void setPrimaryKeyJoinColumnInfos(List<PrimaryKeyJoinColumnInfo> primaryKeyJoinColumnInfos) {
        this.primaryKeyJoinColumnInfos = primaryKeyJoinColumnInfos;
    }

}
