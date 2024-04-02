package org.mariella.persistence.mapping;

import org.mariella.persistence.database.Sequence;
import org.mariella.persistence.persistor.DatabaseAccess;

public class SequenceGenerator extends ColumnValueGenerator {
    private final Sequence sequence;

    public SequenceGenerator(Sequence sequence) {
        super();
        this.sequence = sequence;
    }

    @Override
    public boolean isGeneratedByDatabase() {
        return false;
    }

    @Override
    public Object generate(DatabaseAccess dba) {
        return dba.getSequenceAccessor(sequence).nextValue(dba);
    }

}
