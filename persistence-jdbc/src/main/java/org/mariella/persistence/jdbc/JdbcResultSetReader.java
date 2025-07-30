package org.mariella.persistence.jdbc;

import jakarta.persistence.PersistenceException;
import org.mariella.persistence.database.ResultRow;
import org.mariella.persistence.database.ResultSetReader;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcResultSetReader implements ResultSetReader {
    private final ResultSet resultSet;
    private final JdbcResultRow resultRow;
    private int currentIndex;

    public JdbcResultSetReader(ResultSet resultSet) {
        this.resultSet = resultSet;
        resultRow = new JdbcResultRow(resultSet);
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    @Override
    public boolean next() {
        try {
            return resultSet.next();
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public ResultRow getResultRow() {
        return resultRow;
    }

    public int getCurrentColumnIndex() {
        return currentIndex;
    }

    public void setCurrentColumnIndex(int index) {
        currentIndex = index;
    }

}
