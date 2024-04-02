package org.mariella.persistence.persistor;

import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.mapping.CollectionWithoutReferencePropertyMapping;
import org.mariella.persistence.mapping.RelationshipAsTablePropertyMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractPersistorStrategy<T extends PreparedPersistorStatement> implements PersistorStrategy<T> {
    protected Map<RelationshipAsTablePropertyMapping, T> batchRelationshipDeleteMap = new HashMap<>();
    protected Map<RelationshipAsTablePropertyMapping, T> batchRelationshipInsertMap = new HashMap<>();
    protected Map<CollectionWithoutReferencePropertyMapping, Map<Table, T>> batchCollectionUpdateMap = new HashMap<>();
    protected List<T> relationshipPreparedPersistorStatements = new ArrayList<>();
    protected Map<Table, T> primaryPreparedPersistorStatementMap = new HashMap<>();
    protected List<T> primaryPreparedPersistorStatements = new ArrayList<>();

    protected void closeAll() {
        closePrimary();
        closeRelationships();
    }

    protected void closePrimary() {
        for (PreparedPersistorStatement ps : primaryPreparedPersistorStatements) {
            ps.close();
        }
        primaryPreparedPersistorStatementMap = new HashMap<>();
        primaryPreparedPersistorStatements = new ArrayList<>();
    }

    protected void closeRelationships() {
        for (PreparedPersistorStatement ps : relationshipPreparedPersistorStatements) {
            ps.close();
        }
        batchRelationshipDeleteMap = new HashMap<>();
        batchRelationshipInsertMap = new HashMap<>();
        batchCollectionUpdateMap = new HashMap<>();
        relationshipPreparedPersistorStatements = new ArrayList<>();
    }

    @Override
    public T getBatchRelationshipInsert(RelationshipAsTablePropertyMapping propertyMapping) {
        return batchRelationshipInsertMap.get(propertyMapping);
    }

    @Override
    public void registerBatchRelationshipInsert(RelationshipAsTablePropertyMapping propertyMapping, T ps) {
        batchRelationshipInsertMap.put(propertyMapping, ps);
        relationshipPreparedPersistorStatements.add(ps);
    }

    @Override
    public T getBatchRelationshipDelete(RelationshipAsTablePropertyMapping propertyMapping) {
        return batchRelationshipDeleteMap.get(propertyMapping);
    }

    @Override
    public void registerBatchRelationshipDelete(RelationshipAsTablePropertyMapping propertyMapping, T ps) {
        batchRelationshipDeleteMap.put(propertyMapping, ps);
        relationshipPreparedPersistorStatements.add(ps);
    }

    @Override
    public T getBatchCollectionUpdate(CollectionWithoutReferencePropertyMapping propertyMapping, Table table) {
        Map<Table, T> map = batchCollectionUpdateMap.get(propertyMapping);
        return map == null ? null : map.get(table);
    }

    @Override
    public void registerBatchCollectionUpdate(CollectionWithoutReferencePropertyMapping propertyMapping, Table table, T ps) {
        Map<Table, T> map = batchCollectionUpdateMap.computeIfAbsent(propertyMapping, k -> new HashMap<>());
        map.put(table, ps);
        relationshipPreparedPersistorStatements.add(ps);
    }

    @Override
    public T getPrimaryPreparedPersistorStatement(Table table) {
        return primaryPreparedPersistorStatementMap.get(table);
    }

    @Override
    public void registerPrimaryPreparedInsertStatement(Table table, T ps) {
        primaryPreparedPersistorStatementMap.put(table, ps);
        primaryPreparedPersistorStatements.add(ps);
    }
}
