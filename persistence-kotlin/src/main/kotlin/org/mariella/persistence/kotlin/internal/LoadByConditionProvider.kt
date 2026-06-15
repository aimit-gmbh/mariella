package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.loader.ClusterLoader
import org.mariella.persistence.loader.ClusterLoaderConditionProviderImpl
import org.mariella.persistence.mapping.ClassMapping
import org.mariella.persistence.mapping.ReferencePropertyMapping
import org.mariella.persistence.query.*

internal class LoadByConditionProvider(private val conditions: Map<String, Any?>) : ClusterLoaderConditionProviderImpl() {
    private lateinit var clusterLoader: ClusterLoader

    init {
        require(conditions.isNotEmpty()) { "conditions must not be empty" }
        conditions.forEach {
            require(it.key.startsWith("root.") && it.key.count { k -> k == '.' } == 1) { "only root properties can be set" }
        }
    }

    override fun initialize(clusterLoader: ClusterLoader) {
        this.clusterLoader = clusterLoader
    }

    override fun getConditionPathExpressions(): Array<out String> {
        return arrayOf("root")
    }

    override fun pathExpressionJoined(
        queryBuilder: QueryBuilder,
        pathExpression: String,
        classMapping: ClassMapping,
        tableReference: TableReference
    ) {
        conditions.forEach {
            val column = getColumnReference(classMapping, queryBuilder, tableReference, it.key)
            if (it.value != null) {
                val binaryCondition = BinaryCondition.eq(column, queryBuilder.createParameter())
                queryBuilder.and(binaryCondition)
                clusterLoader.addParameter(column.column(), it.value)
            } else {
                queryBuilder.and(IsNullCondition(column))
            }
        }
    }

    private fun getColumnReference(
        classMapping: ClassMapping,
        queryBuilder: QueryBuilder,
        tableReference: TableReference,
        pathExpression: String
    ): ColumnReference {
        val prop = classMapping.getPropertyMapping(pathExpression.substringAfter(".")) as? ReferencePropertyMapping
        return if (prop == null) {
            queryBuilder.createColumnReference(pathExpression)
        } else {
            tableReference.createColumnReference(prop.joinColumns.single().myReadColumn)
        }
    }
}