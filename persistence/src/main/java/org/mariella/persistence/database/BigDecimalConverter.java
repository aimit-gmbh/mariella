package org.mariella.persistence.database;

import org.mariella.persistence.query.BigDecimalLiteral;
import org.mariella.persistence.query.Literal;

import java.math.BigDecimal;


public class BigDecimalConverter implements Converter<BigDecimal> {
    public static final BigDecimalConverter Singleton = new BigDecimalConverter();

    public BigDecimal getObject(ResultRow row, int index) {
        return row.getBigDecimal(index);
    }

    public void setObject(ParameterValues pv, int index, BigDecimal value) {
        pv.setBigDecimal(index, value);
    }

    public Literal<BigDecimal> createLiteral(Object value) {
        return new BigDecimalLiteral(this, (BigDecimal) value);
    }

    @Override
    public Literal<BigDecimal> createDummy() {
        return createLiteral(new BigDecimal("0"));
    }

    public String toString(BigDecimal value) {
        return value == null ? "null" : "'" + value + "'";
    }
}
