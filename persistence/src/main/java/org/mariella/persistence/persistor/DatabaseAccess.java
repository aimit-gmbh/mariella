package org.mariella.persistence.persistor;

import org.mariella.persistence.database.ConnectionCallback;
import org.mariella.persistence.database.Sequence;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class DatabaseAccess {
    private final Map<Sequence, SequenceAccessor> sequenceAccessors = new HashMap<>();

    public DatabaseAccess() {
        super();
    }

    public SequenceAccessor getSequenceAccessor(Sequence sequence) {
        SequenceAccessor sequenceAccessor = sequenceAccessors.get(sequence);
        if (sequenceAccessor == null) {
            sequenceAccessor = new SequenceAccessor(sequence);
            sequenceAccessors.put(sequence, sequenceAccessor);
        }
        return sequenceAccessor;
    }

    public abstract Object doInConnection(ConnectionCallback callback) throws SQLException;


}
