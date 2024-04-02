package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.database.ResultSetReader;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.persistor.ObjectPersistor;
import org.mariella.persistence.persistor.PropertyChooser;
import org.mariella.persistence.persistor.Row;
import org.mariella.persistence.query.*;
import org.mariella.persistence.runtime.ModifiableAccessor;
import org.mariella.persistence.runtime.ModificationInfo;
import org.mariella.persistence.schema.ClassDescription;
import org.mariella.persistence.schema.PropertyDescription;

import java.sql.SQLException;
import java.util.*;

public abstract class ClassMapping extends AbstractClassMapping {
    private final Map<PropertyDescription, PropertyMapping> hierarchyPropertyMappingMap =
            new HashMap<>();
    private final List<PropertyMapping> hierarchyPropertyMappings = new ArrayList<>();
    private final List<PhysicalPropertyMapping> hierarchyPhysicalPropertyMappingList = new ArrayList<>();
    protected Table primaryTable;
    protected Table primaryUpdateTable;
    private PrimaryKey primaryKey;
    private List<ClassMapping> immediateChildren;
    private List<ClassMapping> allChildren;

    public ClassMapping(SchemaMapping schemaMapping, ClassDescription classDescription) {
        super(schemaMapping, classDescription);
    }

    public Table getPrimaryTable() {
        return primaryTable;
    }

    public void setPrimaryTable(Table primaryTable) {
        this.primaryTable = primaryTable;
    }

    public Table getPrimaryUpdateTable() {
        return primaryUpdateTable;
    }

    public void setPrimaryUpdateTable(Table primaryUpdateTable) {
        this.primaryUpdateTable = primaryUpdateTable;
    }

    public List<PropertyMapping> getHierarchyPropertyMappings() {
        return hierarchyPropertyMappings;
    }

    public List<PhysicalPropertyMapping> getHierarchyPhysicalPropertyMappingList() {
        return hierarchyPhysicalPropertyMappingList;
    }

    public PropertyMapping getPropertyMappingInHierarchy(PropertyDescription propertyDescription) {
        return hierarchyPropertyMappingMap.get(propertyDescription);
    }

    public List<ClassMapping> getImmediateChildren() {
        return immediateChildren;
    }

