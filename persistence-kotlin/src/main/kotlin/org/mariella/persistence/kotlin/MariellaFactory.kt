package org.mariella.persistence.kotlin

import io.vertx.sqlclient.Pool
import org.mariella.persistence.annotations_processing.UnitInfoBuilder
import org.mariella.persistence.bootstrap.DefaultClassResolver
import org.mariella.persistence.bootstrap.Environment.*
import org.mariella.persistence.bootstrap.J2SEConnectionProvider
import org.mariella.persistence.database.IndexedParameter
import org.mariella.persistence.database.JdbcParameter
import org.mariella.persistence.generic.GenericPersistenceBuilder
import org.mariella.persistence.kotlin.internal.*
import org.mariella.persistence.loader.ModifiableFactory
import org.mariella.persistence.loader.ModifiableFactoryImpl
import org.mariella.persistence.mapping.SchemaMapping
import org.mariella.persistence.mapping.UnitInfo
import org.mariella.persistence.mapping_builder.ConverterRegistryImpl
import org.mariella.persistence.mapping_builder.DatabaseInfoProvider
import org.mariella.persistence.mapping_builder.DatabaseMetaDataDatabaseInfoProvider
import org.mariella.persistence.mapping_builder.PersistenceBuilder
import java.sql.Types
import kotlin.reflect.KClass

data class MariellaMapping(
    val schemaMapping: SchemaMapping,
    val converterRegistry: ImmutableConverterRegistry
)

object MariellaFactory {
    fun createMariellaMapping(
        jdbcUrl: String,
        entityPackages: List<String>,
        user: String,
        password: String,
        initPersistenceBuilder: PersistenceBuilder.() -> Unit = {}
    ): MariellaMapping {
        val unitInfo = createUnitInfo(jdbcUrl, entityPackages)
        return createSchemaMapping(user, password, unitInfo, jdbcUrl, initPersistenceBuilder)
    }

    fun createDatabase(
        mariella: MariellaMapping,
        pool: Pool,
        globalSequences: Map<String, ThreadSafeCachedSequence> = emptyMap(),
        modifiableFactory: ModifiableFactory = ModifiableFactoryImpl()
    ): Database {
        return Database(
            pool,
            mariella.schemaMapping,
            mariella.converterRegistry,
            modifiableFactory,
            globalSequences
        )
    }

    private fun createSchemaMapping(
        user: String,
        password: String,
        unitInfo: UnitInfo,
        jdbcUrl: String,
        initPersistenceBuilder: PersistenceBuilder.() -> Unit,
    ): MariellaMapping {
        val connectionProvider = J2SEConnectionProvider(jdbcUrl, user, password)
        val persistenceBuilder = try {
            val databaseInfoProvider = createDatabaseInfoProvider(connectionProvider, unitInfo)
            val persistenceBuilder = createPersistenceBuilder(
                DefaultClassResolver(javaClass.classLoader),
                databaseInfoProvider,
                unitInfo,
                initPersistenceBuilder
            )
            persistenceBuilder.build()
            persistenceBuilder
        } finally {
            connectionProvider.close()
        }

        val converterRegistry = ImmutableConverterRegistry(persistenceBuilder.converterRegistry)
        val s = persistenceBuilder.persistenceInfo.schemaMapping
        s.schemaDescription.schemaName = unitInfo.persistenceUnitName
        val parameterStyle = unitInfo.properties.getProperty(PARAMETER_STYLE, "jdbc")
        if (parameterStyle == PARAMETER_STYLE_JDBC) {
            s.schema.parameterClass = JdbcParameter::class.java
        } else if (parameterStyle == PARAMETER_STYLE_INDEXED) {
            s.schema.parameterClass = IndexedParameter::class.java
        }
        return MariellaMapping(s, converterRegistry)
    }

