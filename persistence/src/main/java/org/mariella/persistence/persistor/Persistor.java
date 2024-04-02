package org.mariella.persistence.persistor;

import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.mapping.PersistorStatement;
import org.mariella.persistence.mapping.SchemaMapping;
import org.mariella.persistence.runtime.ModificationInfo;

import java.util.concurrent.CompletionStage;

public interface Persistor<T extends PreparedPersistorStatement> {
    SchemaMapping getSchemaMapping();

    PersistorStrategy<T> getStrategy();

    CompletionStage<Void> generateKey(ModificationInfo modificationInfo);

    T prepareStatement(PersistorStatement statement, String sql, String[] columnNames);

    T prepareStatement(PersistorStatement statement, String sql);
}
