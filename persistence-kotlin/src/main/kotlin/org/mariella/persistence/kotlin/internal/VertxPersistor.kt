package org.mariella.persistence.kotlin.internal

import io.vertx.sqlclient.SqlClient
import org.mariella.persistence.kotlin.DatabaseException
import org.mariella.persistence.mapping.PersistorStatement
import org.mariella.persistence.mapping.SchemaMapping
import org.mariella.persistence.persistor.BatchingPersistorStrategy
import org.mariella.persistence.persistor.ObjectPersistor
import org.mariella.persistence.persistor.Persistor
import org.mariella.persistence.persistor.PersistorStrategy
import org.mariella.persistence.runtime.ModificationInfo
import org.mariella.persistence.runtime.ModificationTracker
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

internal class VertxPersistor(
    private val sqlClient: SqlClient,
    private val schemaMapping: SchemaMapping,
    private val strategy: BatchingPersistorStrategy<VertxPreparedPersistorStatement>,
    private val modificationTracker: ModificationTracker
) : Persistor<VertxPreparedPersistorStatement> {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(VertxPersistor::class.java)
    }

    suspend fun persist() {
        // all primary statements -> ModificationInfo.status != New && != newRemoved
        persistStatements { it.persistPrimary() }
        persistStatements { it.persistSecondary() }
        modificationTracker.flushed()
    }

    private suspend fun persistStatements(createStatements: (ObjectPersistor<VertxPreparedPersistorStatement>) -> Unit) {
        strategy.begin()
        for (info in ArrayList(modificationTracker.modifications)) {
            if (info.status != ModificationInfo.Status.NewRemoved) {
                val objectPersistor = ObjectPersistor(this, info)
                processResult(strategy.beginObjectPersistor(objectPersistor))
                createStatements(objectPersistor)
                processResult(strategy.endObjectPersistor())
            }
        }
        processResult(strategy.end())
    }

    private suspend fun processResult(result: PersistorStrategy.StrategyResult<VertxPreparedPersistorStatement>?) {
        if (result != null) {
            for (statement in result.statements) {
                statement.executeBatch()
            }
            result.callback.run()
        }
    }

    override fun getSchemaMapping(): SchemaMapping {
        return schemaMapping
    }

    override fun getStrategy(): PersistorStrategy<VertxPreparedPersistorStatement> {
        return strategy
    }

    override fun generateKey(modificationInfo: ModificationInfo): CompletableFuture<Void> {
        throw UnsupportedOperationException("framework generated keys are not supported for vert.x")
    }

    override fun prepareStatement(
        statement: PersistorStatement,
        sql: String,
        columnNames: Array<String>
    ): VertxPreparedPersistorStatement {
        if (sqlClient::class.simpleName != "PgConnectionImpl")
            throw DatabaseException("prepared statements returning auto generated IDs only implemented for postgres")
        val rewrittenStatement = sql + " RETURNING " + columnNames.joinToString { it }
        @Suppress("SqlSourceToSinkFlow") val preparedQuery = sqlClient.preparedQuery(rewrittenStatement)
        return VertxPreparedPersistorStatement(statement, preparedQuery)
    }

    override fun prepareStatement(statement: PersistorStatement, sql: String): VertxPreparedPersistorStatement {
        if (logger.isTraceEnabled)
            logger.trace("prepare: $sql")
        @Suppress("SqlSourceToSinkFlow") val preparedQuery = sqlClient.preparedQuery(sql)
        return VertxPreparedPersistorStatement(statement, preparedQuery)
    }
}