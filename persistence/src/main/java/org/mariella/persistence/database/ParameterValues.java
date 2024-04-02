package org.mariella.persistence.database;

import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.UUID;

public interface ParameterValues {

    void setString(int pos, String value);

    void setNString(int pos, String value);

    void setBigDecimal(int pos, BigDecimal value);

    void setInteger(int pos, Integer value);

    void setBoolean(int pos, Boolean value);

    void setLong(int pos, Long value);

    void setDouble(int pos, Double value);

    void setBytes(int pos, byte[] value);

    void setUUID(int pos, UUID value);

    void setTimestamp(int pos, Timestamp value);

    void setDate(int pos, Date date);

    void setClob(int pos, StringReader value);

}
