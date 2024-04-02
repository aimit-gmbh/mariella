package org.mariella.persistence.database;

import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.UUID;

public interface ResultRow {

    String getString(int pos);

    String getNString(int pos);

    Boolean getBoolean(int pos);

    BigDecimal getBigDecimal(int pos);

    Integer getInteger(int pos);

    Long getLong(int pos);

    Double getDouble(int pos);

    byte[] getBytes(int pos);

    UUID getUUID(int pos);

    Timestamp getTimestamp(int pos);

    Date getDate(int pos);

    Clob getClob(int pos);
}
