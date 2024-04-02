package org.mariella.persistence.database;

import org.mariella.persistence.query.Literal;
import org.mariella.persistence.query.StringLiteral;


public class StringConverter implements Converter<String> {
    public static final StringConverter Singleton = new StringConverter();

    public String getObject(ResultRow row, int index) {
        return row.getString(index);
    }

    public void setObject(ParameterValues pv, int index, String value) {
        pv.setString(index, value);
    }

    @Override
    public Literal<String> createDummy() {
        return createLiteral("");
    }

    public Literal<String> createLiteral(Object value) {
        return new StringLiteral(this, (String) value);
    }

    public String toString(String value) {
        if (value != null) {
            return "'" + value.replaceAll("'", "''") + "'";

        }
        return "null";
    }
}
