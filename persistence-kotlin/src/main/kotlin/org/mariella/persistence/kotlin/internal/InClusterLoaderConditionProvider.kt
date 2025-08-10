package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.database.StandardUUIDConverter
import org.mariella.persistence.loader.ClusterLoaderConditionProviderImpl
import org.mariella.persistence.mapping.ClassMapping
import org.mariella.persistence.query.InCondition
import org.mariella.persistence.query.QueryBuilder
import org.mariella.persistence.query.TableReference
import java.util.*

internal class InClusterLoaderConditionProvider(private val ids: Collection<UUID>, idPropertyPath: String? = null) :
    ClusterLoaderConditionProviderImpl() {

    private val idPropertyPath: String = idPropertyPath ?: "root.id"

    override fun pathExpressionJoined(
        queryBuilder: QueryBuilder,
        pathExpression: String,
        classMapping: ClassMapping,
        tableReference: TableReference
    ) {
        val inExpressions = ids.map { StandardUUIDConverter.Singleton.createLiteral(it) }
        queryBuilder.and(
            InCondition(queryBuilder.createColumnReference(idPropertyPath), inExpressions)
        )
    }

    override fun getConditionPathExpressions(): Array<String> {
        return arrayOf("root")
    }
}