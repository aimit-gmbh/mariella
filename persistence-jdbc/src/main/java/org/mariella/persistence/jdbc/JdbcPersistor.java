package org.mariella.persistence.jdbc;

import org.mariella.persistence.mapping.ClassMapping;
import org.mariella.persistence.mapping.ColumnMapping;
import org.mariella.persistence.mapping.PersistorStatement;
import org.mariella.persistence.mapping.SchemaMapping;
import org.mariella.persistence.persistor.AbstractPersistor;
import org.mariella.persistence.persistor.ConnectionDatabaseAccess;
import org.mariella.persistence.persistor.ObjectPersistor;
import org.mariella.persistence.persistor.PersistorStrategy;
import org.mariella.persistence.runtime.ModifiableAccessor;
import org.mariella.persistence.runtime.ModificationInfo;
import org.mariella.persistence.runtime.ModificationTracker;
import org.mariella.persistence.runtime.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class JdbcPersistor extends AbstractPersistor<JdbcPreparedPersistorStatement> {

    private static final Logger logger = LoggerFactory.getLogger(JdbcPersistor.class);
    
    private final Connection connection;

    public JdbcPersistor(SchemaMapping schemaMapping, PersistorStrategy<JdbcPreparedPersistorStatement> strategy, ModificationTracker modificationTracker, Connection connection) {
        super(schemaMapping, strategy, modificationTracker);
        this.connection = connection;
    }

    protected JdbcPreparedPersistorStatement createPreparedPersistorStatement(PersistorStatement statement, PreparedStatement ps) {
        return new JdbcPreparedPersistorStatement(this, statement, ps);
    }

    @Override
    public CompletableFuture<Void> generateKey(ModificationInfo modificationInfo) {
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        ClassMapping classMapping = getSchemaMapping().getClassMapping(modificationInfo.getObject().getClass().getName());
        for (ColumnMapping columnMapping : classMapping.getPersistorGeneratedColumnMappings()) {
            future.thenCompose(res -> {
                Object value = columnMapping.getValueGenerator().generate(new ConnectionDatabaseAccess(connection));
                ModifiableAccessor.Singleton.setValue(modificationInfo.getObject(), columnMapping.getPropertyDescription(), value);
                return CompletableFuture.completedFuture(null);
            });
        }
        return future;
    }

    @Override
    public JdbcPreparedPersistorStatement prepareStatement(PersistorStatement statement, String sql, String[] columnNames) {
        try {
            if (logger.isDebugEnabled())
                logger.debug("prepare with columns: " + sql);
            PreparedStatement ps = connection.prepareStatement(sql, columnNames);
            return createPreparedPersistorStatement(statement, ps);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public JdbcPreparedPersistorStatement prepareStatement(PersistorStatement statement, String sql) {
        try {
            if (logger.isDebugEnabled())
                logger.debug("prepare: " + sql);
            PreparedStatement ps = connection.prepareStatement(sql);
            return createPreparedPersistorStatement(statement, ps);
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public void persist() {
        PersistorStrategy.StrategyResult<JdbcPreparedPersistorStatement> result;

        // generate keys
        for (ModificationInfo info : new ArrayList<>(modificationTracker.getModifications())) {
            if (info.getStatus() == ModificationInfo.Status.New) {
                generateKey(info);
            }
        }

        // all primary statements -> ModificationInfo.status != New && != newRemoved
        strategy.begin();
        for (ModificationInfo info : new ArrayList<>(modificationTracker.getModifications())) {
            if (info.getStatus() != ModificationInfo.Status.NewRemoved) {
                ObjectPersistor<JdbcPreparedPersistorStatement> objectPersistor = new ObjectPersistor<>(this, info);

                result = strategy.beginObjectPersistor(objectPersistor);
                executeResult(result);
                objectPersistor.persistPrimary();
                result = strategy.endObjectPersistor();
                executeResult(result);
            }
        }
        result = strategy.end();
        executeResult(result);

        strategy.begin();
        for (ModificationInfo info : new ArrayList<>(modificationTracker.getModifications())) {
            if (info.getStatus() != ModificationInfo.Status.NewRemoved) {
                ObjectPersistor<JdbcPreparedPersistorStatement> objectPersistor = new ObjectPersistor<>(this, info);
                result = strategy.beginObjectPersistor(objectPersistor);
                executeResult(result);
                objectPersistor.persistSecondary();
                result = strategy.endObjectPersistor();
                executeResult(result);
                modificationTracker.flushed();
            }
        }
        result = strategy.end();
        executeResult(result);
    }

    private void executeResult(PersistorStrategy.StrategyResult<JdbcPreparedPersistorStatement> result) {
        if (result != null) {
            for (JdbcPreparedPersistorStatement statement : result.statements) {
                statement.executeBatch();
            }
            result.callback.run();
        }
    }
}
