package org.mariella.persistence.kotlin

interface Database {
    suspend fun connect(): TransactionalConnection
    suspend fun connectReadOnly(): ReadOnlyConnection
    suspend fun connectAutoCommit(): AutoCommitConnection

    suspend fun <T> read(block: suspend ReadOnlyConnection.() -> T): T {
        val connection = connectReadOnly()
        try {
            return block(connection)
        } finally {
            connection.close()
        }
    }

    suspend fun <T> write(block: suspend TransactionalConnection.() -> T): T {
        val connection = connect()
        try {
            val ret = block(connection)
            connection.commit()
            return ret
        } finally {
            connection.close()
        }
    }
}