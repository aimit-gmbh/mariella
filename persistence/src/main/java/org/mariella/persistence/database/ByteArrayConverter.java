package org.mariella.persistence.database;

import org.mariella.persistence.query.ByteArrayLiteral;
import org.mariella.persistence.query.Literal;

public class ByteArrayConverter implements Converter<byte[]> {
    public static final ByteArrayConverter Singleton = new ByteArrayConverter();

    @Override
    public Literal<byte[]> createDummy() {
        byte[] bytes = new byte[]{0};
        return createLiteral(bytes);
    }

    @Override
    public Literal<byte[]> createLiteral(Object value) {
        return new ByteArrayLiteral(this, (byte[]) value);
    }

    @Override
    public byte[] getObject(ResultRow row, int index) {
        return row.getBytes(index);
    }

    @Override
    public void setObject(ParameterValues pv, int index, byte[] value) {
        pv.setBytes(index, value);
    }

    @Override
    public String toString(byte[] value) {
        if (value == null) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("'");
            for (byte b : value) {
                sb.append(Integer.toString(b, 16));
            }
            sb.append("'");
            return sb.toString();
        }
    }

}
