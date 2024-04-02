package org.mariella.persistence.kotlin.internal

import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Transaction

internal class VertxConnectionAndTransaction(
    val sqlClient: SqlClient,
    private val transaction: Transaction?
) {

    suspend fun commit() {
        transaction!!.commit().coAwait()
    }

    suspend fun rollback() {
        transaction!!.rollback().coAwait()
    }

    suspend fun close() {
        sqlClient.close().coAwait()
    }

    suspend fun commitAndClose() {
        try {
            commit()
        } finally {
            close()
        }
    }

    suspend fun rollbackAndClose() {
        try {
            rollback()
        } finally {
            close()
        }
    }
}