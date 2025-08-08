package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.database.Converter
import org.mariella.persistence.database.ParameterValues
import org.mariella.persistence.database.ResultRow
import org.mariella.persistence.kotlin.StringMappedSealedClass
import org.mariella.persistence.query.Literal
import kotlin.reflect.KClass

internal class StringSealedClassConverter<T : StringMappedSealedClass>(private val clazz: KClass<T>) : Converter<T?> {
    override fun setObject(pv: ParameterValues, index: Int, value: T?) {
        if (value == null)
            pv.setString(index, null)
        else
            pv.setString(index, value.value)
    }

    override fun toString(value: T?): String {
        return value.toString()
    }

    override fun getObject(row: ResultRow, index: Int): T? {
        val value = row.getString(index)
        return if (value == null) null else clazz.sealedSubclasses.single { it.objectInstance!!.value == value }.objectInstance
    }

    override fun createLiteral(value: Any?): Literal<T?> {
        @Suppress("UNCHECKED_CAST") val casted = value as T?
        return object : Literal<T?>(this, casted) {
            override fun printSql(b: StringBuilder) {
                if (casted == null)
                    b.append("null")
                else {
                    b.append("'${casted.value}'")
                }
            }
        }
    }

    override fun createDummy(): Literal<T?> {
        return object : Literal<T?>(this, null) {
            override fun printSql(b: StringBuilder) {
                b.append("''")
            }
        }
    }

}