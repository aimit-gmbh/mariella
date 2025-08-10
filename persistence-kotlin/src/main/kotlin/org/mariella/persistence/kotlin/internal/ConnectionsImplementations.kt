package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.kotlin.*
import org.mariella.persistence.loader.ModifiableFactory
import org.mariella.persistence.mapping.SchemaMapping
import org.mariella.persistence.runtime.RIListener

internal abstract class BaseConnection<T : ReadOnlyMariella>(
    val vertxConnectionAndTransaction: VertxConnectionAndTransaction,
    protected val schemaMapping: SchemaMapping,
    converterRegistry: ImmutableConverterRegistry,
    protected val modifiableFactory: ModifiableFactory
) : Connection<T> {
    protected val mapper = Mapper(vertxConnectionAndTransaction.sqlClient, converterRegistry)
    override val sqlClient = vertxConnectionAndTransaction.sqlClient

    override fun mapper(): Mapper {
        return mapper
    }

    override suspend fun close() {
        val ex = DatabaseException("close connection failed")
        try {
            vertxConnectionAndTransaction.close()
        } catch (e: Throwable) {
            throw ex.initCause(e)
        }
    }

    protected fun createModificationTracker(): SchemaAwareModificationTracker {
        return SchemaAwareModificationTracker(schemaMapping, modifiableFactory).apply {
            addPersistentListener(RIListener(this))
        }
    }
}

internal class ReadOnlyConnectionImpl internal constructor(
    vertxConnectionAndTransaction: VertxConnectionAndTransaction,
    schemaMapping: SchemaMapping,
    converterRegistry: ImmutableConverterRegistry,
    modifiableFactory: ModifiableFactory
) : BaseConnection<ReadOnlyMariella>(vertxConnectionAndTransaction, schemaMapping, converterRegistry, modifiableFactory), ReadOnlyConnection {
    override fun mariella() = ReadOnlyMariella(
        sqlClient,
        modifiableFactory,
        schemaMapping,
        mapper,
        createModificationTracker()
    )
}

internal class AutoCommitConnectionImpl internal constructor(
    vertxConnectionAndTransaction: VertxConnectionAndTransaction,
    schemaMapping: SchemaMapping,
    converterRegistry: ImmutableConverterRegistry,
    modifiableFactory: ModifiableFactory,
    private val globalSequences: Map<String, CachedSequence>
) : BaseConnection<Mariella>(vertxConnectionAndTransaction, schemaMapping, converterRegistry, modifiableFactory), AutoCommitConnection {
    override fun mariella() = Mariella(
        sqlClient,
        modifiableFactory,
        schemaMapping,
        mapper,
        createModificationTracker(),
        globalSequences
    )
}

internal class TransactionConnectionImpl internal constructor(
    vertxConnectionAndTransaction: VertxConnectionAndTransaction,
    schemaMapping: SchemaMapping,
    converterRegistry: ImmutableConverterRegistry,
    modifiableFactory: ModifiableFactory,
    private val globalSequences: Map<String, CachedSequence>
) : BaseConnection<Mariella>(vertxConnectionAndTransaction, schemaMapping, converterRegistry, modifiableFactory), TransactionalConnection {

    override fun mariella() = Mariella(
        sqlClient,
        modifiableFactory,
        schemaMapping,
        mapper,
        createModificationTracker(),
        globalSequences
    )

    override suspend fun commit() {
        val ex = DatabaseException("commit failed")
        try {
            vertxConnectionAndTransaction.commit()
        } catch (e: Throwable) {
            throw ex.initCause(e)
        }
    }

    override suspend fun commitAndClose() {
        val ex = DatabaseException("commit and close failed")
        try {
            vertxConnectionAndTransaction.commitAndClose()
        } catch (e: Throwable) {
            throw ex.initCause(e)
        }
    }

    override suspend fun rollbackAndClose() {
        val ex = DatabaseException("rollback and close failed")
        try {
            vertxConnectionAndTransaction.rollbackAndClose()
        } catch (e: Throwable) {
            throw ex.initCause(e)
        }
    }

    override suspend fun rollback() {
        val ex = DatabaseException("rollback failed")
        try {
            vertxConnectionAndTransaction.rollback()
        } catch (e: Throwable) {
            throw ex.initCause(e)
        }
    }
}