    public List<ClassMapping> getAllChildren() {
        return allChildren;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public ColumnMapping getColumnMapping(Column column) {
        for (PhysicalPropertyMapping pm : physicalPropertyMappingList) {
            if (pm instanceof ColumnMapping cm) {
                if (cm.getReadColumn() == column || cm.getUpdateColumn() == column) {
                    return cm;
                }
            }
        }
        return null;
    }

    public boolean isChildOf(ClassMapping classMapping) {
        ClassMapping cm = getSuperClassMapping();
        while (cm != null && cm != classMapping) {
            cm = cm.getSuperClassMapping();
        }
        return cm != null;
    }

    @Override
    public void initialize(ClassMappingInitializationContext context) {
        super.initialize(context);

        immediateChildren = new ArrayList<>();
        allChildren = new ArrayList<>();

        for (ClassMapping classMapping : getSchemaMapping().getClassMappings()) {
            if (classMapping.isChildOf(this)) {
                allChildren.add(classMapping);
                if (classMapping.getSuperClassMapping() == this) {
                    immediateChildren.add(classMapping);
                }
            }
        }

        if (getSuperClassMapping() == null) {
            if (getClassDescription().getIdentityPropertyDescriptions() == null) {
                throw new IllegalStateException("No @Id specified for class " + getClassDescription().getClassName());
            }
            List<ColumnMapping> columnMappings = new ArrayList<>();
            for (PhysicalPropertyMapping propertyMapping : getPhysicalPropertyMappingList()) {
                if (getClassDescription().isId(propertyMapping.getPropertyDescription())) {
                    columnMappings.add((ColumnMapping) propertyMapping);
                }
            }
            primaryKey = new PrimaryKey(this, columnMappings.toArray(new ColumnMapping[0]));
        } else {
            primaryKey = getSuperClassMapping().getPrimaryKey();
        }
    }

    public void postInitialize(ClassMappingInitializationContext context) {
        super.postInitialize(context);
        for (PropertyMapping pm : getPropertyMappings()) {
            hierarchyPropertyMappings.add(pm);
            hierarchyPropertyMappingMap.put(pm.getPropertyDescription(), pm);
        }
        hierarchyPhysicalPropertyMappingList.addAll(getPhysicalPropertyMappingList());

        for (ClassMapping child : getImmediateChildren()) {
            context.ensureInitialized(child);
            for (PropertyMapping pm : child.getHierarchyPropertyMappings()) {
                if (!hierarchyPropertyMappings.contains(pm)) {
                    hierarchyPropertyMappings.add(pm);
                    hierarchyPropertyMappingMap.put(pm.getPropertyDescription(), pm);
                }
            }
            for (PhysicalPropertyMapping pm : child.getHierarchyPhysicalPropertyMappingList()) {
                if (!hierarchyPhysicalPropertyMappingList.contains(pm)) {
                    hierarchyPhysicalPropertyMappingList.add(pm);
                }
            }
        }
    }

    public boolean isLeaf() {
        return getAllChildren().isEmpty();
    }

    public abstract Object getDiscriminatorValue();

    public Object getDiscriminatorValue(ResultSetReader reader) {
        return reader.getResultRow().getString(reader.getCurrentColumnIndex());
    }

    public void addObjectColumns(SubSelectBuilder subSelectBuilder, TableReference tableReference,
                                 PropertyChooser propertyChooser) {
        if (!isLeaf()) {
            addDiscriminatorColumn(subSelectBuilder, tableReference);
        }
        for (PhysicalPropertyMapping pm : getHierarchyPhysicalPropertyMappingList()) {
            if (propertyChooser.wants(pm.getPropertyDescription())) {
                pm.addColumns(subSelectBuilder, tableReference);
            }
        }
    }

    public abstract SelectItem addDiscriminatorColumn(SubSelectBuilder subSelectBuilder, TableReference tableReference);

    public abstract void registerDiscriminator(HierarchySubSelect hierarchySubSelect);

    public void addIdentityColumns(SubSelectBuilder subSelectBuilder, TableReference tableReference) {
        if (!isLeaf()) {
            addDiscriminatorColumn(subSelectBuilder, tableReference);
        }
        primaryKey.addColumns(subSelectBuilder, tableReference);
    }

    public JoinBuilder createJoinBuilder(SubSelectBuilder subSelectBuilder) {
        if (needsSubSelect()) {
            JoinBuilderImpl joinBuilder = new JoinBuilderImpl(subSelectBuilder);
            HierarchySubSelect hierarchySubSelect = new HierarchySubSelect();
            createJoinBuilderSubSelect(hierarchySubSelect, getHierarchyPropertyMappings());
            hierarchySubSelect.setAlias(subSelectBuilder.createTableAlias("A"));
            subSelectBuilder.addJoinedTableReference(hierarchySubSelect);
            joinBuilder.setJoinedTableReference(hierarchySubSelect);
            return joinBuilder;
        } else {
            return primitiveCreateJoinBuilder(subSelectBuilder);
        }
    }

    protected JoinBuilder primitiveCreateJoinBuilder(SubSelectBuilder subSelectBuilder) {
        JoinBuilderImpl joinBuilder = new JoinBuilderImpl(subSelectBuilder);
        JoinedTable joinedTable = subSelectBuilder.createJoinedTable(getPrimaryTable());
        joinBuilder.setJoinedTableReference(joinedTable);
        return joinBuilder;
    }

    protected void createJoinBuilderSubSelect(final HierarchySubSelect hierarchySubSelect,
                                              List<PropertyMapping> propertyMappingList) {
        final SubSelectBuilder subSelectBuilder = new SubSelectBuilder();
        JoinBuilder joinBuilder = primitiveCreateJoinBuilder(subSelectBuilder);
        joinBuilder.createJoin();

        final TableReference joinedTableReference = joinBuilder.getJoinedTableReference();

        ColumnVisitor addColumnsCallback = column -> hierarchySubSelect.selectColumn(subSelectBuilder, joinedTableReference,
                column);

        ColumnVisitor addDummiesCallback = column -> hierarchySubSelect.selectDummy(subSelectBuilder, joinedTableReference,
                column);

        registerDiscriminator(hierarchySubSelect);
        SelectItem selectItem = addDiscriminatorColumn(subSelectBuilder, joinedTableReference);
        selectItem.setAlias("D");

        for (PropertyMapping pm : propertyMappingList) {
            if (maySelectForHierarchy(pm)) {
                pm.visitColumns(addColumnsCallback);
            } else {
                pm.visitColumns(addDummiesCallback);
            }
        }
        hierarchySubSelect.addSubSelectBuilder(subSelectBuilder);

        for (ClassMapping child : immediateChildren) {
            child.collectJoinBuilderSubSelects(hierarchySubSelect, propertyMappingList);
        }
    }

    protected abstract boolean maySelectForHierarchy(PropertyMapping propertyMapping);

    protected abstract void collectJoinBuilderSubSelects(HierarchySubSelect hierarchySubSelect,
                                                         List<PropertyMapping> propertyMappingList);

    protected boolean needsSubSelect() {
        return !isLeaf();
    }

    protected boolean needsSubSelect(ClassMapping childMapping) {
        return true;
    }

    public Object createObject(ResultSetReader reader, ObjectFactory factory, boolean wantsObjects,
                               PropertyChooser propertyChooser)
            throws SQLException {
        if (isLeaf()) {
            return createObject(reader, factory, wantsObjects, getHierarchyPhysicalPropertyMappingList(), propertyChooser);
        } else {
            Object value = getDiscriminatorValue(reader);
            if (value == null) {
                return null;
            } else {
                reader.setCurrentColumnIndex(reader.getCurrentColumnIndex() + 1);
                ClassMapping effectiveMapping = getClassMappingForDiscriminatorValue(value);
                return effectiveMapping.createObject(reader, factory, wantsObjects, getHierarchyPhysicalPropertyMappingList(),
                        propertyChooser);
            }
        }
    }

    public Object createObject(ResultSetReader reader, ObjectFactory factory, boolean wantsObjects,
                               List<PhysicalPropertyMapping> physicalPropertyMappings,
                               PropertyChooser propertyChooser)
            throws SQLException {
        int columnIndex = reader.getCurrentColumnIndex();
        int idIndex = reader.getCurrentColumnIndex();
        if (wantsObjects) {
            idIndex += primaryKey.getIndex(physicalPropertyMappings);
        }
        reader.setCurrentColumnIndex(idIndex);
        Object identity = primaryKey.getIdentity(reader, factory, getClassDescription());
        Object entity = identity == null ? null : factory.getObject(this, identity);
        if (identity == null) {
            reader.setCurrentColumnIndex(reader.getCurrentColumnIndex() + (wantsObjects ? physicalPropertyMappings.size() : 1));
        } else {
            boolean update;
            if (entity != null) {
                update = true;
            } else {
                update = false;
                entity = factory.createObject(this, identity);
            }
            if (wantsObjects) {
                reader.setCurrentColumnIndex(columnIndex);
                for (PhysicalPropertyMapping pm : physicalPropertyMappings) {
                    if (propertyChooser.wants(pm.getPropertyDescription())) {
                        if (primaryKey.contains(pm) || !getPhysicalPropertyMappingList().contains(pm)) {
                            pm.advance(reader);
                        } else {
                            Object value = pm.getObject(reader, factory);
                            if (update) {
                                factory.updateValue(entity, pm.getPropertyDescription(), value);
                            } else {
                                factory.setValue(entity, pm.getPropertyDescription(), value);
                            }
                        }
                    }
                }
            }
        }

        return entity;
    }

    public ClassMapping getClassMappingForDiscriminatorValue(Object value) {
        if (getDiscriminatorValue() != null && getDiscriminatorValue().equals(value)) {
            return this;
        } else {
            for (ClassMapping child : getAllChildren()) {
                if (!child.getClassDescription().isAbstract() && child.getDiscriminatorValue().equals(value)) {
                    return child;
                }
            }
            return null;
        }
    }

    public String toString() {
        return getClassDescription().toString() + " (" + getPrimaryTable().getName() + ")";
    }

    public void collectUsedTables(Collection<Table> collection) {
        if (!collection.contains(primaryTable)) {
            collection.add(primaryTable);
        }
        if (!collection.contains(primaryUpdateTable)) {
            collection.add(primaryUpdateTable);
        }
        collectUsedTablesFromProperties(collection);
    }

    protected void collectUsedTablesFromProperties(Collection<Table> collection) {
        for (PropertyMapping pm : getPropertyMappings()) {
            pm.collectUsedTables(collection);
        }
    }

    public void collectUsedColumns(Collection<Column> collection) {
        for (PropertyMapping pm : getPropertyMappings()) {
            pm.collectUsedColumns(collection);
        }
    }

    public void createInitialPrimaryRows(ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor) {
        ModificationInfo.Status status = objectPersistor.getModificationInfo().getStatus();
        if (status == ModificationInfo.Status.New || status == ModificationInfo.Status.Removed) {
            objectPersistor.getPrimaryRow(getPrimaryUpdateTable());
        }
    }

    public PersistorStatement createPrimaryStatement(ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor, Row row) {
        if (row.getTable() == primaryUpdateTable) {
            PersistorStatement statement;
            if (objectPersistor.getModificationInfo().getStatus() == ModificationInfo.Status.New) {
                statement = new PrimaryInsertStatement(objectPersistor, row.getTable(), row.getSetColumns());
            } else if (objectPersistor.getModificationInfo().getStatus() == ModificationInfo.Status.Removed) {
                statement = new DeleteStatement(getSchemaMapping().getSchema(), row.getTable(), row.getSetColumns());
            } else if (objectPersistor.getModificationInfo().getStatus() == ModificationInfo.Status.Modified) {
                statement = new UpdateStatement(getSchemaMapping().getSchema(), row.getTable(), row.getSetColumns());
            } else {
                throw new IllegalStateException();
            }
            return statement;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public Row getPrimaryRow(ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor, PropertyMapping propertyMapping) {
        return objectPersistor.getPrimaryRow(getPrimaryUpdateTable());
    }

    public void initializePrimaryRow(ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor, Row row) {
        if (row.getTable() == primaryUpdateTable) {
            ModificationInfo.Status status = objectPersistor.getModificationInfo().getStatus();
            if (status == ModificationInfo.Status.Removed || status == ModificationInfo.Status.Modified) {
                persistPrimaryKey(objectPersistor, row);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void persistPrimaryKey(ObjectPersistor<? extends PreparedPersistorStatement> objectPersistor, Row row) {
        for (ColumnMapping columnMapping : getPrimaryKey().getColumnMappings()) {
            Object value = ModifiableAccessor.Singleton.getValue(objectPersistor.getModificationInfo().getObject(),
                    columnMapping.getPropertyDescription());
            columnMapping.persistPrimary(objectPersistor, value);
        }
    }
}

