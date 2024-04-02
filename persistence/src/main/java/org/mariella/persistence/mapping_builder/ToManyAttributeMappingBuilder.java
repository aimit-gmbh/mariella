package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.IntegerConverter;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.mapping.*;
import org.mariella.persistence.runtime.Introspector;
import org.mariella.persistence.schema.CollectionPropertyDescription;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

public abstract class ToManyAttributeMappingBuilder<T extends ToManyAttributeInfo> extends RelationAttributeMappingBuilder<T> {
    protected String attributeInfoString;
    protected Table joinTable;
    protected Map<Column, ColumnMapping> foreignKeyMapToOwner;
    protected Map<Column, ColumnMapping> foreignKeyMapToContent;

    public ToManyAttributeMappingBuilder(EntityMappingBuilder entityMappingBuilder, T attributeInfo) {
        super(entityMappingBuilder, attributeInfo);
    }

    @Override
    public CollectionPropertyDescription getPropertyDescription() {
        return (CollectionPropertyDescription) super.getPropertyDescription();
    }

    @Override
    public boolean buildDescription() {
        PropertyDescriptor propertyDescriptor = Introspector.Singleton.getBeanInfo(getEntityInfo().getClazz())
                .getPropertyDescriptor(attributeInfo.getName());
        Class<?> referencedClass = attributeInfo.getRelatedEntityInfo().getClazz();

        CollectionPropertyDescription pd;
        if (attributeInfo.getReverseAttributeInfo() != null) {
            pd = new CollectionPropertyDescription(getClassDescription(), propertyDescriptor, referencedClass.getName(),
                    attributeInfo.getReverseAttributeInfo().getName());
        } else {
            pd = new CollectionPropertyDescription(getClassDescription(), propertyDescriptor, referencedClass.getName());
        }
        getClassDescription().addPropertyDescription(pd);
        propertyDescription = pd;
        return true;
    }

    @Override
    public void buildMapping() {
        super.buildMapping();

        attributeInfoString = getClassDescription().getClassName() + "." + attributeInfo.getName();
        if (attributeInfo.getJoinTableInfo() != null) {
            if (!attributeInfo.getJoinColumnInfos().isEmpty()) {
                throw new IllegalStateException(
                        "Must not specify JoinColumnInfos and a JoinTableInfo for a to many relationship");
            }

            if (attributeInfo.getJoinTableInfo().getJoinColumnInfos().size() == 0) {
                throw new IllegalStateException(
                        attributeInfoString + ": Must specify at least one join column for table "
                                + attributeInfo.getJoinTableInfo() + ")!");
            }

            joinTable = getEntityMappingBuilder().getPersistenceBuilder()
                    .getTable(attributeInfo.getJoinTableInfo().getCatalog(), attributeInfo.getJoinTableInfo().getSchema(),
                            attributeInfo.getJoinTableInfo().getName());
            createForeignKeyMapToOwner();
            createForeignKeyMapToContent();

        }
        if (joinTable != null) {
            createMappingWithJoinTable();
        } else if (attributeInfo.getReverseAttributeInfo() != null) {
            createBiDirectionalMappingWithoutJoinTable();
        } else if (!attributeInfo.getJoinColumnInfos().isEmpty()) {
            createUniDirectionalMappingWithoutJoinTable();
        } else {
            throw new IllegalStateException(
                    "Must specify either JoinColumnInfos or a JoinTableInfo for a to many relationship without reverse " +
                            "attribute specified");
        }
    }

    protected abstract void createBiDirectionalMappingWithoutJoinTable();

    protected abstract void createUniDirectionalMappingWithoutJoinTable();

