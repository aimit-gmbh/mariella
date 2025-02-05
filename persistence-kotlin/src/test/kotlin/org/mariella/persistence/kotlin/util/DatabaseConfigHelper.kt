package org.mariella.persistence.kotlin.util


import java.util.*

object DatabaseConfigHelper {
    fun createDefaultConfig(databaseType: DatabaseType): DatabaseConfig {
        val config = when (databaseType) {
            DatabaseType.H2 -> DatabaseConfig(
                databaseType,
                "./build/tmp/database/${UUID.randomUUID()}",
                "sa",
                "",
                urlParams = ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
                persistenceUnit = "repong/h2"
            )

            DatabaseType.H2_MEM -> DatabaseConfig(
                databaseType,
                UUID.randomUUID().toString(),
                "sa",
                "",
                urlParams = ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
                persistenceUnit = "repong/h2"
            )

            DatabaseType.POSTGRES -> DatabaseConfig(
                databaseType,
                "a" + UUID.randomUUID().toString().replace("-", ""),
                "postgres",
                "postgres",
                "localhost",
                5432,
                persistenceUnit = "repong/h2"
            )
        }
        return config
    }
}
