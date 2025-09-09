package org.mariella.persistence.kotlin

import io.vertx.sqlclient.SqlClient

interface Connection<T : ReadOnlyMariella> {
    val sqlClient: SqlClient
    fun mapper(): Mapper
    fun mariella(): T
    suspend fun close()
}

interface ReadOnlyConnection : Connection<ReadOnlyMariella>

interface AutoCommitConnection : Connection<Mariella>

interface TransactionalConnection : Connection<Mariella> {
    suspend fun commit()
    suspend fun rollback()
}