package org.mariella.persistence.mapping;

import org.mariella.persistence.persistor.DatabaseAccess;

public class AutoGenerator extends ColumnValueGenerator {

    @Override
    public boolean isGeneratedByDatabase() {
        return true;
    }

    @Override
    public Object generate(DatabaseAccess dba) {
        throw new UnsupportedOperationException();
    }

}
