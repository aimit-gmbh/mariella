package org.mariella.persistence.database;

import org.mariella.persistence.query.Literal;
import org.mariella.persistence.query.StringLiteral;


public class NStringConverter implements Converter<String> {
    public static final NStringConverter Singleton = new NStringConverter();

    @Override
    public String getObject(ResultRow row, int index) {
        return row.getNString(index);
    }

    @Override
    public void setObject(ParameterValues pv, int index, String value) {
        pv.setNString(index, value);
    }

    @Override
    public Literal<String> createDummy() {
        return createLiteral("");
    }

    @Override
    public Literal<String> createLiteral(Object value) {
        return new StringLiteral(this, (String) value);
    }

    @Override
    public String toString(String value) {
        if (value != null) {
            return "'" + value.replaceAll("'", "''") + "'";

        }
        return "null";
    }
}
