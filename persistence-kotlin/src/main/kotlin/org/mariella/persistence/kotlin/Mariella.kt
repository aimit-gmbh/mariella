package org.mariella.persistence.kotlin

import io.vertx.sqlclient.SqlClient
import org.mariella.persistence.kotlin.internal.SchemaAwareModificationTracker
import org.mariella.persistence.kotlin.internal.VertxPersistor
import org.mariella.persistence.loader.ModifiableFactory
import org.mariella.persistence.mapping.SchemaMapping
import org.mariella.persistence.persistor.BatchingPersistorStrategy

class Mariella internal constructor(
    sqlClient: SqlClient,
    modifiableFactory: ModifiableFactory,
    schemaMapping: SchemaMapping,
    mapper: Mapper,
    tracker: SchemaAwareModificationTracker,
    private val globalSequences: Map<String, CachedSequence>
) : ReadOnlyMariella(sqlClient, modifiableFactory, schemaMapping, mapper, tracker) {

    suspend inline fun <reified T> modify(
        id: Any,
        isUpdate: Boolean = false,
        vararg paths: String = arrayOf(),
        block: (T) -> Unit
    ): T {
        val entity = loadEntity<T>(id, isUpdate = isUpdate, paths = paths)
            ?: error("entity with id $id and type ${T::class.java} does not exist")
        return entity.apply(block)
    }

    inline fun <reified T> create(block: Mariella.(T) -> Unit): T {
        val entity = tracker.createNew<T>()
        block(entity)
        return entity
    }

    inline fun <reified T> create(): T {
        return tracker.createNew()
    }

    inline fun <reified T> addExisting(id: Any): T {
        return tracker.addExisting(id)
    }

    inline fun <reified T> addExisting(id: Any, discriminator: String): T {
        return tracker.addExisting(id, discriminator)
    }

    inline fun <reified T> delete(id: Any) {
        val entity = tracker.addExisting<T>(id)
        tracker.remove(entity)
    }

    suspend fun flush() {
        if (tracker.isDirty) {
            val ex = DatabaseException("persisting failed")
            try {
                VertxPersistor(
                    sqlClient,
                    schemaMapping,
                    BatchingPersistorStrategy(),
                    tracker
                ).persist()
            } catch (e: Exception) {
                throw ex.initCause(e)
            }
        }
    }

    suspend fun sequenceNextValue(name: String): Long {
        return mapper.selectOneExistingPrimitive("select NEXTVAL('$name')")
    }

    suspend fun cachedSequenceNextValue(name: String): Long {
        return globalSequences.getValue(name).next(mapper)
    }

}
