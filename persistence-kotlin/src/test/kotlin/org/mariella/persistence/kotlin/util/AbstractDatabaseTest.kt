package org.mariella.persistence.kotlin.util

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.vertx.core.Vertx
import io.vertx.jdbcclient.JDBCPool
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.pgclient.PgBuilder
import io.vertx.pgclient.PgConnectOptions
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PoolOptions
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mariella.persistence.kotlin.Database
import org.mariella.persistence.kotlin.DatabaseSession
import org.mariella.persistence.kotlin.MariellaMapping
import org.mariella.persistence.kotlin.entities.*
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.Instant

val DATABASE_TYPE = if (System.getenv("MARIELLA_TEST_DB") != null) DatabaseType.valueOf(System.getenv("MARIELLA_TEST_DB")) else DatabaseType.H2_MEM

/**
 * to run the tests start postgres with
 * docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres
 * and set DATABASE_TYPE = DatabaseType.POSTGRES
 */
abstract class AbstractDatabaseTest {
    protected val vertx = Vertx.vertx()!!
    protected val dbConfig = DatabaseConfigHelper.createDefaultConfig(DATABASE_TYPE)
    lateinit var database: Database

    @BeforeEach
    fun setupDb() {
        database = createDatabase(dbConfig)
    }

    private fun createDatabase(databaseConfig: DatabaseConfig): Database {
        optionallyCreatePostgresDbFromTemplate(databaseConfig, vertx)
        migrateDb(databaseConfig, "db")
        return TestEnvironment.createDatabase(TEST_MARIELLA, createPool(vertx, databaseConfig))
    }

    @AfterEach
    fun close() {
        runBlocking {
            vertx.close().coAwait()
        }
    }

    suspend fun checkCountOfTable(table: String, expectedCount: Int) {
        val count = getCountOfTable(table)
        expectThat(count).describedAs("count of table $table").isEqualTo(expectedCount)
    }

    private suspend fun getCountOfTable(table: String): Int {
        return database.getCountOfTable(table)
    }
}

fun createPool(vertx: Vertx, databaseConfig: DatabaseConfig, autoCommit: Boolean = false): Pool {
    return when (databaseConfig.type) {
        DatabaseType.POSTGRES -> createPostgresPool(vertx, databaseConfig)
        DatabaseType.H2, DatabaseType.H2_MEM -> createJdbcPool(vertx, databaseConfig, autoCommit)
    }
}

private fun createJdbcPool(vertx: Vertx, databaseConfig: DatabaseConfig, autoCommit: Boolean = false): Pool {
    val poolConfig = HikariConfig()
    poolConfig.jdbcUrl = databaseConfig.getUrl()
    poolConfig.username = databaseConfig.user
    poolConfig.password = databaseConfig.password
    poolConfig.maximumPoolSize = databaseConfig.poolSize
    poolConfig.isAutoCommit = autoCommit
    poolConfig.transactionIsolation = "TRANSACTION_READ_COMMITTED"
    poolConfig.validate()

    return JDBCPool.pool(
        vertx,
        HikariDataSource(poolConfig),
        PoolOptions().setMaxSize(databaseConfig.poolSize)
    )
}

private fun createPostgresPool(vertx: Vertx, databaseConfig: DatabaseConfig): Pool {
    val connectOptions = PgConnectOptions()
        .setPort(databaseConfig.port)
        .setHost(databaseConfig.host)
        .setDatabase(databaseConfig.database)
        .setUser(databaseConfig.user)
        .setPassword(databaseConfig.password)
    return PgBuilder
        .pool()
        .using(vertx)
        .with(PoolOptions().setMaxSize(databaseConfig.poolSize))
        .connectingTo(connectOptions)
        .build()
}


suspend fun <T> Database.read(block: suspend DatabaseSession.() -> T): T {
    val session = createSession()
    try {
        return block(session)
    } finally {
        session.close()
    }
}

suspend fun Database.getCountOfTable(table: String): Int {
    return read {
        mapper.selectOneExistingPrimitive("select count(*) from $table")
    }
}

