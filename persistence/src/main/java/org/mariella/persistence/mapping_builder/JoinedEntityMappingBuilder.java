package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.StringConverter;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.mapping.*;

public class JoinedEntityMappingBuilder extends SelectableHierarchyEntityMappingBuilder {

    public JoinedEntityMappingBuilder(PersistenceBuilder persistenceBuilder, EntityInfo entityInfo) {
        super(persistenceBuilder, entityInfo);
    }

    @Override
    protected void primitiveBuildMapping() {
        super.primitiveBuildMapping();

        classMapping = new JoinedClassMapping(persistenceBuilder.getPersistenceInfo().getSchemaMapping(), getClassDescription());
        ((JoinedClassMapping) classMapping).setDiscriminatorValue(discriminatorValue);

        SelectableHierarchyEntityMappingBuilder tableDefiningBuilder = getTableDefiningEntityMappingBuilder();
        if (tableDefiningBuilder == this) {
            if (!entityInfo.getPrimaryKeyJoinColumnInfos().isEmpty()) {
                throw new IllegalStateException("@PrimaryKeyJoinColumn must not occur in the root of a joined table hierarchy");
            }
            classMapping.setPrimaryTable(table);
            classMapping.setPrimaryUpdateTable(updateTable);
            ((JoinedClassMapping) classMapping).setDiscriminatorColumn(discriminatorColumn);
        } else {
            if (discriminatorColumn != null) {
                throw new IllegalStateException("@DiscriminatorColumn must not occur in the root of a joined table hierarchy");
            }

            if (entityInfo.getPrimaryKeyJoinColumnInfos().isEmpty() && !getClassDescription().isAbstract()) {
                throw new IllegalStateException(
                        "At least one @PrimaryKeyJoinColumn must occur in the root of a joined table hierarchy");
            }

            Table primaryTable = persistenceBuilder.getTable(getTableDefiningEntityMappingBuilder().getTableInfo());
            TableInfo joinTableInfo = entityInfo.getTableInfo();
            if (joinTableInfo == null && !getClassDescription().isAbstract()) {
                throw new IllegalStateException("A table info must be specified for Inheritance Type JOINED");
            }
            if (joinTableInfo == null) {
                if (getClassDescription().hasLocalPropertyDescriptions()) {
                    throw new IllegalArgumentException(
                            "Abstract joined subclasses without join tables must not have locally defined mapped properties!");
                }
                if (!entityInfo.getPrimaryKeyJoinColumnInfos().isEmpty()) {
                    throw new IllegalArgumentException(
                            "@JoinColumnInfos are not allowed for abstract joined subclasses without join tables!");
                }

            } else {
                Table joinTable = persistenceBuilder.getTable(joinTableInfo);
                ((JoinedClassMapping) classMapping).setJoinTable(joinTable);

                TableInfo joinUpdateTableInfo = entityInfo.getUpdateTableInfo();
                Table joinUpdateTable = joinTable;
                if (joinUpdateTableInfo != null) {
                    joinUpdateTable = persistenceBuilder.getTable(joinUpdateTableInfo);
                }
                ((JoinedClassMapping) classMapping).setJoinUpdateTable(joinUpdateTable);
                for (PrimaryKeyJoinColumnInfo primaryKeyJoinColumnInfo : entityInfo.getPrimaryKeyJoinColumnInfos()) {
                    if (primaryKeyJoinColumnInfo.getName() == null || primaryKeyJoinColumnInfo.getName().length() == 0) {
                        throw new IllegalArgumentException("Primary key join colums must have their names specified!");
                    }
                    if (primaryKeyJoinColumnInfo.getReferencedColumnName() == null
                            || primaryKeyJoinColumnInfo.getReferencedColumnName()
                            .length() == 0) {
                        throw new IllegalArgumentException("name invalid for @PrimaryKeyJoinColumn!");
                    }
                    Column primaryKeyColumn = primaryTable.getColumn(primaryKeyJoinColumnInfo.getReferencedColumnName());
                    if (primaryKeyColumn == null) {
                        throw new IllegalArgumentException("referencedColumnName invalid for @PrimaryKeyJoinColumn!");
                    }
                    Column joinTableColumn = persistenceBuilder.getColumn(joinUpdateTable, primaryKeyJoinColumnInfo.getName(),
                            primaryKeyColumn.converter());
                    if (joinTableColumn == null) {
                        throw new IllegalArgumentException("Primary key join column not found!");
                    }

                    PrimaryKeyJoinColumn primaryKeyJoinColumn = new PrimaryKeyJoinColumn();
                    primaryKeyJoinColumn.setJoinTableColumn(joinTableColumn);
                    primaryKeyJoinColumn.setPrimaryTableColumn(primaryKeyColumn);
                    ((JoinedClassMapping) classMapping).getPrimaryKeyJoinColumns().getPrimaryKeyJoinColumns()
                            .add(primaryKeyJoinColumn);
                }
            }
        }
    }

    private SelectableHierarchyEntityMappingBuilder getTableDefiningEntityMappingBuilder() {
        SelectableHierarchyEntityMappingBuilder root = this;
        while (root.getSuperEntityMappingBuilder() != null
                && root.getSuperEntityMappingBuilder() instanceof SelectableHierarchyEntityMappingBuilder) {
            root = (SelectableHierarchyEntityMappingBuilder) root.getSuperEntityMappingBuilder();
        }
        return root;
    }

    private TableInfo getPrimaryTableInfo() {
        return getTableDefiningEntityMappingBuilder().getTableInfo();
    }

    @Override
    protected Column getColumn(DiscriminatorColumnInfo discriminatorColumnInfo) {
        Table table = persistenceBuilder.getTable(getPrimaryTableInfo());
        return getPersistenceBuilder().getColumn(table, discriminatorColumnInfo.getName(), StringConverter.Singleton);
    }

    @Override
    protected Column getUpdateColumn(DiscriminatorColumnInfo discriminatorColumnInfo) {
        if (updateTable != null) {
            if (persistenceBuilder.hasColumn(updateTable, discriminatorColumnInfo.getName())) {
                return getPersistenceBuilder().getColumn(updateTable, discriminatorColumnInfo.getName(),
                        StringConverter.Singleton);
            }
        }
        return null;
    }


    @Override
    protected TableInfo getTableInfo(AttributeInfo attributeInfo) {
        if (isInherited(attributeInfo)) {
            return getSuperEntityMappingBuilder().getTableInfo(attributeInfo);
        } else {
            return getTableInfo();
        }
    }

    @Override
    public TableInfo getTableInfoForForeignKeyColumn(String columnName) {
        return getTableDefiningEntityMappingBuilder().getTableInfo();
    }

    @Override
    public TableInfo getUpdateTableInfoForForeignKeyColumn(String columnName) {
        return getTableDefiningEntityMappingBuilder().getUpdateTableInfo();
    }

}
