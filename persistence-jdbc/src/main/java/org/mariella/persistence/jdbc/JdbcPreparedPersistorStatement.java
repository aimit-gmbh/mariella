package org.mariella.persistence.jdbc;

import org.mariella.persistence.database.AbstractPreparedPersistorStatement;
import org.mariella.persistence.database.ParameterValues;
import org.mariella.persistence.mapping.PersistorStatement;
import org.mariella.persistence.mapping.RowAndObject;
import org.mariella.persistence.persistor.Row;
import org.mariella.persistence.runtime.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public class JdbcPreparedPersistorStatement extends AbstractPreparedPersistorStatement implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(JdbcPreparedPersistorStatement.class);
    
    protected final JdbcPersistor persistor;
    protected final PreparedStatement preparedStatement;

    public JdbcPreparedPersistorStatement(JdbcPersistor persistor, PersistorStatement statement, PreparedStatement preparedStatement) {
        super(statement);
        this.persistor = persistor;
        this.preparedStatement = preparedStatement;
    }

    protected void execute(Row row) throws SQLException {
        try {
            if (logger.isDebugEnabled())
                logger.debug("execute: " + statement.getSqlDebugString(row));
            ParameterValues parameterValues = new JdbcParameterValues(preparedStatement);
            statement.setParameters(parameterValues, row);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to execute statement: " + statement.getSqlDebugString(row), e);
            throw e;
        }
    }

    protected void execute(Row row, Consumer<RowAndObject> generatedColumnNamesCallback, Object entity) {
        try {
            if (logger.isDebugEnabled())
                logger.debug("execute with callback: " + statement.getSqlDebugString(row));
            ParameterValues parameterValues = new JdbcParameterValues(preparedStatement);
            statement.setParameters(parameterValues, row);
            preparedStatement.executeQuery();

            try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                rs.next();
                JdbcResultRow rr = new JdbcResultRow(rs);
                generatedColumnNamesCallback.accept(new RowAndObject(rr, entity));
            }
        } catch (SQLException e) {
            logger.error("Failed to execute statement: " + statement.getSqlDebugString(row), e);
            throw new PersistenceException(e);
        }
    }

    @Override
    public void addBatch(Row row, Object entity) {
        if (logger.isDebugEnabled())
            logger.debug("addBatch: {}", statement.getSqlDebugString(row));

        ParameterValues parameterValues = new JdbcParameterValues(preparedStatement);
        statement.setParameters(parameterValues, row);

        Consumer<RowAndObject> consumer = statement.getGeneratedColumnsCallback();
        if (consumer == null) {
            try {
                persistor.getSchemaMapping().getSchema().addBatch(preparedStatement);
            } catch (SQLException e) {
                logger.error("Failed to add to batch: " + statement.getSqlDebugString(row), e);
                throw new PersistenceException(e);
            }
        } else {
            execute(row, consumer, entity);
        }
    }

    public void executeBatch() {
        try {
            if (logger.isDebugEnabled())
                logger.debug("executeBatch: {}", statement.getSqlString());
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            logger.error("Failed to execute batch statement: " + statement.getSqlString(), e);
            throw new PersistenceException(e);
        }
    }

    @Override
    public void close() {
        try {
            preparedStatement.close();
        } catch (SQLException e) {
            logger.error("Failed to close statement: " + statement.getSqlString(), e);
            throw new PersistenceException(e);
        }
    }
}
