package org.mariella.persistence.persistor;

import org.mariella.persistence.database.PreparedPersistorStatement;

import java.util.ArrayList;

public class SimplePersistorStrategy<T extends PreparedPersistorStatement> extends AbstractPersistorStrategy<T> {
    @Override
    public void begin() {
    }

    @Override
    public StrategyResult<T> end() {
        return null;
    }

    @Override
    public StrategyResult<T> beginObjectPersistor(ObjectPersistor<T> objectPersistor) {
        return null;
    }

    public StrategyResult<T> endObjectPersistor() {
        StrategyResult<T> result = new StrategyResult<>();
        result.statements = new ArrayList<>(primaryPreparedPersistorStatements);
        result.statements.addAll(relationshipPreparedPersistorStatements);
        result.callback = this::closeAll;
        return result;
    }
}
