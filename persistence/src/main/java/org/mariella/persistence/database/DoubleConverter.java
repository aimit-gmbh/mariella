package org.mariella.persistence.database;

import org.mariella.persistence.query.DoubleLiteral;
import org.mariella.persistence.query.Literal;

public class DoubleConverter implements Converter<Double> {

    public static final DoubleConverter Singleton = new DoubleConverter();

    @Override
    public Literal<Double> createLiteral(Object value) {
        return new DoubleLiteral(this, (Double) value);
    }

    @Override
    public Literal<Double> createDummy() {
        return createLiteral(0.0d);
    }

    @Override
    public Double getObject(ResultRow row, int index) {
        return row.getDouble(index);
    }

    @Override
    public void setObject(ParameterValues pv, int index, Double value) {
        pv.setDouble(index, value);
    }

    @Override
    public String toString(Double value) {
        return value == null ? "null" : value.toString();
    }

}
