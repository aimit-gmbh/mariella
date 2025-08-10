package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.loader.ClusterLoaderConditionProvider
import org.mariella.persistence.mapping.ClassMapping
import org.mariella.persistence.mapping.ReferencePropertyMapping
import org.mariella.persistence.mapping.RelationshipPropertyMapping
import org.mariella.persistence.query.*

internal class LoadByConditionProvider(private val conditions: Map<String, Any?>) : ClusterLoaderConditionProvider {
    init {
        require(conditions.isNotEmpty()) { "conditions must not be empty" }
        conditions.forEach {
            require(it.key.startsWith("root.") && it.key.count { k -> k == '.' } == 1) { "only root properties can be set" }
        }
    }

    override fun getConditionPathExpressions(): Array<out String> {
        return arrayOf("root")
    }

    override fun aboutToJoinRelationship(
        p0: QueryBuilder?,
        p1: String?,
        p2: RelationshipPropertyMapping?,
        p3: JoinBuilder?
    ) {
    }

    override fun pathExpressionJoined(
        queryBuilder: QueryBuilder,
        pathExpression: String,
        classMapping: ClassMapping,
        tableReference: TableReference
    ) {
        conditions.forEach {
            val column = getColumnReference(classMapping, queryBuilder, tableReference, it.key)
            val binaryCondition = BinaryCondition.eq(column, createValueExpression(column, it.value))
            queryBuilder.and(binaryCondition)
        }
    }

    private fun createValueExpression(column: ColumnReference, value: Any?) = if (value != null) column.column().converter.createLiteral(value) else IsNullCondition(column)

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