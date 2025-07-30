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
import org.mariella.persistence.kotlin.util.read
import org.mariella.persistence.kotlin.util.write
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
            // all modifications done within a session are not thread safe
            val connectionWithTransaction = database.createSession()
            val context = connectionWithTransaction.modify()
            val bob = context.create<SimpleEntity> {
                it.name = "Bob"
                it.age = 42
            }
            val alice = context.create<SimpleEntity> {
                it.name = "Alice"
                it.age = 42
            }
            // flush serializes all modifications made to the context to the db
            // statements are written in the same order as the modifications occurred
            context.flush()
            connectionWithTransaction.commitAndClose()

            // typical interaction 2 - update data
            val connectionWithTransaction1 = database.createSession()
            val context1 = connectionWithTransaction1.modify()

            context1.updateOne<SimpleEntity>(bob.id) {
                it.age = 60
            }
            context1.updateOne<SimpleEntity>(alice.id) {
                it.age = 22
            }
            context1.flush()
            connectionWithTransaction1.commitAndClose()

            // typical interaction 3 - read data
            val connectionWithTransaction2 = database.createSession()
            val context2 = connectionWithTransaction2.modify()

            val people = context2.loadEntities<SimpleEntity>(listOf(bob.id, alice.id))
            expectThat(people).hasSize(2)
            connectionWithTransaction2.close()

            // typical interaction 4 - combine sql with mapping
            val connectionWithTransaction3 = database.createSession()
            val context3 = connectionWithTransaction3.modify()

            val allIds = context3.mapper.selectPrimitive<UUID>("select id from simple for update")
            val all = context3.loadEntities<SimpleEntity>(allIds)

            all.forEach {
                it.age = 100
                it.name = "the same"
            }
            context3.flush()
            connectionWithTransaction3.commitAndClose()
        }
    }

    @Test
    fun `can connect to 2 different databases with different mappings`() {
        runTest {
            val database = createDatabase("org.mariella.persistence.kotlin")
            val database1 = createDatabase("org.mariella.persistence.kotlin.simple")

            database.write {
                val modify = modify()
                modify.create<SimpleEntity> {
                    it.name = "Bob"
                    it.age = 42
                }
                modify.flush()
            }

            database1.write {
                val modify = modify()
                modify.create<OtherSimpleEntity> {
                    it.name = "Alice"
                    it.age = 42
                }
                modify.flush()
            }

            database.read {
                val count = mapper.selectOneExistingPrimitive<Int>("select count(*) from simple")
                expectThat(count).isEqualTo(1)
            }

            database1.read {
                val count = mapper.selectOneExistingPrimitive<Int>("select count(*) from simple")
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
        connection.preparedQuery("create table simple (id uuid not null, name varchar, age int, constraint simple_pk primary key (id))").execute().coAwait()
        connection.close().coAwait()
        // end setup code

        // reads metadata from the database and the jpa annotations
        // the mapping can be cached as long as the database does not change
        val mapping = MariellaFactory.createMariellaMapping(
            jdbcUrl = databaseUrl,
            entityPackages = listOf(packageName),
            user = databaseUser,
            password = ""
        )
        // immutable and thread safe
        val database = MariellaFactory.createDatabase(mapping, pool)
        return database
    }
}