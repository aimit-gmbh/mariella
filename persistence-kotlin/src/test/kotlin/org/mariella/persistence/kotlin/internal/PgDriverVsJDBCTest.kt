package org.mariella.persistence.kotlin.internal

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.mariella.persistence.kotlin.util.AbstractDatabaseTest
import org.mariella.persistence.kotlin.util.DATABASE_TYPE
import org.mariella.persistence.kotlin.util.DatabaseType
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.sql.DriverManager
import java.sql.Timestamp
import java.time.Instant
import java.util.*

class PgDriverVsJDBCTest : AbstractDatabaseTest() {

    @Test
    fun `JDBC rounds timestamp values to microseconds on insert instead of truncating them`() {
        Assumptions.assumeTrue(DATABASE_TYPE == DatabaseType.POSTGRES)
        runTest {
            val queriedHigh = writeAndGet(Instant.ofEpochSecond(1781528778, 153130500))
            expectThat(queriedHigh.toString()).isEqualTo("2026-06-15T13:06:18.153131Z")
            database.read {
                val queriedWithVertx = mapper().selectOneExistingPrimitive<Instant>("select revision_time from tstest")
                expectThat(queriedWithVertx.toString()).isEqualTo("2026-06-15T13:06:18.153131Z")
            }

            val queriedLow = writeAndGet(Instant.ofEpochSecond(1781528778, 153130400))
            expectThat(queriedLow.toString()).isEqualTo("2026-06-15T13:06:18.153130Z")
            database.read {
                val queriedWithVertx = mapper().selectOneExistingPrimitive<Instant>("select revision_time from tstest")
                expectThat(queriedWithVertx.toString()).isEqualTo("2026-06-15T13:06:18.153130Z")
            }
        }
    }

    private fun writeAndGet(instant: Instant): Instant {
        val props = Properties()
        props.setProperty("user", "postgres")
        props.setProperty("password", "postgres")
        val ret: Instant
        DriverManager.getConnection(dbConfig.getUrl(), props).use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("truncate table tstest")
            }
            conn.prepareStatement("insert into tstest(revision_time) values (?)").use { statement ->
                statement.setTimestamp(1, Timestamp.from(instant))
                statement.execute()
            }
            conn.prepareStatement("select revision_time from tstest").use { statement ->
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    ret = resultSet.getTimestamp(1).toInstant()
                }
            }
        }
        return ret
    }
}