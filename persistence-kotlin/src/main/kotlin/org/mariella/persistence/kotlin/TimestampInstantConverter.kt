package org.mariella.persistence.kotlin

import org.mariella.persistence.database.Converter
import org.mariella.persistence.database.ParameterValues
import org.mariella.persistence.database.ResultRow
import org.mariella.persistence.kotlin.internal.InstantLiteral
import org.mariella.persistence.query.Literal
import java.sql.Timestamp
import java.time.Instant

object TimestampInstantConverter : Converter<Instant?> {
    override fun toString(value: Instant?): String {
        return value.toString()
    }

    override fun getObject(row: ResultRow, index: Int): Instant? {
        return row.getTimestamp(index)?.toInstant()
    }

    override fun setObject(pv: ParameterValues, index: Int, value: Instant?) {
        if (value == null)
            pv.setTimestamp(index, null)
        else
            pv.setTimestamp(index, Timestamp.from(value))
    }

    override fun createLiteral(value: Any): Literal<Instant?> {
        return InstantLiteral(this, (value as Instant?))
    }

    override fun createDummy(): Literal<Instant?> {
        return createLiteral(0)
    }
}