package org.mariella.persistence.kotlin.util

enum class DatabaseType {
    H2, H2_MEM, POSTGRES
}

data class DatabaseConfig(
    val type: DatabaseType,
    val database: String,
    val user: String,
    val password: String,
    val host: String = "",
    val port: Int = 0,
    val urlParams: String = "",
    val poolSize: Int = 10,
    val persistenceUnit: String = "sample/h2",
    val flywayMigrations: List<String> = listOf("db/core")
) {
    fun getUrl(): String {
        return when (type) {
            DatabaseType.H2 -> "jdbc:h2:$database$urlParams"
            DatabaseType.H2_MEM -> "jdbc:h2:mem:$database$urlParams"
            DatabaseType.POSTGRES -> "jdbc:postgresql://$host:$port/$database"
        }
    }
}