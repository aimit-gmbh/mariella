package org.mariella.persistence.kotlin

import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.mariella.persistence.annotations.Converter
import org.mariella.persistence.kotlin.internal.ImmutableConverterRegistry
import org.mariella.persistence.kotlin.internal.VertxResultRow
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

class Mapper internal constructor(private val sqlClient: SqlClient, private val converterRegistry: ImmutableConverterRegistry) {

    private data class ConverterAndValueClass(val converter: org.mariella.persistence.database.Converter<*>, val valueClass: KClass<*>?)

    private data class ParameterAndConvert(val parameter: KParameter, val converterWithClass: ConverterAndValueClass)

    private fun KClass<*>.getValueClassSingleType() = (primaryConstructor!!.parameters.single().type.classifier as KClass<*>).javaObjectType

    private fun KClass<*>.getValueClassInstance(data: Any) = primaryConstructor!!.call(data)

    private fun <T : Any> RowSet<Row>.mapRowsToObjects(
        kClass: KClass<T>, converterRegistry: ImmutableConverterRegistry, primaryConstructor: KFunction<Any>
    ): List<T> {
        val converters = columnDescriptors().map { columnDescriptor ->
            val parameter = primaryConstructor.parameters.singleOrNull {
                it.name!!.equals(columnDescriptor.name(), ignoreCase = true)
            } ?: error("no constructor parameter for class ${kClass.simpleName} with name ${columnDescriptor.name().lowercase()}")

            val field = kClass.javaObjectType.declaredFields.single { parameter.name == it.name }
            val annotation = field.getDeclaredAnnotation(Converter::class.java)

            val converter = if (annotation == null) {
                val targetClass = parameter.type.classifier as KClass<*>
                if (targetClass.isValue) {
                    ConverterAndValueClass(converterRegistry.getConverterForColumn(targetClass.getValueClassSingleType(), columnDescriptor.jdbcType()), targetClass)
                } else {
                    ConverterAndValueClass(converterRegistry.getConverterForColumn(targetClass.javaObjectType, columnDescriptor.jdbcType()), null)
                }
            } else {
                ConverterAndValueClass(converterRegistry.getNamedConverter(annotation.name), null)
            }
            ParameterAndConvert(parameter, converter)
        }
        return map { row ->
            val resultRow = VertxResultRow(row)
            val paramValues = mutableMapOf<KParameter, Any?>()

            converters.forEachIndexed { index, parameterAndConvert ->
                val value = parameterAndConvert.converterWithClass.converter.getObject(resultRow, index + 1)
                if (!(parameterAndConvert.parameter.isOptional && value == null)) {
                    if (parameterAndConvert.converterWithClass.valueClass != null) {
                        paramValues[parameterAndConvert.parameter] = parameterAndConvert.converterWithClass.valueClass.getValueClassInstance(value)
                    } else {
                        paramValues[parameterAndConvert.parameter] = value
                    }
                }
            }
            @Suppress("UNCHECKED_CAST")
            primaryConstructor.callBy(paramValues) as T
        }
    }

    suspend fun <T : Any> select(sql: String, clazz: KClass<T>, vararg params: Any?): List<T> {
        val primaryConstructor = verifyConstructor(clazz)
        val ex = DatabaseException("query failed -> $sql")
        try {
            val result = runQuery(sql, params)
            if (result.size() == 0) return emptyList()
            return result.mapRowsToObjects(clazz, converterRegistry, primaryConstructor)
        } catch (e: Throwable) {
            throw ex.initCause(e)
        }
    }

    private fun <T : Any> verifyConstructor(clazz: KClass<T>): KFunction<T> {
        val primaryConstructor = clazz.primaryConstructor!!

        primaryConstructor.parameters.forEach {
            if (it.isOptional && it.type.isMarkedNullable) throw DatabaseException("mapping a nullable type with default value does not make sense")
        }
        return primaryConstructor
    }

