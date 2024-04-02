package org.mariella.persistence.persistor;

import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.database.Table;
import org.mariella.persistence.mapping.CollectionWithoutReferencePropertyMapping;
import org.mariella.persistence.mapping.RelationshipAsTablePropertyMapping;

import java.util.List;

public interface PersistorStrategy<T extends PreparedPersistorStatement> {

    void begin();

    StrategyResult<T> end();

    StrategyResult<T> beginObjectPersistor(ObjectPersistor<T> objectPersistor);

    StrategyResult<T> endObjectPersistor();

    T getBatchRelationshipInsert(RelationshipAsTablePropertyMapping propertyMapping);

    void registerBatchRelationshipInsert(RelationshipAsTablePropertyMapping propertyMapping, T ps);

    T getBatchRelationshipDelete(RelationshipAsTablePropertyMapping propertyMapping);

    void registerBatchRelationshipDelete(RelationshipAsTablePropertyMapping propertyMapping, T ps);

    T getBatchCollectionUpdate(CollectionWithoutReferencePropertyMapping propertyMapping, Table table);

    void registerBatchCollectionUpdate(CollectionWithoutReferencePropertyMapping propertyMapping, Table table, T ps);

    T getPrimaryPreparedPersistorStatement(Table table);

    void registerPrimaryPreparedInsertStatement(Table table, T ps);

    class StrategyResult<T extends PreparedPersistorStatement> {
        public List<T> statements;
        public Runnable callback;
    }

}
