package org.mariella.persistence.kotlin.internal

import io.vertx.sqlclient.Tuple
import org.mariella.persistence.database.ParameterValues
import java.io.StringReader
import java.math.BigDecimal
import java.sql.Date
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

internal class VertxParameterValues : ParameterValues {
    private val utcZoneId = ZoneId.of("UTC")
    private val values = mutableListOf<Any?>()

    private fun set(pos: Int, value: Any?) {
        val p = pos - 1
        // TODO: ensureCapacity does not work. this is an ugly workaround
        for (@Suppress("Unused") i in values.size until p + 1) {
            values.add(null)
        }
        values[p] = value
    }

    fun tuple(): Tuple {
        return Tuple.from(values)
    }

    override fun setString(pos: Int, value: String?) {
        set(pos, value)
    }

    override fun setNString(pos: Int, value: String?) {
        set(pos, value)
    }

    override fun setBigDecimal(pos: Int, value: BigDecimal?) {
        set(pos, value)
    }

    override fun setInteger(pos: Int, value: Int?) {
        set(pos, value)
    }

    override fun setBoolean(pos: Int, value: Boolean?) {
        set(pos, value)
    }

    override fun setLong(pos: Int, value: Long?) {
        set(pos, value)
    }

    override fun setDouble(pos: Int, value: Double?) {
        set(pos, value)
    }

    override fun setBytes(pos: Int, value: ByteArray?) {
        set(pos, value)
    }

    override fun setUUID(pos: Int, value: UUID?) {
        set(pos, value)
    }

    override fun setTimestamp(pos: Int, value: Timestamp?) {
        val odt = value?.let { OffsetDateTime.ofInstant(it.toInstant(), utcZoneId) }
        set(pos, odt)
    }

    override fun setDate(pos: Int, value: Date?) {
        set(pos, value?.toLocalDate())
    }

    override fun setClob(pos: Int, value: StringReader) {
        throw UnsupportedOperationException()
    }
}