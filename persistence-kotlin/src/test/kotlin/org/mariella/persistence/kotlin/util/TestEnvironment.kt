package org.mariella.persistence.kotlin.util

import io.vertx.sqlclient.Pool
import org.mariella.persistence.kotlin.*
import org.mariella.persistence.kotlin.entities.ResourceType
import org.mariella.persistence.kotlin.entities.SecurityConcept
import org.mariella.persistence.kotlin.entities.SystemGroup
import org.mariella.persistence.kotlin.entities.UserRole
import org.mariella.persistence.mapping_builder.ConverterRegistryImpl.ConverterFactoryImpl
import org.mariella.persistence.oracle.OracleUUIDConverter
import java.sql.Types
import java.time.Instant
import java.util.*
import kotlin.uuid.Uuid

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
        if (databaseConfig.type == DatabaseType.ORACLE) listOf("org.mariella.persistence.kotlin.entities")
        else listOf("org.mariella.persistence.kotlin.entities", "org.mariella.persistence.kotlin.postgres"),
        databaseConfig.user,
        databaseConfig.password
    ) {
        converterRegistry.registerConverterFactory(
            Types.OTHER,
            Uuid::class.java,
            ConverterFactoryImpl(KotlinUuidConverter)
        )
        converterRegistry.registerConverterFactory(
            Types.BINARY,
            Uuid::class.java,
            ConverterFactoryImpl(KotlinUuidConverter)
        )
        converterRegistry.registerConverterFactory(
            Types.VARBINARY,
            Uuid::class.java,
            ConverterFactoryImpl(KotlinOracleUuidConverter)
        )
        converterRegistry.registerConverterFactory(
            Types.VARBINARY,
            UUID::class.java,
            ConverterFactoryImpl(OracleUUIDConverter.Singleton)
        )
        converterRegistry.registerConverterFactory(
            Types.TIMESTAMP_WITH_TIMEZONE,
            Instant::class.java,
            ConverterFactoryImpl(TimestampInstantConverter)
        )
        converterRegistry.registerConverterFactory(
            -101,
            kotlin.time.Instant::class.java,
            ConverterFactoryImpl(TimestampKotlinInstantConverter)
        )
        converterRegistry.registerConverterFactory(
            -101,
            Instant::class.java,
            ConverterFactoryImpl(TimestampInstantConverter)
        )
        converterRegistry.registerConverterFactory(
            Types.TIMESTAMP,
            Instant::class.java,
            ConverterFactoryImpl(TimestampInstantConverter)
        )
        converterRegistry.registerConverterFactory(
            Types.TIMESTAMP_WITH_TIMEZONE,
            kotlin.time.Instant::class.java,
            ConverterFactoryImpl(TimestampKotlinInstantConverter)
        )
        converterRegistry.registerConverterFactory(
            Types.TIMESTAMP,
            kotlin.time.Instant::class.java,
            ConverterFactoryImpl(TimestampKotlinInstantConverter)
        )
        registerIntMappedSealedClass(SecurityConcept::class)
        registerIntMappedSealedClass(SecurityConcept::class, 2)
        registerIntMappedSealedClass(SystemGroup::class)
        registerIntMappedSealedClass(SystemGroup::class, 2)
        registerStringMappedSealedClass(ResourceType::class)
        registerStringMappedSealedClass(UserRole::class)
    }
}
