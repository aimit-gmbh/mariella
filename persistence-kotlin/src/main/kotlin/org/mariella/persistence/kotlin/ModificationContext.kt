package org.mariella.persistence.kotlin

import org.mariella.persistence.kotlin.internal.*
import org.mariella.persistence.loader.ClusterLoaderConditionProvider
import org.mariella.persistence.loader.LoaderContext
import org.mariella.persistence.persistor.BatchingPersistorStrategy
import org.mariella.persistence.persistor.ClusterDescription
import org.mariella.persistence.runtime.ModificationTracker
import java.util.*

class ModificationContext internal constructor(
    private val databaseSession: DatabaseSession,
    val mapper: Mapper,
    val tracker: SchemaAwareModificationTracker
) {
    suspend inline fun <reified T> modify(id: UUID, block: (T) -> Unit): T {
        val entity = loadEntity(id, isUpdate = false, clazz = T::class.java)
            ?: error("entity with id $id and type ${T::class.java} does not exist")
        return entity.apply(block)
    }

    suspend inline fun <reified T> updateOne(id: UUID, block: (T) -> Unit): T {
        return modify(id) {
            block(it)
        }
    }

    inline fun <reified T> create(block: ModificationContext.(T) -> Unit = {}): T {
        val entity = tracker.createNew<T>()
        block(entity)
        return entity
    }

    inline fun <reified T> create(): T {
        return tracker.createNew()
    }

    inline fun <reified T> addExisting(id: UUID): T {
        return tracker.addExisting(id)
    }

    inline fun <reified T> addExisting(id: UUID, discriminator: String): T {
        return tracker.addExisting(id, discriminator)
    }

    inline fun <reified T> delete(id: UUID) {
        val entity = tracker.addExisting<T>(id)
        tracker.remove(entity)
    }

    suspend fun flush() {
        if (tracker.isDirty) {
            val ex = DatabaseException("persisting failed")
            try {
                VertxPersistor(
                    databaseSession.sqlClient,
                    databaseSession.schemaMapping,
                    BatchingPersistorStrategy<VertxPreparedPersistorStatement>(),
                    tracker
                ).persist()
            } catch (e: Exception) {
                throw ex.initCause(e)
            }
        }
    }

    private suspend fun <T> load(
        paths: Array<out String>,
        modifications: ModificationTracker,
        isUpdate: Boolean,
        conditionProvider: ClusterLoaderConditionProvider,
        clazz: Class<T>
    ): List<T> {
        val classDesc = databaseSession.schemaMapping.schemaDescription.getClassDescription(clazz.name)
        val cd = ClusterDescription(classDesc, *paths)
        val clusterLoader = VertxClusterLoader<T>(databaseSession.schemaMapping, cd)
        val loaderContext = LoaderContext(modifications, databaseSession.modifiableFactory).apply { this.isUpdate = isUpdate }
        val ex = DatabaseException("loading cluster failed")
        return try {
            clusterLoader.load(databaseSession.sqlClient, loaderContext, conditionProvider)
        } catch (e: Exception) {
            throw ex.initCause(e)
        }
    }

    suspend inline fun <reified T> loadEntity(
        id: UUID,
        vararg paths: String = arrayOf("root"),
        isUpdate: Boolean = false
    ): T? {
        return loadEntity(id, paths = paths, isUpdate, T::class.java)
    }

    suspend fun <T> loadEntity(
        id: UUID,
        vararg paths: String = arrayOf("root"),
        isUpdate: Boolean = false,
        clazz: Class<T>
    ): T? {
        return load(paths, tracker, isUpdate, LoadByIdProvider(id), clazz).singleOrNull()
    }

    suspend inline fun <reified T> loadEntities(
        ids: Collection<UUID>,
        vararg paths: String = arrayOf("root"),
        isUpdate: Boolean = false,
    ): List<T> {
        return loadEntities(ids, paths = paths, isUpdate, T::class.java)
    }

    suspend fun <T> loadEntities(
        ids: Collection<UUID>,
        vararg paths: String = arrayOf("root"),
        isUpdate: Boolean = false,
        clazz: Class<T>
    ): List<T> {
        return load(paths, tracker, isUpdate, InClusterLoaderConditionProvider(ids), clazz)
    }

    suspend fun sequenceNextValue(name: String): Long {
        return databaseSession.sequenceNextValue(name)
    }

    suspend fun cachedSequenceNextValue(name: String): Long {
        return databaseSession.cachedSequenceNextValue(name)
    }

}