    fun <T : Any> cursor(sql: String, clazz: KClass<T>, vararg params: Any?, batchSize: Int = 100): Flow<T> {
        val primaryConstructor = verifyConstructor(clazz)
        return flow {
            val ex = DatabaseException("cursor failed -> $sql")
            val cursor = try {
                @Suppress("SqlSourceToSinkFlow") (sqlClient as SqlConnection).prepare(sql).coAwait().cursor(getTuple(params))
            } catch (e: Throwable) {
                throw ex.initCause(e)
            }
            try {
                cursor.read(batchSize).coAwait().mapRowsToObjects(clazz, converterRegistry, primaryConstructor).forEach { emit(it) }
                while (cursor.hasMore()) cursor.read(batchSize).coAwait().mapRowsToObjects(clazz, converterRegistry, primaryConstructor).forEach { emit(it) }
            } catch (e: Throwable) {
                throw ex.initCause(e)
            } finally {
                cursor.close()
            }
        }
    }

    suspend inline fun <reified T : Any> select(sql: String, vararg params: Any?): List<T> = select(sql, T::class, params = params)

    inline fun <reified T : Any> cursor(sql: String, vararg params: Any?, batchSize: Int = 1000): Flow<T> = cursor(sql, T::class, params = params, batchSize)

    suspend inline fun <reified T : Any> selectOne(sql: String, vararg params: Any?): T? = select<T>(sql, params = params).singleOrNull()

    suspend inline fun <reified T> selectPrimitive(sql: String, vararg params: Any?): List<T> = selectPrimitive(sql, T::class, params = params)

    suspend fun <T> selectPrimitive(sql: String, clazz: KClass<*>, vararg params: Any?): List<T> {
        val ex = DatabaseException("query failed -> $sql")
        try {
            val result = runQuery(sql, params)
            if (result.size() == 0) return emptyList()

            if (result.columnDescriptors().size != 1) {
                error("expected a single result column for select $sql")
            }
            val converter = converterRegistry.getConverterForColumn(
                if (clazz.isValue) clazz.getValueClassSingleType() else clazz.java, result.columnDescriptors().single().jdbcType()
            )
            return result.map {
                val row = VertxResultRow(it)
                @Suppress("UNCHECKED_CAST") if (clazz.isValue) clazz.getValueClassInstance(converter.getObject(row, 1)) as T
                else converter.getObject(row, 1) as T
            }
        } catch (e: Throwable) {
            throw ex.initCause(e)
        }
    }

    suspend fun execute(sql: String, vararg params: Any?): RowSet<Row> {
        return runQuery(sql, params)
    }

    suspend fun executeBatch(sql: String, params: List<List<Any?>>, batchSize: Int = 10000): List<RowSet<Row>> {
        val ex = DatabaseException("batch operation failed -> $sql")
        try {
            @Suppress("SqlSourceToSinkFlow") val st = sqlClient.preparedQuery(sql)
            return params.chunked(batchSize).map { sublist ->
                st.executeBatch(sublist.map { getTuple(it.toTypedArray()) }).coAwait()
            }
        } catch (e: Throwable) {
            throw ex.initCause(e)
        }
    }

    private suspend fun runQuery(sql: String, params: Array<out Any?>) = sqlClient.preparedQuery(sql).execute(getTuple(params)).coAwait()

    private fun getTuple(params: Array<out Any?>): Tuple {
        return Tuple.from(params.map {
            if (it != null) {
                val actualValue = if (it::class.isValue) it::class.memberProperties.single().getter.call(it) else it
                when (actualValue) {
                    is Instant -> OffsetDateTime.ofInstant(actualValue, ZoneId.of("UTC"))
                    is StringMappedSealedClass -> actualValue.value
                    is IntegerMappedSealedClass -> actualValue.value
                    else -> actualValue
                }
            } else {
                null
            }
        })
    }

    suspend inline fun <reified T> selectOnePrimitive(sql: String, vararg params: Any?): T? = selectPrimitive<T>(sql, params = params).singleOrNull()

    suspend inline fun <reified T> selectOneExistingPrimitive(sql: String, vararg params: Any?): T {
        val res = selectPrimitive<T>(sql, params = params)
        if (res.size != 1)
            throw DatabaseException("Expected exactly 1 result for query \"$sql\" but got ${res.size}")
        return res.single()
    }

}

