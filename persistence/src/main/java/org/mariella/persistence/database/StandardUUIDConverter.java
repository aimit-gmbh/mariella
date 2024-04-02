package org.mariella.persistence.database;

import java.util.UUID;

public class StandardUUIDConverter extends BaseUUIDConverter {
    public static final StandardUUIDConverter Singleton = new StandardUUIDConverter();

    @Override
    public void setObject(ParameterValues pv, int index, UUID value) {
        pv.setUUID(index, value);
    }

    @Override
    public UUID getObject(ResultRow row, int index) {
        return row.getUUID(index);
    }

    @Override
    public void printSql(StringBuilder b, UUID value) {
        if (value == null) {
            b.append("null");
        } else {
            b.append(toString(value));
        }
    }
}
