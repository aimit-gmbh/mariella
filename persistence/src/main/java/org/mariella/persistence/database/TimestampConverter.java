package org.mariella.persistence.database;

import org.mariella.persistence.query.Literal;
import org.mariella.persistence.query.TimestampLiteral;

import java.sql.Timestamp;


public class TimestampConverter implements Converter<Timestamp> {
    public static final TimestampConverter Singleton = new TimestampConverter();

    public Timestamp getObject(ResultRow row, int index) {
        return row.getTimestamp(index);
    }

    public void setObject(ParameterValues pv, int index, Timestamp value) {
        pv.setTimestamp(index, value);
    }

    public Literal<Timestamp> createLiteral(Object value) {
        return new TimestampLiteral(this, (Timestamp) value);
    }

    @Override
    public Literal<Timestamp> createDummy() {
        return createLiteral(new Timestamp(0));
    }

    public String toString(Timestamp value) {
        return value == null ? "null" : value.toString();
    }

}
