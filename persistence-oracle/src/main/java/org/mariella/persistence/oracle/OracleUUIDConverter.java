package org.mariella.persistence.oracle;

import org.mariella.persistence.database.BaseUUIDConverter;
import org.mariella.persistence.database.ParameterValues;
import org.mariella.persistence.database.ResultRow;
import org.mariella.persistence.util.UUIDUtils;

import java.util.UUID;

public class OracleUUIDConverter extends BaseUUIDConverter {
    public static final OracleUUIDConverter Singleton = new OracleUUIDConverter();

    @Override
    public final void setObject(ParameterValues pv, int index, UUID value) {
        // TODO check: removed null option: pv.setNull(index, Types.VARBINARY);
        pv.setBytes(index, value == null ? null : UUIDUtils.toBytes(value));
    }

    @Override
    public UUID getObject(ResultRow row, int index) {
        byte[] bytes = row.getBytes(index);
        return bytes != null ? UUIDUtils.toUUID(bytes) : null;
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
