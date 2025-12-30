package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.loader.ClusterLoaderConditionProviderImpl
import org.mariella.persistence.mapping.ClassMapping
import org.mariella.persistence.query.InCondition
import org.mariella.persistence.query.QueryBuilder
import org.mariella.persistence.query.TableReference

internal class InClusterLoaderConditionProvider(private val ids: Collection<Any>, idPropertyPath: String? = null) :
    ClusterLoaderConditionProviderImpl() {

    private val idPropertyPath: String = idPropertyPath ?: "root.id"

    override fun pathExpressionJoined(
        queryBuilder: QueryBuilder,
        pathExpression: String,
        classMapping: ClassMapping,
        tableReference: TableReference
    ) {
        val converter = classMapping.primaryKey.columnMappings.single().readColumn.converter
        val inExpressions = ids.map { converter.createLiteral(it) }
        queryBuilder.and(
            InCondition(queryBuilder.createColumnReference(idPropertyPath), inExpressions)
        )
    }

    override fun getConditionPathExpressions(): Array<String> {
        return arrayOf("root")
    }
}