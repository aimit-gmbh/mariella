package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.database.Converter
import org.mariella.persistence.database.ParameterValues
import org.mariella.persistence.database.ResultRow
import org.mariella.persistence.kotlin.ClusterLoaderAwareConditionProvider
import org.mariella.persistence.mapping.ClassMapping
import org.mariella.persistence.query.*
import org.mariella.persistence.query.Function
import java.sql.Types

internal class InClusterLoaderConditionProvider(private val ids: Array<*>) : ClusterLoaderAwareConditionProvider() {
    override fun pathExpressionJoined(
        queryBuilder: QueryBuilder,
        pathExpression: String,
        classMapping: ClassMapping,
        tableReference: TableReference
    ) {
        val column = classMapping.primaryKey.columnMappings.single().readColumn
        val colRef = tableReference.createColumnReference(column)

        val condition = BinaryCondition.eq(
            colRef,
            Function("ANY", queryBuilder.createParameter())
        )
        queryBuilder.and(condition)
        clusterLoader.addParameter(object : Converter<Array<*>> {
            override fun setObject(pv: ParameterValues?, index: Int, value: Array<*>) {
                (pv as VertxParameterValues).set(index, value)
            }

            override fun getObject(row: ResultRow?, index: Int): Array<*>? {
                throw UnsupportedOperationException()
            }

            override fun createLiteral(value: Any?): Literal<Array<*>?>? {
                throw UnsupportedOperationException()
            }

            override fun createDummy(): Literal<Array<*>?>? {
                throw UnsupportedOperationException()
            }

            override fun toString(value: Array<*>?): String? {
                throw UnsupportedOperationException()
            }


        }, Types.ARRAY, ids)
    }
}