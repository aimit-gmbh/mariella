package org.mariella.persistence.database;

import org.mariella.persistence.query.CharArrayLiteral;
import org.mariella.persistence.query.Literal;

public class CharArrayConverter implements Converter<char[]> {
    public static final CharArrayConverter Singleton = new CharArrayConverter();

    @Override
    public Literal<char[]> createDummy() {
        char[] chars = new char[]{'A'};
        return createLiteral(chars);
    }

    @Override
    public Literal<char[]> createLiteral(Object value) {
        return new CharArrayLiteral(this, (char[]) value);
    }

    @Override
    public char[] getObject(ResultRow row, int index) {
        String string = row.getString(index);
        return string == null ? null : string.toCharArray();
    }

    @Override
    public void setObject(ParameterValues pv, int index, char[] value) {
        pv.setString(index, value == null ? null : new String(value));
    }

    @Override
    public String toString(char[] value) {
        if (value == null) {
            return null;
        } else {
            return "'" + new String(value) + "'";
        }
    }

}
