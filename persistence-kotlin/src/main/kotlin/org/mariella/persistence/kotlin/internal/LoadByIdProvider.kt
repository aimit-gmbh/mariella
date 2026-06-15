package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.loader.ClusterLoaderConditionProviderImpl
import org.mariella.persistence.mapping.ClassMapping
import org.mariella.persistence.query.BinaryCondition
import org.mariella.persistence.query.QueryBuilder
import org.mariella.persistence.query.TableReference

internal class LoadByIdProvider(private val id: Any) : ClusterLoaderConditionProviderImpl() {
    override fun getConditionPathExpressions(): Array<String> {
        return arrayOf("root")
    }

    override fun pathExpressionJoined(
        queryBuilder: QueryBuilder,
        pathExpression: String,
        classMapping: ClassMapping,
        tableReference: TableReference
    ) {
        if (pathExpression == "root") {
            val column = classMapping.primaryKey.columnMappings.single().readColumn
            val condition = BinaryCondition.eq(
                tableReference.createColumnReference(column),
                column.converter.createLiteral(id)
            )
            queryBuilder.and(condition)
        }
    }
}