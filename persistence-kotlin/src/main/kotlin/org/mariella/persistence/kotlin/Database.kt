package org.mariella.persistence.kotlin

interface Database {
    suspend fun connect(): TransactionalConnection
    suspend fun connectReadOnly(): ReadOnlyConnection
    suspend fun connectAutoCommit(): AutoCommitConnection
}