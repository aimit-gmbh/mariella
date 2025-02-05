package org.mariella.test.util

import io.vertx.sqlclient.Pool
import org.mariella.persistence.kotlin.*
import org.mariella.persistence.mapping_builder.ConverterRegistryImpl
import org.mariella.test.entities.ResourceType
import org.mariella.test.entities.SecurityConcept
import org.mariella.test.entities.SystemGroup
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
        map: Map<String, ThreadSafeCachedSequence> = emptyMap()
    ) = MariellaFactory.createDatabase(mariella, pool, map)

    fun createMariella(databaseConfig: DatabaseConfig) = MariellaFactory.createMariellaMapping(
        databaseConfig.getUrl(),
        listOf("org.mariella.test.entities"),
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
    }
}
