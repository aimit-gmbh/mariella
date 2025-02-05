package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.persistor.JoinedInsertStatement;
import org.mariella.persistence.persistor.ObjectPersistor;
import org.mariella.persistence.persistor.Row;
import org.mariella.persistence.query.*;
import org.mariella.persistence.runtime.ModifiableAccessor;
import org.mariella.persistence.runtime.ModificationInfo;
import org.mariella.persistence.schema.ClassDescription;
import org.mariella.persistence.schema.PropertyDescription;
import org.mariella.persistence.util.InitializationHelper;

import java.util.Collection;
import java.util.List;

public class JoinedClassMapping extends SelectableHierarchyClassMapping {
    private final PrimaryKeyJoinColumns primaryKeyJoinColumns = new PrimaryKeyJoinColumns();

    private Table joinTable;
    private Table joinUpdateTable;

    public JoinedClassMapping(SchemaMapping schemaMapping, ClassDescription classDescription) {
        super(schemaMapping, classDescription);
    }

    public PrimaryKeyJoinColumns getPrimaryKeyJoinColumns() {
        return primaryKeyJoinColumns;
    }

    public Table getJoinTable() {
        return joinTable;
    }

    public void setJoinTable(Table joinTable) {
        this.joinTable = joinTable;
    }

    public Table getJoinUpdateTable() {
        return joinUpdateTable;
    }

    public void setJoinUpdateTable(Table joinUpdateTable) {
        this.joinUpdateTable = joinUpdateTable;
    }

    @Override
    public void initialize(InitializationHelper<ClassMapping> initializationHelper) {
        super.initialize(initializationHelper);
        for (PrimaryKeyJoinColumn primaryKeyJoinColumn : primaryKeyJoinColumns.getPrimaryKeyJoinColumns()) {
            primaryKeyJoinColumn.setPrimaryKeyProperty(
                    getPrimaryKeyPropertyDescription(primaryKeyJoinColumn.getPrimaryTableColumn()));
        }
    }

    private PropertyDescription getPrimaryKeyPropertyDescription(Column primaryKeyColumn) {
        for (ColumnMapping columnMapping : getPrimaryKey().getColumnMappings()) {
            if (columnMapping.getReadColumn() == primaryKeyColumn || columnMapping.getUpdateColumn() == primaryKeyColumn) {
                return columnMapping.getPropertyDescription();
            }
        }
        throw new IllegalStateException("No primary key property found for column " + primaryKeyColumn);
    }

    @Override
    protected boolean shouldBeContainedBy(ClassMapping classMapping) {
        return classMapping instanceof JoinedClassMapping;
    }

    @Override
    protected JoinBuilder primitiveCreateJoinBuilder(SubSelectBuilder subSelectBuilder,
                                                     List<SelectableHierarchyClassMapping> affectedChildren,
                                                     List<SelectableHierarchyClassMapping> selectedChildren) {
        if (isSelectableHierarchyRoot()) {
            JoinedClassMappingJoinBuilder joinBuilder = new JoinedClassMappingJoinBuilder();
            JoinBuilder primaryJoinBuilder = super.primitiveCreateJoinBuilder(subSelectBuilder, affectedChildren,
                    selectedChildren);
            joinBuilder.addJoinBuilder(primaryJoinBuilder);
            updateJoinBuilder(subSelectBuilder, joinBuilder);
            for (SelectableHierarchyClassMapping child : affectedChildren) {
                ((JoinedClassMapping) child).updateJoinBuilder(subSelectBuilder, joinBuilder);
            }
            return joinBuilder;
        } else {
            affectedChildren.add(0, this);
            return ((SelectableHierarchyClassMapping) getSuperClassMapping()).primitiveCreateJoinBuilder(subSelectBuilder,
                    affectedChildren, selectedChildren);
        }
    }

