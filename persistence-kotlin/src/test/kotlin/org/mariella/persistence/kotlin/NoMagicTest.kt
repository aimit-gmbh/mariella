package org.mariella.persistence.kotlin

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.vertx.core.Vertx
import io.vertx.jdbcclient.JDBCPool
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.PoolOptions
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mariella.persistence.kotlin.simple.OtherSimpleEntity
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.util.*

class NoMagicTest {

    private val vertx = Vertx.vertx()

    @AfterEach
    fun closeVertx() = runBlocking<Unit> { vertx.close().coAwait() }

    @Table(name = "simple")
    @Entity
    class SimpleEntity : TrackingSupport() {
        @get:Id
        @get:Column(name = "id")
        var id: UUID by changeSupport(UUID.randomUUID())

        @get:Column(name = "name")
        var name: String by changeSupport()

        @get:Column(name = "age")
        var age: Int by changeSupport()
    }

    @Test
    fun `plain h2 example without test helper classes`() {
        runTest {
            val database = createDatabase("org.mariella.persistence.kotlin")

            // typical interaction 1 - insert data
            // all modifications done within a session are NOT thread safe
            val connection = database.connect()
            val mariella = connection.mariella()
            val bob = mariella.create<SimpleEntity> {
                it.name = "Bob "
                it.age = 42
            }
            val alice = mariella.create<SimpleEntity> {
                it.name = "Alice"
                it.age = 42
            }
            // flush serializes all modifications made to the context to the db
            // statements are written in the same order as the modifications occurred
            mariella.flush()
            connection.commitAndClose()

            // typical interaction 2 - update data
            val updateConnection = database.connectAutoCommit()
            val updateMariella = updateConnection.mariella()

            updateMariella.modify<SimpleEntity>(bob.id) {
                it.age = 60
            }
            updateMariella.modify<SimpleEntity>(alice.id) {
                it.age = 22
            }
            updateMariella.flush()
            updateConnection.close()

            // typical interaction 3 - read data
            val readOnlyConnection = database.connectReadOnly()
            val readOnlyMariella = readOnlyConnection.mariella()

            val people = readOnlyMariella.loadEntities<SimpleEntity>(listOf(bob.id, alice.id))
            expectThat(people).hasSize(2)
            expectThat(people.single { it.id == bob.id }.age).isEqualTo(60)
            expectThat(people.single { it.id == alice.id }.age).isEqualTo(22)
            readOnlyConnection.close()

            // typical interaction 4 - combine the mariella API with the mapper API
            val transactionalConnection = database.connect()
            val mariellaAPI = transactionalConnection.mariella()
            val mapperAPI = transactionalConnection.mapper()

            val allIds = mapperAPI.selectPrimitive<UUID>("select id from simple for update")
            val all = mariellaAPI.loadEntities<SimpleEntity>(allIds)

            all.forEach {
                it.age = 100
                it.name = "the same"
            }
            mariellaAPI.flush()
            transactionalConnection.commitAndClose()
        }
    }

    @Test
    fun `can connect to 2 different databases with different mappings`() {
        runTest {
            val database = createDatabase("org.mariella.persistence.kotlin")
            val database1 = createDatabase("org.mariella.persistence.kotlin.simple")

            database.write {
                val modify = mariella()
                modify.create<SimpleEntity> {
                    it.name = "Bob"
                    it.age = 42
                }
                modify.flush()
            }

            database1.write {
                val modify = mariella()
                modify.create<OtherSimpleEntity> {
                    it.name = "Alice"
                    it.age = 42
                }
                modify.flush()
            }

            database.read {
                val count = mapper().selectOneExistingPrimitive<Int>("select count(*) from simple")
                expectThat(count).isEqualTo(1)
            }

            database1.read {
                val count = mapper().selectOneExistingPrimitive<Int>("select count(*) from simple")
                expectThat(count).isEqualTo(1)
            }
        }
    }

    private suspend fun createDatabase(packageName: String): Database {
        val databaseUrl = "jdbc:h2:mem:${UUID.randomUUID()};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1"
        val databaseUser = "sa"

        // setup code (create pool and create db structure)
        val poolConfig = HikariConfig()
        poolConfig.jdbcUrl = databaseUrl
        poolConfig.username = databaseUser
        poolConfig.password = ""
        poolConfig.maximumPoolSize = 10
        poolConfig.isAutoCommit = false
        poolConfig.transactionIsolation = "TRANSACTION_READ_COMMITTED"
        poolConfig.validate()

        val pool = JDBCPool.pool(vertx, HikariDataSource(poolConfig), PoolOptions().setMaxSize(10))

        val connection = pool.connection.coAwait()
        @Suppress("SqlNoDataSourceInspection")
        connection.preparedQuery("create table simple (id uuid not null, name varchar, age int, constraint simple_pk primary key (id))").execute().coAwait()
        connection.close().coAwait()
        // end setup code

        // immutable and thread safe
        val database = VertxDatabaseFactory.create(
            jdbcUrl = databaseUrl,
            entityPackages = listOf(packageName),
            user = databaseUser,
            password = "",
            pool = pool
        )
        return database
    }
}