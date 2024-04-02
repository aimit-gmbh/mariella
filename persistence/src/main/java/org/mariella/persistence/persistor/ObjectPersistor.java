package org.mariella.persistence.persistor;

import org.mariella.persistence.database.Column;
import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.mapping.*;
import org.mariella.persistence.runtime.ModifiableAccessor;
import org.mariella.persistence.runtime.ModificationInfo;
import org.mariella.persistence.schema.PropertyDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectPersistor<T extends PreparedPersistorStatement> {
    private final Persistor<T> persistor;
    private final ModificationInfo modificationInfo;

    private final Map<Table, Row> primaryRowMap = new HashMap<>();
    private final List<Row> primaryRows = new ArrayList<>();

    public ObjectPersistor(Persistor<T> persistor, ModificationInfo modificationInfo) {
        this.persistor = persistor;
        this.modificationInfo = modificationInfo;
    }

    public Persistor<T> getPersistor() {
        return persistor;
    }

    public ModificationInfo getModificationInfo() {
        return modificationInfo;
    }

    public ClassMapping getClassMapping() {
        return persistor.getSchemaMapping().getClassMapping(modificationInfo.getObject().getClass().getName());
    }

    public T getBatchInsertStatement(RelationshipAsTablePropertyMapping pm) {
        T ps = persistor.getStrategy().getBatchRelationshipInsert(pm);
        if (ps == null) {
            InsertStatement insert = new InsertStatement(getPersistor().getSchemaMapping().getSchema(), pm.getTable(), pm.getColumnsForInsert());
            ps = insert.prepare(persistor);
            persistor.getStrategy().registerBatchRelationshipInsert(pm, ps);
        }
        return ps;
    }

    public T getSetColumnsDeleteStatement(RelationshipAsOwnedTablePropertyMapping pm) {
        T ps = persistor.getStrategy().getBatchRelationshipDelete(pm);
        if (ps == null) {
            DeleteStatement delete = new DeleteStatement(getPersistor().getSchemaMapping().getSchema(), pm.getTable(), pm.getColumnsForInsert());
            ps = delete.prepare(persistor);
            persistor.getStrategy().registerBatchRelationshipDelete(pm, ps);
        }
        return ps;
    }

    public T getCollectionWithoutReferenceUpdateStatement(CollectionWithoutReferencePropertyMapping pm, Table table, List<Column> columns) {
        T ps = persistor.getStrategy().getBatchCollectionUpdate(pm, table);
        if (ps == null) {
            UpdateStatement update = new UpdateStatement(getPersistor().getSchemaMapping().getSchema(), table, columns);
            ps = update.prepare(persistor);
            persistor.getStrategy().registerBatchCollectionUpdate(pm, table, ps);
        }
        return ps;
    }

    public Row getPrimaryRow(Table table) {
        Row row = primaryRowMap.get(table);
        if (row == null) {
            row = new Row(table);
            primaryRowMap.put(table, row);
            primaryRows.add(row);
            getClassMapping().initializePrimaryRow(this, row);
        }
        return row;
    }

    public T getPrimaryPreparedPersistorStatement(Row row) {
        T ps = persistor.getStrategy().getPrimaryPreparedPersistorStatement(row.getTable());
        if (ps == null) {
            ps = getClassMapping().createPrimaryStatement(this, row).prepare(getPersistor());
            persistor.getStrategy().registerPrimaryPreparedInsertStatement(row.getTable(), ps);
        }
        return ps;
    }

    public void persistPrimary() {
        getClassMapping().createInitialPrimaryRows(this);

        if (modificationInfo.getStatus() == ModificationInfo.Status.New) {
            for (PropertyMapping propertyMapping : getClassMapping().getPropertyMappings()) {
                if (propertyMapping.isInsertable()) {
                    Object value = ModifiableAccessor.Singleton.getValue(modificationInfo.getObject(),
                            propertyMapping.getPropertyDescription());
                    propertyMapping.insertPrimary(this, value);
                }
            }
        } else if (modificationInfo.getStatus() == ModificationInfo.Status.Modified) {
            for (String propertyName : modificationInfo.getModifiedProperties()) {
                PropertyDescription pd = getClassMapping().getClassDescription().getPropertyDescription(propertyName);
                if (pd != null) {
                    PropertyMapping propertyMapping = getClassMapping().getPropertyMapping(pd);
                    if (propertyMapping.isUpdatable()) {
                        Object value = ModifiableAccessor.Singleton.getValue(modificationInfo.getObject(),
                                propertyMapping.getPropertyDescription());
                        propertyMapping.updatePrimary(this, value);
                    }
                }
            }
        }
        for (Row row : primaryRows) {
            getPrimaryPreparedPersistorStatement(row).addBatch(row);
        }
    }

    public void persistSecondary() {
        if (modificationInfo.getStatus() == ModificationInfo.Status.New) {
            for (PropertyMapping propertyMapping : getClassMapping().getPropertyMappings()) {
                if (propertyMapping.isInsertable()) {
                    Object value = ModifiableAccessor.Singleton.getValue(modificationInfo.getObject(),
                            propertyMapping.getPropertyDescription());
                    propertyMapping.insertSecondary(this, value);
                }
            }
        } else if (modificationInfo.getStatus() == ModificationInfo.Status.Modified) {
            for (String propertyName : modificationInfo.getModifiedProperties()) {
                PropertyDescription pd = getClassMapping().getClassDescription().getPropertyDescription(propertyName);
                if (pd != null) {
                    PropertyMapping propertyMapping = getClassMapping().getPropertyMapping(pd);
                    if (propertyMapping.isUpdatable()) {
                        Object value = ModifiableAccessor.Singleton.getValue(modificationInfo.getObject(),
                                propertyMapping.getPropertyDescription());
                        propertyMapping.updateSecondary(this, value);
                    }
                }
            }
        }
    }

}
