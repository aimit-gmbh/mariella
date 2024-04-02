package org.mariella.persistence.jdbc;

import org.mariella.persistence.database.ParameterValues;

import javax.persistence.PersistenceException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.UUID;

public class JdbcParameterValues implements ParameterValues {
    private final PreparedStatement ps;

    public JdbcParameterValues(PreparedStatement ps) {
        this.ps = ps;
    }

    @Override
    public void setString(int pos, String value) {
        try {
            ps.setString(pos, value);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void setNString(int pos, String value) {
        try {
            ps.setNString(pos, value);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void setBigDecimal(int pos, BigDecimal value) {
        try {
            ps.setBigDecimal(pos, value);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void setInteger(int pos, Integer value) {
        try {
            if (value == null) {
                ps.setNull(pos, Types.INTEGER);
            } else {
                ps.setInt(pos, value);
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void setBoolean(int pos, Boolean value) {
        try {
            if (value == null) {
                ps.setNull(pos, Types.BOOLEAN);
            } else {
                ps.setBoolean(pos, value);
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void setLong(int pos, Long value) {
        try {
            if (value == null) {
                ps.setNull(pos, Types.BIGINT);
            } else {
                ps.setLong(pos, value);
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void setDouble(int pos, Double value) {
        try {
            if (value == null) {
                ps.setNull(pos, Types.DOUBLE);
            } else {
                ps.setDouble(pos, value);
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void setBytes(int pos, byte[] value) {
        try {
            ps.setBytes(pos, value);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void setUUID(int pos, UUID value) {
        try {
            ps.setObject(pos, value);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void setTimestamp(int pos, Timestamp value) {
        try {
            ps.setTimestamp(pos, value);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void setDate(int pos, Date value) {
        try {
            ps.setDate(pos, value);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void setClob(int pos, StringReader value) {
        try {
            ps.setClob(pos, value);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

}
