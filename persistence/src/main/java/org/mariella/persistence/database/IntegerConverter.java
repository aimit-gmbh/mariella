package org.mariella.persistence.database;

import org.mariella.persistence.query.IntegerLiteral;
import org.mariella.persistence.query.Literal;


public class IntegerConverter implements Converter<Integer> {
    public static final IntegerConverter Singleton = new IntegerConverter();

    public Integer getObject(ResultRow row, int index) {
        return row.getInteger(index);
    }

    public void setObject(ParameterValues pv, int index, Integer value) {
        pv.setInteger(index, value);
    }

    public Literal<Integer> createLiteral(Object value) {
        return new IntegerLiteral(this, (Integer) value);
    }

    @Override
    public Literal<Integer> createDummy() {
        return createLiteral(0);
    }

    public String toString(Integer value) {
        return value == null ? "null" : value.toString();
    }

}
