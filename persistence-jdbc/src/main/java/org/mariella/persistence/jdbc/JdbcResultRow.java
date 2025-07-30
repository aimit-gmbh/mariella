package org.mariella.persistence.jdbc;

import jakarta.persistence.PersistenceException;
import org.mariella.persistence.database.ResultRow;

import java.math.BigDecimal;
import java.sql.*;
import java.util.UUID;

public class JdbcResultRow implements ResultRow {
    private final ResultSet resultSet;

    public JdbcResultRow(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public String getString(int pos) {
        try {
            return resultSet.getString(pos);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public String getNString(int pos) {
        try {
            return resultSet.getNString(pos);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Boolean getBoolean(int pos) {
        try {
            return resultSet.getBoolean(pos);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public BigDecimal getBigDecimal(int pos) {
        try {
            return resultSet.getBigDecimal(pos);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Integer getInteger(int pos) {
        try {
            int i = resultSet.getInt(pos);
            return resultSet.wasNull() ? null : i;
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Long getLong(int pos) {
        try {
            long l = resultSet.getLong(pos);
            return resultSet.wasNull() ? null : l;
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Double getDouble(int pos) {
        try {
            double d = resultSet.getDouble(pos);
            return resultSet.wasNull() ? null : d;
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public byte[] getBytes(int pos) {
        try {
            return resultSet.getBytes(pos);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public UUID getUUID(int pos) {
        try {
            return (UUID) resultSet.getObject(pos);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Timestamp getTimestamp(int pos) {
        try {
            return resultSet.getTimestamp(pos);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Date getDate(int pos) {
        try {
            return resultSet.getDate(pos);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Clob getClob(int pos) {
        try {
            return resultSet.getClob(pos);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

}
