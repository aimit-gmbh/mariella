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
        vertxConnectionAndTransaction.close()
    }

    suspend fun commit() {
        vertxConnectionAndTransaction.commit()
    }

    suspend fun commitAndClose() {
        vertxConnectionAndTransaction.commitAndClose()
    }

    suspend fun rollbackAndClose() {
        vertxConnectionAndTransaction.rollbackAndClose()
    }

    suspend fun rollback() {
        vertxConnectionAndTransaction.rollback()
    }
}