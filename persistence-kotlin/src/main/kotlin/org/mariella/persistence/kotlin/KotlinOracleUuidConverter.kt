package org.mariella.persistence.kotlin

import org.mariella.persistence.database.Converter
import org.mariella.persistence.database.ParameterValues
import org.mariella.persistence.database.ResultRow
import org.mariella.persistence.query.Literal
import org.mariella.persistence.util.UUIDUtils
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

object KotlinOracleUuidConverter : Converter<Uuid?> {
    override fun toString(value: Uuid?): String {
        return value.toString()
    }

    override fun getObject(row: ResultRow, index: Int): Uuid? {
        val bytes = row.getBytes(index) ?: return null
        return UUIDUtils.toUUID(bytes).toKotlinUuid()
    }

    override fun setObject(pv: ParameterValues, index: Int, value: Uuid?) {
        pv.setBytes(index, if (value == null) null else UUIDUtils.toBytes(value.toJavaUuid()))
    }

    override fun createLiteral(value: Any): Literal<Uuid?> {
        return KotlinUuidLiteral((value as Uuid))
    }

    override fun createDummy(): Literal<Uuid?> {
        return createLiteral(0)
    }

    class KotlinUuidLiteral(value: Uuid?) : Literal<Uuid?>(this@KotlinOracleUuidConverter, value) {
        override fun printSql(b: StringBuilder) {
            if (value == null) {
                b.append("null")
            } else {
                b.append("'").append(toString(value)).append("'")
            }
        }
    }
}