package org.mariella.persistence.kotlin

import org.mariella.persistence.kotlin.internal.ImmutableConverterRegistry
import org.mariella.persistence.kotlin.internal.SchemaAwareModificationTracker
import org.mariella.persistence.kotlin.internal.VertxConnectionAndTransaction
import org.mariella.persistence.loader.ModifiableFactory
import org.mariella.persistence.mapping.SchemaMapping
import org.mariella.persistence.runtime.RIListener

class DatabaseSession internal constructor(
    private val vertxConnectionAndTransaction: VertxConnectionAndTransaction,
    internal val schemaMapping: SchemaMapping,
    converterRegistry: ImmutableConverterRegistry,
    internal val modifiableFactory: ModifiableFactory,
    private val globalSequences: Map<String, ThreadSafeCachedSequence>
) {
    val mapper = Mapper(vertxConnectionAndTransaction.sqlClient, converterRegistry)
    val sqlClient = vertxConnectionAndTransaction.sqlClient

    internal suspend fun sequenceNextValue(name: String): Long {
        return mapper.selectOneExistingPrimitive("select NEXTVAL('$name')")
    }

    internal suspend fun cachedSequenceNextValue(name: String): Long {
        return globalSequences.getValue(name).next(mapper)
    }

    fun modify() = ModificationContext(
        this,
        mapper,
        createModificationTracker()
    )

    private fun createModificationTracker(): SchemaAwareModificationTracker {
        return SchemaAwareModificationTracker(schemaMapping, modifiableFactory).apply {
            addPersistentListener(RIListener(this))
        }
    }

    suspend fun close() {
        val ex = DatabaseException("close connection failed")
        try {
            vertxConnectionAndTransaction.close()
        } catch (e: Throwable) {
            throw ex.initCause(e)
        }
    }

    suspend fun commit() {
        val ex = DatabaseException("commit failed")
        try {
            vertxConnectionAndTransaction.commit()
        } catch (e: Throwable) {
            throw ex.initCause(e)
        }
    }

    suspend fun commitAndClose() {
        val ex = DatabaseException("commit and close failed")
        try {
            vertxConnectionAndTransaction.commitAndClose()
        } catch (e: Throwable) {
            throw ex.initCause(e)
        }
    }

    suspend fun rollbackAndClose() {
        val ex = DatabaseException("rollback and close failed")
        try {
            vertxConnectionAndTransaction.rollbackAndClose()
        } catch (e: Throwable) {
            throw ex.initCause(e)
        }
    }

    suspend fun rollback() {
        val ex = DatabaseException("rollback failed")
        try {
            vertxConnectionAndTransaction.rollback()
        } catch (e: Throwable) {
            throw ex.initCause(e)
        }
    }
}