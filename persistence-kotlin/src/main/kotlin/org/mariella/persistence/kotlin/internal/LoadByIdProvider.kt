package org.mariella.persistence.kotlin.internal

import org.mariella.persistence.loader.ClusterLoaderConditionProvider
import org.mariella.persistence.mapping.ClassMapping
import org.mariella.persistence.mapping.RelationshipPropertyMapping
import org.mariella.persistence.query.BinaryCondition
import org.mariella.persistence.query.JoinBuilder
import org.mariella.persistence.query.QueryBuilder
import org.mariella.persistence.query.TableReference
import java.util.*

internal class LoadByIdProvider(private val id: UUID) : ClusterLoaderConditionProvider {
    override fun getConditionPathExpressions(): Array<String> {
        return arrayOf("root")
    }

    override fun aboutToJoinRelationship(
        queryBuilder: QueryBuilder, pathExpression: String,
        rpm: RelationshipPropertyMapping,
        joinBuilder: JoinBuilder
    ) {
    }

    override fun pathExpressionJoined(
        queryBuilder: QueryBuilder,
        pathExpression: String,
        classMapping: ClassMapping,
        tableReference: TableReference
    ) {
        if (pathExpression == "root") {
            classMapping.primaryKey.columnMappings.forEach {
                val condition = BinaryCondition.eq(
                    tableReference.createColumnReference(it.readColumn),
                    it.readColumn.converter().createLiteral(id)
                )
                queryBuilder.and(condition)
            }
        }
    }
}