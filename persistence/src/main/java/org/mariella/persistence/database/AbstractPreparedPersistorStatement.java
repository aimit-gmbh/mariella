package org.mariella.persistence.database;

import org.mariella.persistence.mapping.PersistorStatement;

public abstract class AbstractPreparedPersistorStatement implements PreparedPersistorStatement {
    protected final PersistorStatement statement;

    public AbstractPreparedPersistorStatement(PersistorStatement statement) {
        this.statement = statement;
    }
}
