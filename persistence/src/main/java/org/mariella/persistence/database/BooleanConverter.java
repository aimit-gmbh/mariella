package org.mariella.persistence.database;

import org.mariella.persistence.query.BooleanLiteral;
import org.mariella.persistence.query.Literal;


public class BooleanConverter implements Converter<Boolean> {
    public static final BooleanConverter Singleton = new BooleanConverter();

    public Boolean getObject(ResultRow row, int index) {
        return row.getBoolean(index);
    }

    public void setObject(ParameterValues pv, int index, Boolean value) {
        pv.setBoolean(index, value);
    }

    public Literal<Boolean> createLiteral(Object value) {
        return new BooleanLiteral(this, (Boolean) value);
    }

    @Override
    public Literal<Boolean> createDummy() {
        return createLiteral(false);
    }

    public String toString(Boolean value) {
        return value == null ? "null" : value.toString();
    }
}
