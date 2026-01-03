package org.mariella.persistence.kotlin

import org.mariella.persistence.database.Converter
import org.mariella.persistence.database.ParameterValues
import org.mariella.persistence.database.ResultRow
import org.mariella.persistence.query.Literal
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

object KotlinUuidConverter : Converter<Uuid?> {
    override fun toString(value: Uuid?): String {
        return value.toString()
    }

    override fun getObject(row: ResultRow, index: Int): Uuid? {
        return row.getUUID(index)?.toKotlinUuid()
    }

    override fun setObject(pv: ParameterValues, index: Int, value: Uuid?) {
        if (value == null)
            pv.setUUID(index, null)
        else
            pv.setUUID(index, value.toJavaUuid())
    }

    override fun createLiteral(value: Any): Literal<Uuid?> {
        return KotlinUuidLiteral((value as Uuid))
    }

    override fun createDummy(): Literal<Uuid?> {
        return createLiteral(0)
    }

    class KotlinUuidLiteral(value: Uuid?) : Literal<Uuid?>(this@KotlinUuidConverter, value) {
        override fun printSql(b: StringBuilder) {
            if (value == null) {
                b.append("null")
            } else {
                b.append("'").append(toString(value)).append("'")
            }
        }
    }
}