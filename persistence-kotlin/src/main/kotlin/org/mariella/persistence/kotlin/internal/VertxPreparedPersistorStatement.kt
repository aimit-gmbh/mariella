package org.mariella.persistence.kotlin.internal

import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.PreparedQuery
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import org.mariella.persistence.database.AbstractPreparedPersistorStatement
import org.mariella.persistence.mapping.PersistorStatement
import org.mariella.persistence.mapping.RowAndObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.ceil
import kotlin.system.measureTimeMillis

typealias MariellaRow = org.mariella.persistence.persistor.Row

internal class VertxPreparedPersistorStatement(
    statement: PersistorStatement,
    private val preparedQuery: PreparedQuery<RowSet<Row>>,
) : AbstractPreparedPersistorStatement(statement) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(VertxPreparedPersistorStatement::class.java)
    }

    private val callback = statement.generatedColumnsCallback
    private val batch: MutableList<VertxParameterValues> = ArrayList()
    private val objects: MutableList<Any?> = ArrayList()

    override fun addBatch(parameters: MariellaRow, o: Any?) {
        if (logger.isTraceEnabled)
            logger.trace("addBatch: {}", statement.getSqlDebugString(parameters))
        val parameterValues = VertxParameterValues()
        statement.setParameters(parameterValues, parameters)
        batch.add(parameterValues)
        objects.add(o)
    }

    suspend fun executeBatch(batchSize: Int = 10000) {
        val elapsed = measureTimeMillis {
            batch.chunked(batchSize).forEach { values ->
                try {
                    val result = preparedQuery
                        .executeBatch(values.map { p -> p.tuple() })
                        .coAwait()

                    if (callback != null) {
                        var index = 0
                        var curRes = result
                        while (curRes != null) {
                            curRes.forEach { row ->
                                callback.accept(RowAndObject(VertxResultRow(row), objects[index]))
                            }
                            index++
                            curRes = curRes.next()
                        }
                    }
                } catch (e: Throwable) {
                    throw QueryExecutionException(statement.sqlString, e)
                }
            }
        }
        val chunks = ceil(batch.size / batchSize.toFloat()).toInt()
        if (logger.isDebugEnabled)
            logger.debug(
                "executeBatch (size {} / {} chunk(s)) in {} ms: {}",
                batch.size,
                chunks,
                elapsed,
                statement.sqlString
            )
    }

    override fun close() {}
}