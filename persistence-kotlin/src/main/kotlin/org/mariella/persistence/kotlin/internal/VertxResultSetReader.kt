package org.mariella.persistence.kotlin.internal

import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import org.mariella.persistence.database.ResultRow
import org.mariella.persistence.database.ResultSetReader

internal class VertxResultSetReader(result: RowSet<Row>) : ResultSetReader {
    private val iterator = result.iterator()
    private var currentColumnIndex = 0
    private var resultRow: VertxResultRow? = null

    override fun getCurrentColumnIndex(): Int {
        return currentColumnIndex
    }

    override fun setCurrentColumnIndex(index: Int) {
        currentColumnIndex = index
    }

    override fun getResultRow(): ResultRow? {
        return resultRow
    }

    override fun next(): Boolean {
        resultRow = if (iterator.hasNext()) {
            VertxResultRow(iterator.next()!!)
        } else {
            null
        }
        return resultRow != null
    }
}