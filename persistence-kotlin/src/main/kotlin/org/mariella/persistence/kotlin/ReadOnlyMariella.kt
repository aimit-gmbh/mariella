package org.mariella.persistence.kotlin

import io.vertx.sqlclient.SqlClient
import org.mariella.persistence.kotlin.internal.*
import org.mariella.persistence.loader.ClusterLoaderConditionProvider
import org.mariella.persistence.loader.LoaderContext
import org.mariella.persistence.loader.ModifiableFactory
import org.mariella.persistence.mapping.SchemaMapping
import org.mariella.persistence.persistor.ClusterDescription
import org.mariella.persistence.runtime.ModificationTracker
import java.util.*

open class ReadOnlyMariella internal constructor(
    protected val sqlClient: SqlClient,
    protected val modifiableFactory: ModifiableFactory,
    protected val schemaMapping: SchemaMapping,
    val mapper: Mapper,
    val tracker: SchemaAwareModificationTracker
) {
    fun createConditionProvider(data: Map<String, Any?>): ClusterLoaderConditionProvider {
        return LoadByConditionProvider(data)
    }

    suspend inline fun <reified T> load(
        vararg paths: String = arrayOf("root"),
        isUpdate: Boolean = false,
        conditionProvider: ClusterLoaderConditionProvider
    ): List<T> = load(paths = paths, isUpdate, conditionProvider, T::class.java)

    suspend fun <T> load(
        vararg paths: String = arrayOf("root"),
        isUpdate: Boolean = false,
        conditionProvider: ClusterLoaderConditionProvider,
        clazz: Class<T>
    ) = load(paths, tracker, isUpdate, conditionProvider, clazz)

    private suspend fun <T> load(
        paths: Array<out String>,
        modifications: ModificationTracker,
        isUpdate: Boolean,
        conditionProvider: ClusterLoaderConditionProvider,
        clazz: Class<T>
    ): List<T> {
        val classDesc = schemaMapping.schemaDescription.getClassDescription(clazz.name)
        val cd = ClusterDescription(classDesc, *paths)
        val clusterLoader = VertxClusterLoader<T>(schemaMapping, cd)
        val loaderContext = LoaderContext(modifications, modifiableFactory).apply { this.isUpdate = isUpdate }
        val ex = DatabaseException("loading cluster failed")
        return try {
            clusterLoader.load(sqlClient, loaderContext, conditionProvider)
        } catch (e: Exception) {
            throw ex.initCause(e)
        }
    }

    suspend inline fun <reified T> loadAll(vararg paths: String = arrayOf("root")): List<T> {
        return load(paths = paths, conditionProvider = ClusterLoaderConditionProvider.Default)
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
        val result = load(paths, tracker, isUpdate, LoadByIdProvider(id), clazz)
        return if (result.isEmpty()) null else if (result.size == 1) result[0] else error("expected exactly one or zero results but got ${result.size}")
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
}