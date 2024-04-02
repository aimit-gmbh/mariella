package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.database.Converter
import org.mariella.persistence.database.ParameterValues
import org.mariella.persistence.database.ResultRow
import org.mariella.persistence.kotlin.IntegerMappedSealedClass
import org.mariella.persistence.query.Literal
import kotlin.reflect.KClass

internal class IntSealedClassConverter<T : IntegerMappedSealedClass>(private val clazz: KClass<T>) : Converter<T?> {

    init {
        if (!clazz.isSealed)
            error("${clazz.simpleName} needs to be a sealed class")
    }

    override fun setObject(pv: ParameterValues, index: Int, value: T?) {
        if (value == null)
            pv.setInteger(index, null)
        else
            pv.setInteger(index, value.value)
    }

    override fun toString(value: T?): String {
        return value.toString()
    }

    override fun getObject(row: ResultRow, index: Int): T? {
        val value = row.getInteger(index)
        return if (value == null) null else clazz.sealedSubclasses.single { it.objectInstance!!.value == value }.objectInstance
    }

    override fun createLiteral(value: Any?): Literal<T?> {
        throw UnsupportedOperationException()
    }

    override fun createDummy(): Literal<T?> {
        return object : Literal<T?>(this, null) {
            override fun printSql(b: StringBuilder) {
                b.append("''")
            }
        }
    }

}