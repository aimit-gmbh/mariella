package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.mapping.*;
import org.mariella.persistence.runtime.Introspector;
import org.mariella.persistence.schema.ReferencePropertyDescription;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

public abstract class ToOneAttributeMappingBuilder<T extends ToOneAttributeInfo> extends RelationAttributeMappingBuilder<T> {

    public ToOneAttributeMappingBuilder(EntityMappingBuilder entityMappingBuilder, T attributeInfo) {
        super(entityMappingBuilder, attributeInfo);
    }

    @Override
    public ReferencePropertyDescription getPropertyDescription() {
        return (ReferencePropertyDescription) super.getPropertyDescription();
    }

    @Override
    public boolean buildDescription() {
        PropertyDescriptor propertyDescriptor = Introspector.Singleton.getBeanInfo(getEntityInfo().getClazz())
                .getPropertyDescriptor(attributeInfo.getName());

        if (attributeInfo.getReverseAttributeInfo() != null) {
            propertyDescription = new ReferencePropertyDescription(getClassDescription(), propertyDescriptor,
                    attributeInfo.getReverseAttributeInfo().getName());
        } else {
            propertyDescription = new ReferencePropertyDescription(getClassDescription(), propertyDescriptor);
        }
        getEntityMappingBuilder().getClassDescription().addPropertyDescription(propertyDescription);
        return true;
    }

    @Override
    public void buildMapping() {
        super.buildMapping();

        List<JoinColumnInfo> joinColumnInfos = attributeInfo.getJoinColumnInfos();
        ReferencePropertyMapping rpm = new ReferencePropertyMapping(getClassMapping(), propertyDescription);
        if (attributeInfo.getJoinColumnInfos() != null && attributeInfo.getJoinColumnInfos().size() > 0) {
            List<JoinColumn> joinColumns = new ArrayList<>();
            for (JoinColumnInfo jci : joinColumnInfos) {
                if (jci.getReferencedColumnName() == null || jci.getReferencedColumnName().length() == 0) {
                    throw new IllegalStateException(
                            "No referenced column name specified for relationship " + getClassDescription().getClassName() +
                                    "." + attributeInfo.getName() + "!");
                }
                EntityMappingBuilder referencedEntityMappingBuilder = getEntityMappingBuilder().getPersistenceBuilder()
                        .getEntityMappingBuilder(referencedClassMapping);
                TableInfo referencedTableInfo = referencedEntityMappingBuilder.getTableInfoForForeignKeyColumn(
                        jci.getReferencedColumnName());
                TableInfo referencedUpdateTableInfo = referencedEntityMappingBuilder.getUpdateTableInfoForForeignKeyColumn(
                        jci.getReferencedColumnName());
                Table referencedTable = getEntityMappingBuilder().getPersistenceBuilder().getTable(referencedTableInfo);
                Table referencedUpdateTable = getEntityMappingBuilder().getPersistenceBuilder()
                        .getTable(referencedUpdateTableInfo);

                Column keyReadColumn = referencedTable.getColumn(jci.getReferencedColumnName());
                if (keyReadColumn == null) {
                    throw new IllegalStateException(
                            "Join column " + referencedTable + "." + jci.getReferencedColumnName() + " does not exist!");
                }

                Column keyUpdateColumn = referencedUpdateTable.getColumn(jci.getReferencedColumnName());
                if (keyUpdateColumn == null) {
                    throw new IllegalStateException(
                            "Join column " + referencedUpdateTable + "." + jci.getReferencedColumnName() + " does not exist!");
                }

                Table myTable = getEntityMappingBuilder().getPersistenceBuilder()
                        .getTable(getEntityMappingBuilder().getTableInfo(attributeInfo));
                Table myUpdateTable = getEntityMappingBuilder().getPersistenceBuilder()
                        .getTable(getEntityMappingBuilder().getUpdateTableInfo(attributeInfo));

                Column joinReadColumn = getEntityMappingBuilder().getPersistenceBuilder()
                        .getColumn(myTable, jci.getName(), keyReadColumn.converter());
                Column joinUpdateColumn = null;
                if (jci.isInsertable() || jci.isUpdatable()) {
                    joinUpdateColumn = getEntityMappingBuilder().getPersistenceBuilder()
                            .getColumn(myUpdateTable, jci.getName(), keyUpdateColumn.converter());
                }
                JoinColumn jc = new JoinColumn();
                jc.setMyReadColumn(joinReadColumn);
                if (joinUpdateColumn != null) {
                    jc.setMyUpdateColumn(joinUpdateColumn);
                }
                jc.setReferencedReadColumn(keyReadColumn);
                jc.setReferencedUpdateColumn(keyReadColumn);
                jc.setInsertable(jci.isInsertable());
                jc.setUpdatable(jci.isUpdatable());
                joinColumns.add(jc);
            }
            rpm.setJoinColumns(joinColumns);
        }
        getClassMapping().setPropertyMapping(propertyDescription, rpm);
        propertyMapping = rpm;
    }


}
