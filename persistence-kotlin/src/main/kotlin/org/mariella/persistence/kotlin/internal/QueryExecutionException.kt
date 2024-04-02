package org.mariella.persistence.kotlin.internal

internal class QueryExecutionException(sql: String, cause: Throwable) : RuntimeException("Failed to execute statement: $sql", cause)