    private void updateJoinBuilder(SubSelectBuilder subSelectBuilder, JoinedClassMappingJoinBuilder joinBuilder) {
        if (!isSelectableHierarchyRoot()) {
            if (joinTable != null) {
                SecondaryTableJoinBuilder joinTableJoinBuilder = new SecondaryTableJoinBuilder(subSelectBuilder);
                JoinedSecondaryTable joinedTable = new JoinedSecondaryTable();
                joinedTable.setTable(joinTable);
                subSelectBuilder.addJoinedTable(joinedTable);
                joinTableJoinBuilder.setSecondaryTable(joinedTable);
                for (PrimaryKeyJoinColumn primaryKeyJoinColumn : primaryKeyJoinColumns.getPrimaryKeyJoinColumns()) {
                    joinTableJoinBuilder.getConditionBuilder(primaryKeyJoinColumn.getPrimaryTableColumn()).and(
                            BinaryCondition.eq(
                                    joinBuilder.getPrimaryJoinBuilder().getJoinedTableReference()
                                            .createColumnReference(primaryKeyJoinColumn.getPrimaryTableColumn()),
                                    joinedTable.createUnreferencedColumnReference(primaryKeyJoinColumn.getJoinTableColumn())));
                }
                joinBuilder.addJoinBuilder(joinTableJoinBuilder);
            } else if (!getClassDescription().isAbstract()) {
                throw new IllegalStateException();
            }
        }
    }

    public String toString() {
        return getClassDescription().toString() + "(" + (joinTable != null ? joinTable.toString() : getPrimaryTable().toString())
                + ")";
    }

    @Override
    public void collectUsedTables(Collection<Table> collection) {
        if (isSelectableHierarchyRoot()) {
            super.collectUsedTables(collection);
        } else {
            if (joinTable != null && !collection.contains(joinTable)) {
                collection.add(joinTable);
            }
            if (joinUpdateTable != null && !collection.contains(joinUpdateTable)) {
                collection.add(joinUpdateTable);
            }
            collectUsedTablesFromProperties(collection);
        }
    }

    @Override
    public Row getPrimaryRow(ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor, PropertyMapping propertyMapping) {
        if (propertyMapping.getPropertyDescription().getClassDescription() == getClassDescription()) {
            if (isSelectableHierarchyRoot()) {
                return super.getPrimaryRow(objectPersistor, propertyMapping);
            } else {
                return objectPersistor.getPrimaryRow(joinUpdateTable);
            }
        } else {
            return getSuperClassMapping().getPrimaryRow(objectPersistor, propertyMapping);
        }
    }

    @Override
    public void initializePrimaryRow(ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor, Row row, Object discriminatorValue) {
        if (isSelectableHierarchyRoot()) {
            super.initializePrimaryRow(objectPersistor, row, discriminatorValue);
        } else if (row.getTable() == joinUpdateTable) {
            for (PrimaryKeyJoinColumn primaryKeyJoinColumn : getPrimaryKeyJoinColumns().getPrimaryKeyJoinColumns()) {
                Object value = ModifiableAccessor.Singleton.getValue(objectPersistor.getModificationInfo().getObject(), primaryKeyJoinColumn.getPrimaryKeyProperty());
                row.setProperty(primaryKeyJoinColumn.getJoinTableColumn(), value);
            }
        } else {
            ((SelectableHierarchyClassMapping) getSuperClassMapping()).initializePrimaryRow(objectPersistor, row, discriminatorValue);
        }
    }

    @Override
    public void createInitialPrimaryRows(ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor) {
        if (isSelectableHierarchyRoot()) {
            super.createInitialPrimaryRows(objectPersistor);
        } else {
            if (objectPersistor.getModificationInfo().getStatus() == ModificationInfo.Status.Removed) {
                objectPersistor.getPrimaryRow(joinUpdateTable);
            }
            getSuperClassMapping().createInitialPrimaryRows(objectPersistor);
            if (objectPersistor.getModificationInfo().getStatus() == ModificationInfo.Status.New) {
                objectPersistor.getPrimaryRow(joinUpdateTable);
            }
        }
    }

    @Override
    public PersistorStatement createPrimaryStatement(ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor, Row row) {
        if (isSelectableHierarchyRoot()) {
            return super.createPrimaryStatement(objectPersistor, row);
        } else {
            if (row.getTable() == joinUpdateTable) {
                if (objectPersistor.getModificationInfo().getStatus() == ModificationInfo.Status.New) {
                    return new JoinedInsertStatement(this, row.getTable(), row.getSetColumns());
                } else if (objectPersistor.getModificationInfo().getStatus() == ModificationInfo.Status.Modified) {
                    return objectPersistor.getClassMapping().getSchemaMapping().getSchema().createJoinedUpsertStatement(this, row.getSetColumns());
                } else if (objectPersistor.getModificationInfo().getStatus() == ModificationInfo.Status.Removed) {
                    return new DeleteStatement(schemaMapping.getSchema(), row.getTable(), row.getTable().getPrimaryKey());
                } else {
                    throw new IllegalStateException();
                }
            } else {
                return getSuperClassMapping().createPrimaryStatement(objectPersistor, row);
            }
        }
    }
}
