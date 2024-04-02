package org.mariella.persistence.database;

import org.mariella.persistence.query.Literal;
import org.mariella.persistence.query.LongLiteral;


public class LongConverter implements Converter<Long> {
    public static final LongConverter Singleton = new LongConverter();

    public Long getObject(ResultRow row, int index) {
        return row.getLong(index);
    }

    public void setObject(ParameterValues pv, int index, Long value) {
        pv.setLong(index, value);
    }

    @Override
    public Literal<Long> createDummy() {
        return createLiteral(0L);
    }

    public Literal<Long> createLiteral(Object value) {
        return new LongLiteral(this, (Long) value);
    }

    public String toString(Long value) {
        return value == null ? "null" : value.toString();
    }
}
