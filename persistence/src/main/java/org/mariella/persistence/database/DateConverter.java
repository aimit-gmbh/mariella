package org.mariella.persistence.database;

import org.mariella.persistence.query.DateLiteral;
import org.mariella.persistence.query.Literal;

import java.util.Date;


public class DateConverter implements Converter<Date> {
    public static final DateConverter Singleton = new DateConverter();

    public Date getObject(ResultRow row, int index) {
        return row.getDate(index);
    }

    public void setObject(ParameterValues pv, int index, Date value) {
        pv.setDate(index, value == null ? null : new java.sql.Date(value.getTime()));
    }

    public Literal<Date> createLiteral(Object value) {
        return new DateLiteral(this, (Date) value);
    }

    @Override
    public Literal<Date> createDummy() {
        return createLiteral(new Date(0));
    }

    public String toString(Date value) {
        return value == null ? "null" : value.toString();
    }

}