    private fun createDatabaseInfoProvider(
        connectionProvider: J2SEConnectionProvider,
        unitInfo: UnitInfo
    ): DatabaseMetaDataDatabaseInfoProvider {
        val databaseInfoProvider = DatabaseMetaDataDatabaseInfoProvider(connectionProvider.connection.metaData)
        val ignoreSchema = getBooleanProperty(IGNORE_DB_SCHEMA, unitInfo)
        if (ignoreSchema != null) {
            databaseInfoProvider.isIgnoreSchema = ignoreSchema
        }
        val usernameAsSchema = getBooleanProperty(USERNAME_AS_DB_SCHEMA, unitInfo)
        if (usernameAsSchema != null) {
            databaseInfoProvider.isUsernameAsSchema = usernameAsSchema
        }
        val ignoreCatalog = getBooleanProperty(IGNORE_DB_CATALOG, unitInfo)
        if (ignoreCatalog != null) {
            databaseInfoProvider.isIgnoreCatalog = ignoreCatalog
        }
        return databaseInfoProvider
    }

    private fun createPersistenceBuilder(
        persistenceClassResolver: DefaultClassResolver,
        databaseInfoProvider: DatabaseMetaDataDatabaseInfoProvider,
        unitInfo: UnitInfo,
        initPersistenceBuilder: PersistenceBuilder.() -> Unit
    ): PersistenceBuilder {
        val persistenceBuilderClassName = getStringProperty(PERSISTENCE_BUILDER, unitInfo)

        val persistenceBuilder: PersistenceBuilder = if (persistenceBuilderClassName != null) {
            val persistenceBuilderClass = persistenceClassResolver.resolveClass(persistenceBuilderClassName)
            val constructor = persistenceBuilderClass.getConstructor(
                UnitInfo::class.java,
                DatabaseInfoProvider::class.java
            )
            constructor.newInstance(unitInfo, databaseInfoProvider) as PersistenceBuilder
        } else {
            GenericPersistenceBuilder(unitInfo, databaseInfoProvider)
        }
        initPersistenceBuilder(persistenceBuilder)
        return persistenceBuilder
    }

    private fun createUnitInfo(jdbcUrl: String, entityPackages: List<String>): UnitInfo {
        val persistenceXml = PersistenceXmlGenerator().getXml(jdbcUrl, entityPackages)
        val parser = VertxPersistenceUnitParser(
            javaClass.classLoader,
            persistenceXml
        )
        val builder = UnitInfoBuilder(parser)
        builder.build()
        return builder.unitInfos.single { it.persistenceUnitName == PersistenceXmlGenerator.NAME }
    }

    private fun getBooleanProperty(propertyName: String?, unitInfo: UnitInfo): Boolean? {
        return getBooleanProperty(unitInfo.properties, propertyName, unitInfo)
    }

    private fun getStringProperty(propertyName: String?, unitInfo: UnitInfo): String? {
        return getStringProperty(unitInfo.properties, propertyName)
    }

    private fun getStringProperty(properties: Map<*, *>, propertyName: String?): String? {
        return properties[propertyName] as String?
    }

    private fun getBooleanProperty(properties: Map<*, *>, propertyName: String?, unitInfo: UnitInfo): Boolean? {
        return if (properties[propertyName] == null) {
            null
        } else if (properties[propertyName] == "true") {
            true
        } else if (properties[propertyName] == "false") {
            false
        } else {
            throw java.lang.RuntimeException(
                "Invalid value for boolean property + '" + unitInfo.properties.getProperty(propertyName) + "'."
            )
        }
    }
}

fun <T : StringMappedSealedClass> PersistenceBuilder.registerStringMappedSealedClass(
    kClass: KClass<T>
) {
    converterRegistry.registerConverterFactory(
        Types.VARCHAR,
        kClass.java,
        ConverterRegistryImpl.ConverterFactoryImpl(StringSealedClassConverter(kClass))
    )
    converterRegistry.registerConverterFactory(
        Types.LONGVARCHAR,
        kClass.java,
        ConverterRegistryImpl.ConverterFactoryImpl(StringSealedClassConverter(kClass))
    )
    converterRegistry.registerConverterFactory(
        Types.CHAR,
        kClass.java,
        ConverterRegistryImpl.ConverterFactoryImpl(StringSealedClassConverter(kClass))
    )
}

fun <T : IntegerMappedSealedClass> PersistenceBuilder.registerIntMappedSealedClass(
    kClass: KClass<T>
) {
    converterRegistry.registerConverterFactory(
        Types.INTEGER,
        kClass.java,
        ConverterRegistryImpl.ConverterFactoryImpl(IntSealedClassConverter(kClass))
    )
}