package org.mariella.persistence.persistor;

import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.mapping.SchemaMapping;
import org.mariella.persistence.runtime.ModificationTracker;

public abstract class AbstractPersistor<T extends PreparedPersistorStatement> implements Persistor<T> {

    protected final SchemaMapping schemaMapping;
    protected final ModificationTracker modificationTracker;
    protected final PersistorStrategy<T> strategy;

    public AbstractPersistor(SchemaMapping schemaMapping, PersistorStrategy<T> strategy, ModificationTracker modificationTracker) {
        this.modificationTracker = modificationTracker;
        this.schemaMapping = schemaMapping;
        this.strategy = strategy;
    }

    @Override
    public SchemaMapping getSchemaMapping() {
        return schemaMapping;
    }

    @Override
    public PersistorStrategy<T> getStrategy() {
        return strategy;
    }

}