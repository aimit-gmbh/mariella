package org.mariella.test

import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mariella.test.entities.FileVersion
import org.mariella.test.util.AbstractDatabaseTest
import org.mariella.test.util.createFiles
import org.mariella.test.util.createPool
import strikt.api.expectThat
import strikt.assertions.contains
import java.util.*

class StackTraceTest : AbstractDatabaseTest() {

    @Test
    fun `persist produces a correct stacktrace`() {
        val pool = createPool(vertx, dbConfig)
        runTest {
            pool.connection.coAwait().preparedQuery("alter table resource_node drop column node_comment").execute()
                .coAwait()
            pool.close().coAwait()
            val ex = runCatching { createFiles(1) }.exceptionOrNull()!!
            expectThat(ex.stackTraceToString()).contains(this@StackTraceTest.javaClass.simpleName)
        }
    }

    @Test
    fun `load cluster produces a correct stacktrace`() {
        val pool = createPool(vertx, dbConfig)
        runTest {
            pool.connection.coAwait().preparedQuery("alter table file_version drop column filesize").execute().coAwait()
            pool.close().coAwait()
            val session = database.createSession()
            val modifications = session.modify()
            val ex = runCatching { modifications.loadEntity<FileVersion>(UUID.randomUUID()) }.exceptionOrNull()!!
            expectThat(ex.stackTraceToString()).contains(this@StackTraceTest.javaClass.simpleName)
        }
    }

    @Test
    fun `mapper produces a correct stacktrace`() {
        runTest {
            data class Test(val id: String)

            val session = database.createSession()
            val mapper = session.mapper
            val ex = runCatching { mapper.select<Test>("select asdasd from asdasd") }.exceptionOrNull()!!
            expectThat(ex.stackTraceToString()).contains(this@StackTraceTest.javaClass.simpleName)

            val ex1 = runCatching { mapper.selectPrimitive<Int>("select asdasd from asdasd") }.exceptionOrNull()!!
            expectThat(ex1.stackTraceToString()).contains(this@StackTraceTest.javaClass.simpleName)

            val ex2 = runCatching { mapper.cursor<Test>("select asdasd from asdasd").toList() }.exceptionOrNull()!!
            expectThat(ex2.stackTraceToString()).contains(this@StackTraceTest.javaClass.simpleName)
        }
    }
}