suspend fun <T> Database.write(block: suspend DatabaseSession.() -> T): T {
    val session = createSession()
    return try {
        val t = block(session)
        session.commit()
        t
    } finally {
        session.close()
    }
}

fun migrateDb(databaseConfig: DatabaseConfig, vararg locations: String) {
    val flyway = Flyway.configure().loggers("slf4j").locations(*locations)
        .dataSource(databaseConfig.getUrl(), databaseConfig.user, databaseConfig.password).load()
    flyway.migrate()
}

suspend fun AbstractDatabaseTest.createFiles(nrOfFiles: Int = 1): List<FileVersion> {
    val files = database.write {
        val context = modify()
        val space = context.addExisting<Space>(TestData.TEST_SPACE)
        val user = context.addExisting<UserEntity>(TestData.USER_SEPPI)

        val revision = context.create<Revision>()
        revision.space = space
        revision.creationUser = user
        revision.createdAt = Instant.now()

        val files = (1..nrOfFiles).map {
            val file = context.create<File>()
            file.space = space
            file.comment = "my comment"
            file.owner = user
            file.revision = revision
            file.createdAt = revision.createdAt
            file.entityId = "entityId$it"
            file
        }.map {
            val fileVersion = context.create<FileVersion>()
            fileVersion.name = "my file"
            fileVersion.space = space
            fileVersion.path = "/my/file/ola"
            fileVersion.revision = revision
            fileVersion.size = 100
            fileVersion.resource = it
            fileVersion.revisionFrom = revision.createdAt
            fileVersion.versionId = it.entityId + "-1"

            fileVersion
        }
        context.flush()
        files
    }
    checkCountOfTable("resource_node", nrOfFiles)
    checkCountOfTable("resource_node_version", nrOfFiles)
    return files
}

class VertxWrapper : AutoCloseable {
    val vertx: Vertx = Vertx.vertx()
    override fun close() {
        runBlocking {
            vertx.close().coAwait()
        }
    }
}

val PROTOTYPE_POSTGRES_DB: String by lazy {
    require(DATABASE_TYPE == DatabaseType.POSTGRES) { "prototype db only works for postgres" }
    val prototypeConfig = DatabaseConfigHelper.createDefaultConfig(DatabaseType.POSTGRES)
    VertxWrapper().use {
        try {
            runStatementWithoutTransaction("create database ${prototypeConfig.database}", prototypeConfig, it.vertx)
        } catch (e: Exception) {
            throw RuntimeException("postgres not running! start with 'docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres'", e)
        }
    }
    migrateDb(prototypeConfig, "db")
    prototypeConfig.database
}

val TEST_MARIELLA: MariellaMapping by lazy {
    val config = if (DATABASE_TYPE == DatabaseType.POSTGRES) {
        DatabaseConfigHelper.createDefaultConfig(DatabaseType.POSTGRES).copy(database = PROTOTYPE_POSTGRES_DB)
    } else {
        val c = DatabaseConfigHelper.createDefaultConfig(DATABASE_TYPE)
        migrateDb(c, "db")
        c
    }
    TestEnvironment.createMariella(databaseConfig = config)
}

fun runStatementWithoutTransaction(statement: String, databaseConfig: DatabaseConfig, vertx: Vertx) {
    val rootDb = createPostgresPool(vertx, databaseConfig.copy(database = "postgres"))
    runBlocking {
        try {
            val connection = rootDb.connection.coAwait()
            @Suppress("SqlSourceToSinkFlow")
            connection.preparedQuery(statement).execute().coAwait()
        } finally {
            rootDb.close().coAwait()
        }
    }
}

fun optionallyCreatePostgresDbFromTemplate(databaseConfig: DatabaseConfig, vertx: Vertx) {
    if (databaseConfig.type == DatabaseType.POSTGRES) {
        runStatementWithoutTransaction(
            "create database ${databaseConfig.database} with template $PROTOTYPE_POSTGRES_DB",
            databaseConfig,
            vertx
        )
    }
}