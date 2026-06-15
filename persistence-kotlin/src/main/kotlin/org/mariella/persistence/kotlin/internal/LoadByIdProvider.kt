package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.kotlin.ClusterAwareConditionProvider
import org.mariella.persistence.mapping.ClassMapping
import org.mariella.persistence.query.BinaryCondition
import org.mariella.persistence.query.QueryBuilder
import org.mariella.persistence.query.TableReference

internal class LoadByIdProvider(private val id: Any) : ClusterAwareConditionProvider() {
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
            val colRef = tableReference.createColumnReference(column)
            val binaryCondition = BinaryCondition.eq(colRef, queryBuilder.createParameter())
            queryBuilder.and(binaryCondition)
            clusterLoader.addParameter(column, id)
        }
    }
}