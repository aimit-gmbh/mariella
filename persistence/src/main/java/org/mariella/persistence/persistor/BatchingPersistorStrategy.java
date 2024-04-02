package org.mariella.persistence.persistor;

import org.mariella.persistence.database.PreparedPersistorStatement;
import org.mariella.persistence.runtime.ModificationInfo;

import java.util.ArrayList;

public class BatchingPersistorStrategy<T extends PreparedPersistorStatement> extends AbstractPersistorStrategy<T> {
    private ObjectPersistor<T> currentObjectPersistor;

    @Override
    public void begin() {
        currentObjectPersistor = null;
    }

    @Override
    public StrategyResult<T> end() {
        if (currentObjectPersistor != null) {
            StrategyResult<T> result = new StrategyResult<>();
            result.statements = new ArrayList<>(primaryPreparedPersistorStatements);
            result.statements.addAll(relationshipPreparedPersistorStatements);
            result.callback = this::closeAll;
            return result;
        } else {
            return null;
        }
    }

    @Override
    public StrategyResult<T> beginObjectPersistor(ObjectPersistor<T> objectPersistor) {
        if (currentObjectPersistor != null && currentObjectPersistor.getClassMapping() == objectPersistor.getClassMapping() && currentObjectPersistor.getModificationInfo().getStatus() == objectPersistor.getModificationInfo().getStatus()) {
            if (currentObjectPersistor.getModificationInfo().getStatus() == ModificationInfo.Status.Removed) {
                currentObjectPersistor = objectPersistor;
                return null;
            } else if (currentObjectPersistor.getModificationInfo().getModifiedProperties().equals(objectPersistor.getModificationInfo().getModifiedProperties())) {
                currentObjectPersistor = objectPersistor;
                return null;
            }
        }
        StrategyResult<T> result = new StrategyResult<>();
        result.statements = new ArrayList<>(primaryPreparedPersistorStatements);
        result.statements.addAll(relationshipPreparedPersistorStatements);
        result.callback = () -> {
            closeAll();
            currentObjectPersistor = objectPersistor;
        };
        return result;
    }

    public StrategyResult<T> endObjectPersistor() {
        return null;
    }

}