    protected void createMappingWithJoinTable() {
        if (attributeInfo.getOrderByInfo() != null) {
            Column orderByColumn = getEntityMappingBuilder().getPersistenceBuilder()
                    .getColumn(joinTable, attributeInfo.getOrderByInfo().getOrderBy(), IntegerConverter.Singleton);
            OrderedListAsTablePropertyMapping pm = new OrderedListAsTablePropertyMapping(
                    getClassMapping(),
                    getPropertyDescription(),
                    joinTable.getName(),
                    foreignKeyMapToOwner,
                    foreignKeyMapToContent,
                    orderByColumn.name());
            getClassMapping().setPropertyMapping(getPropertyDescription(), pm);
            propertyMapping = pm;
        } else {
            CollectionAsTablePropertyMapping pm = new CollectionAsTablePropertyMapping(
                    getClassMapping(),
                    getPropertyDescription(),
                    joinTable.getName(),
                    foreignKeyMapToOwner,
                    foreignKeyMapToContent);
            getClassMapping().setPropertyMapping(getPropertyDescription(), pm);
            propertyMapping = pm;
        }
    }

    protected void createForeignKeyMapToOwner() {
        foreignKeyMapToOwner = new HashMap<>();
        for (JoinColumnInfo jci : attributeInfo.getJoinTableInfo().getJoinColumnInfos()) {
            if (jci.getReferencedColumnName() == null || jci.getReferencedColumnName().isEmpty()) {
                throw new IllegalStateException(attributeInfoString + ": No referencedColumnName specified!");
            }
            TableInfo ownerTableInfo = getEntityMappingBuilder().getTableInfoForForeignKeyColumn(jci.getReferencedColumnName());
            Table ownerTable = getEntityMappingBuilder().getPersistenceBuilder().getTable(ownerTableInfo);

            Column ownerColumn = ownerTable.getColumn(jci.getReferencedColumnName());
            if (ownerColumn == null) {
                throw new IllegalStateException(
                        attributeInfoString + ": The column " + ownerTable + "." + jci.getReferencedColumnName() + " does not " +
                                "exist!");
            }
            ColumnMapping ownerColumnMapping = getEntityMappingBuilder().getColumnMapping(ownerColumn);
            if (ownerColumnMapping == null) {
                throw new IllegalStateException(
                        attributeInfoString + ": The column " + ownerTable + "." + jci.getReferencedColumnName() + " is not " +
                                "mapped by owner!");
            }

            Column joinColumn = getEntityMappingBuilder().getPersistenceBuilder()
                    .getColumn(joinTable, jci.getName(), ownerColumnMapping.getReadColumn().converter());
            foreignKeyMapToOwner.put(joinColumn, ownerColumnMapping);
        }
    }

    protected void createForeignKeyMapToContent() {
        EntityMappingBuilder referencedEntityMappingBuilder = getEntityMappingBuilder().getPersistenceBuilder()
                .getEntityMappingBuilder(referencedClassMapping);
        foreignKeyMapToContent = new HashMap<>();
        for (JoinColumnInfo jci : attributeInfo.getJoinTableInfo().getInverseJoinColumnInfos()) {
            if (jci.getReferencedColumnName() == null || jci.getReferencedColumnName().isEmpty()) {
                throw new IllegalStateException(attributeInfoString + ": No referencedColumnName specified!");
            }
            TableInfo referencedTableInfo = referencedEntityMappingBuilder.getTableInfoForForeignKeyColumn(
                    jci.getReferencedColumnName());
            Table referencedTable = referencedEntityMappingBuilder.getPersistenceBuilder().getTable(referencedTableInfo);

            Column contentColumn = referencedTable.getColumn(jci.getReferencedColumnName());
            if (contentColumn == null) {
                throw new IllegalStateException(
                        attributeInfoString + ": The column " + referencedTable + "." + jci.getReferencedColumnName() + " does " +
                                "not exist!");
            }
            ColumnMapping contentColumnMapping = referencedEntityMappingBuilder.getColumnMapping(contentColumn);
            if (contentColumnMapping == null) {
                throw new IllegalStateException(
                        attributeInfoString + ": The column " + referencedTable + "." + jci.getReferencedColumnName() + " is " +
                                "not mapped by content!");
            }

            Column joinColumn = getEntityMappingBuilder().getPersistenceBuilder()
                    .getColumn(joinTable, jci.getName(), contentColumnMapping.getReadColumn().converter());
            foreignKeyMapToContent.put(joinColumn, contentColumnMapping);
        }
    }

}
