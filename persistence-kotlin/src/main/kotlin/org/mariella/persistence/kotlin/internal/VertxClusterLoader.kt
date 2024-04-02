package org.mariella.persistence.kotlin.internal

import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import org.mariella.persistence.loader.*
import org.mariella.persistence.mapping.SchemaMapping
import org.mariella.persistence.persistor.ClusterDescription
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class VertxClusterLoader<T>(schemaMapping: SchemaMapping, clusterDescription: ClusterDescription) :
    AbstractClusterLoader(schemaMapping, clusterDescription) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(VertxClusterLoader::class.java)
    }

    suspend fun load(
        connection: SqlClient,
        loaderContext: LoaderContext,
        conditionProvider: ClusterLoaderConditionProvider
    ): List<T> {
        if (logger.isDebugEnabled)
            logger.debug("loading cluster")

        val ms = System.currentTimeMillis()
        loaderContext.startLoading()
        val results = loadingPolicies.map {
            val statementBuilder = LoadingPolicyStatementBuilder(
                it,
                loaderContext,
                conditionProvider
            )
            val sql = statementBuilder.createSelectStatement()
            val currentMillis = System.currentTimeMillis()
            val res = try {
                connection.preparedQuery(sql).execute().coAwait()
            } catch (e: Throwable) {
                if (logger.isTraceEnabled)
                    logger.trace("executed statement in " + (System.currentTimeMillis() - currentMillis) + " ms")
                throw QueryExecutionException(sql, e)
            }
            if (logger.isTraceEnabled) {
                logger.trace(it.pathExpression + " -> " + sql)
                logger.trace("executed statement in " + (System.currentTimeMillis() - currentMillis) + " ms")
            }

            PolicyAndResult(it, res)
        }

        if (logger.isDebugEnabled)
            logger.debug("${loadingPolicies.size} select(s) for cluster took " + (System.currentTimeMillis() - ms) + " ms")

        val result = results.fold(mutableSetOf<Any>()) { set, policyAndResult ->
            val resultReader = VertxResultSetReader(policyAndResult.result)
            val lpb = LoadingPolicyObjectBuilder(
                policyAndResult.loadingPolicy, loaderContext,
                resultReader
            )
            for (m in lpb.createObjects()) {
                if (m != null && !set.contains(m)) {
                    set.add(m)
                }
            }
            set
        }

        loaderContext.finishedLoading()
        if (logger.isDebugEnabled)
            logger.debug("loaded cluster in " + (System.currentTimeMillis() - ms) + " ms")

        @Suppress("UNCHECKED_CAST")
        return result.toList() as List<T>
    }

    private data class PolicyAndResult(val loadingPolicy: LoadingPolicy, val result: RowSet<Row>)
}