package org.mariella.persistence.database;

import org.mariella.persistence.query.Literal;
import org.mariella.persistence.query.StringLiteral;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.SQLException;

public class ClobConverter implements Converter<String> {
    public static final ClobConverter Singleton = new ClobConverter();

    public String getObject(ResultRow row, int index) {
        Clob clob = row.getClob(index);
        if (clob == null) {
            return null;
        } else {
            try {
                try (BufferedReader reader = new BufferedReader(clob.getCharacterStream())) {
                    char[] buf = new char[4 * 1024];
                    int len;
                    StringBuilder sb = new StringBuilder();
                    while ((len = reader.read(buf)) > -1) {
                        sb.append(new String(buf, 0, len));
                    }
                    return sb.toString();
                }
            } catch (IOException | SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setObject(ParameterValues pv, int index, String value) {
        pv.setClob(index, value == null ? null : new StringReader(value));
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
