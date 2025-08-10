package org.mariella.persistence.kotlin.internal

import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.Pool
import org.mariella.persistence.kotlin.*
import org.mariella.persistence.loader.ModifiableFactory
import org.mariella.persistence.mapping.SchemaMapping

internal class VertxDatabase(
    private val pool: Pool,
    private val schemaMapping: SchemaMapping,
    private val converterRegistry: ImmutableConverterRegistry,
    private val factory: ModifiableFactory,
    private val globalSequences: Map<String, CachedSequence>
) : Database {

    private suspend fun createConnectionAndTransaction(autoCommit: Boolean): VertxConnectionAndTransaction {
        val connection = pool.connection.coAwait()
        return if (autoCommit) {
            VertxConnectionAndTransaction(connection, null)
        } else {
            val transaction = connection.begin().coAwait()
            VertxConnectionAndTransaction(connection, transaction)
        }
    }

    override suspend fun connect(): TransactionalConnection {
        return TransactionConnectionImpl(
            createConnectionAndTransaction(false),
            schemaMapping,
            converterRegistry,
            factory,
            globalSequences
        )
    }

    override suspend fun connectReadOnly(): ReadOnlyConnection {
        return ReadOnlyConnectionImpl(
            createConnectionAndTransaction(false),
            schemaMapping,
            converterRegistry,
            factory
        )
    }

    override suspend fun connectAutoCommit(): AutoCommitConnection {
        return AutoCommitConnectionImpl(
            createConnectionAndTransaction(true),
            schemaMapping,
            converterRegistry,
            factory,
            globalSequences
        )
    }
}