package org.mariella.persistence.kotlin.util

import io.vertx.sqlclient.Pool
import org.mariella.persistence.kotlin.*
import org.mariella.persistence.kotlin.entities.ResourceType
import org.mariella.persistence.kotlin.entities.SecurityConcept
import org.mariella.persistence.kotlin.entities.SystemGroup
import org.mariella.persistence.kotlin.entities.UserRole
import org.mariella.persistence.mapping_builder.ConverterRegistryImpl
import java.sql.Types
import java.time.Instant

object TestEnvironment {

    fun createDatabase(pool: Pool, databaseConfig: DatabaseConfig): Database {
        val mariella = createMariella(databaseConfig)
        return createDatabase(mariella, pool)
    }

    fun createDatabase(
        mariella: MariellaMapping,
        pool: Pool,
        map: Map<String, CachedSequence> = emptyMap()
    ) = VertxDatabaseFactory.createDatabase(mariella, pool, map)

    fun createMariella(databaseConfig: DatabaseConfig) = VertxDatabaseFactory.createMariellaMapping(
        databaseConfig.getUrl(),
        listOf("org.mariella.persistence.kotlin.entities"),
        databaseConfig.user,
        databaseConfig.password
    ) {
        converterRegistry.registerConverterFactory(
            Types.TIMESTAMP_WITH_TIMEZONE,
            Instant::class.java,
            ConverterRegistryImpl.ConverterFactoryImpl(TimestampInstantConverter)
        )
        converterRegistry.registerConverterFactory(
            Types.TIMESTAMP, Instant::class.java, ConverterRegistryImpl.ConverterFactoryImpl(TimestampInstantConverter)
        )
        registerIntMappedSealedClass(SecurityConcept::class)
        registerIntMappedSealedClass(SystemGroup::class)
        registerStringMappedSealedClass(ResourceType::class)
        registerStringMappedSealedClass(UserRole::class)
    }
}
