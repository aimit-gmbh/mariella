package org.mariella.persistence.oracle;

import org.mariella.persistence.database.BaseUUIDConverter;
import org.mariella.persistence.database.ParameterValues;
import org.mariella.persistence.database.ResultRow;

import java.util.UUID;

public class OracleUUIDConverter extends BaseUUIDConverter {
    public static final OracleUUIDConverter Singleton = new OracleUUIDConverter();

    private static UUID toUUID(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (bytes.length != 16) {
            throw new IllegalArgumentException();
        }
        long high = 0;
        for (int i = 0; i < 8; i++) {
            high = high * 256 + ((long) bytes[i] & 0xffL);
        }
        long low = 0;
        for (int i = 8; i < 16; i++) {
            low = low * 256 + ((long) bytes[i] & 0xffL);
        }
        return new UUID(high, low);
    }

    private static byte[] toBytes(UUID uuid) {
        if (uuid == null) return null;
        byte[] result = new byte[16];
        long high = uuid.getMostSignificantBits();
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (high & 0xffL);
            high >>= 8;
        }
        long low = uuid.getLeastSignificantBits();
        for (int i = 15; i >= 8; i--) {
            result[i] = (byte) (low & 0xffL);
            low >>= 8;
        }
        return result;
    }

    @Override
    public final void setObject(ParameterValues pv, int index, UUID value) {
        // TODO check: removed null option: pv.setNull(index, Types.VARBINARY);
        pv.setBytes(index, value == null ? null : toBytes(value));
    }

    @Override
    public UUID getObject(ResultRow row, int index) {
        byte[] bytes = row.getBytes(index);
        return bytes != null ? toUUID(bytes) : null;
    }

    @Override
    public void printSql(StringBuilder b, UUID value) {
        if (value == null) {
            b.append("null");
        } else {
            b.append("hextoraw(").append(toString(value)).append(")");
        }
    }
}
