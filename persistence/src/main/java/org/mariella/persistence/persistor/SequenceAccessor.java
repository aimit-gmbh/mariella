package org.mariella.persistence.persistor;

import org.mariella.persistence.database.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SequenceAccessor {
    private static final Logger logger = LoggerFactory.getLogger(SequenceAccessor.class);
    
    private final Sequence sequence;

    private Long nextId = null;
    private int increment = 0;

    public SequenceAccessor(Sequence sequence) {
        super();
        this.sequence = sequence;
    }

    public long nextValue(final DatabaseAccess dba) {
        if (nextId == null || increment == sequence.getAllocationSize()) {
            try {
                dba.doInConnection(
                        connection -> {
                            String sql = "SELECT " + sequence.getName() + ".NEXTVAL FROM DUAL";
                            if (logger.isDebugEnabled())
                                logger.debug(sql);
                            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                                try (ResultSet rs = ps.executeQuery()) {
                                    rs.next();
                                    nextId = rs.getLong(1);
                                }
                            }
                            return null;
                        });
            } catch (SQLException e) {
                logger.error("Cannot fetch next sequence value from database", e);
                throw new RuntimeException("Cannot fetch next sequence value from database");
            }
            increment = 0;
        }
        long result = nextId + increment;
        increment++;
        return result;

    }

}
