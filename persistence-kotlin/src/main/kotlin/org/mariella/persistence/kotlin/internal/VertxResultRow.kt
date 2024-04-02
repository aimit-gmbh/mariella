package org.mariella.persistence.kotlin.internal

import io.vertx.sqlclient.Row
import org.mariella.persistence.database.ResultRow
import java.math.BigDecimal
import java.sql.Clob
import java.sql.Date
import java.sql.Timestamp
import java.util.*

internal class VertxResultRow(private val row: Row) : ResultRow {
    override fun getString(pos: Int): String? {
        return row.getString(pos - 1)
    }

    override fun getNString(pos: Int): String {
        throw UnsupportedOperationException()
    }

    override fun getBoolean(pos: Int): Boolean {
        return row.getBoolean(pos - 1)
    }

    override fun getBigDecimal(pos: Int): BigDecimal? {
        return row.getBigDecimal(pos - 1)
    }

    override fun getInteger(pos: Int): Int? {
        return row.getInteger(pos - 1)
    }

    override fun getLong(pos: Int): Long? {
        return row.getLong(pos - 1)
    }

    override fun getDouble(pos: Int): Double? {
        return row.getDouble(pos - 1)
    }

    override fun getBytes(pos: Int): ByteArray {
        throw UnsupportedOperationException()
    }

    override fun getUUID(pos: Int): UUID? {
        return row.getUUID(pos - 1)
    }

    override fun getTimestamp(pos: Int): Timestamp? {
        val value = row.getOffsetDateTime(pos - 1)
        return if (value == null) null else Timestamp.from(value.toInstant())
    }

    override fun getDate(pos: Int): Date? {
        val value = row.getLocalDate(pos - 1)
        return if (value == null) null else Date.valueOf(value)
    }

    override fun getClob(pos: Int): Clob {
        throw UnsupportedOperationException()
    }
}