package org.mariella.persistence.database;

import org.mariella.persistence.query.BooleanLiteral;
import org.mariella.persistence.query.Literal;


public class BooleanAsNumberConverter implements Converter<Boolean> {
    public static final BooleanAsNumberConverter Singleton = new BooleanAsNumberConverter();

    public Boolean getObject(ResultRow row, int index) {
        Integer value = row.getInteger(index);
        return value != null && value != 0;
    }

    public void setObject(ParameterValues pv, int index, Boolean value) {
        pv.setInteger(index, value ? 1 : 0);
    }

    public Literal<Boolean> createLiteral(Object value) {
        return new BooleanLiteral(this, (Boolean) value);
    }

    @Override
    public Literal<Boolean> createDummy() {
        return createLiteral(false);
    }

    public String toString(Boolean value) {
        return value == null ? "null" : (value ? "1" : "0");
    }
}
