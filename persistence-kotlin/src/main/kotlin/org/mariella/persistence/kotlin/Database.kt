package org.mariella.persistence.kotlin

import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.Pool
import org.mariella.persistence.kotlin.internal.ImmutableConverterRegistry
import org.mariella.persistence.kotlin.internal.VertxConnectionAndTransaction
import org.mariella.persistence.loader.ModifiableFactory
import org.mariella.persistence.mapping.SchemaMapping

class Database internal constructor(
    private val pool: Pool,
    private val schemaMapping: SchemaMapping,
    private val converterRegistry: ImmutableConverterRegistry,
    private val factory: ModifiableFactory,
    private val globalSequences: Map<String, ThreadSafeCachedSequence>
) {

    private suspend fun createVertxSession(autoCommit: Boolean): VertxConnectionAndTransaction {
        val connection = pool.connection.coAwait()
        return if (autoCommit) {
            VertxConnectionAndTransaction(connection, null)
        } else {
            val transaction = connection.begin().coAwait()
            VertxConnectionAndTransaction(connection, transaction)
        }
    }

    suspend fun createSession(autoCommit: Boolean = false): DatabaseSession {
        return DatabaseSession(
            createVertxSession(autoCommit),
            schemaMapping,
            converterRegistry,
            factory,
            globalSequences
        )
    }
}