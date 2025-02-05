import at.aimit.mariella.*

plugins {
    id("at.aimit.mariella.kotlin-conventions")
}

dependencies {
    implementation(project(":persistence-mapping"))
    implementation(project(":persistence"))
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    implementation("io.vertx:vertx-sql-client:$vertxVersion")

    testImplementation(project(":persistence-jdbc"))
    testImplementation(project(":persistence-h2"))
    testImplementation(project(":persistence-postgres"))
    testImplementation(project(":persistence-kotlin"))

    testImplementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
    testImplementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")

    //h2
    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("com.zaxxer:HikariCP:$hikariVersion")
    testImplementation("io.vertx:vertx-jdbc-client:$vertxVersion")

    // postgres
    testImplementation("org.postgresql:postgresql:$postgresVersion")
    testImplementation("com.ongres.scram:scram-client:$scramClientVersion")
    testImplementation("io.vertx:vertx-pg-client:$vertxVersion")

    // flyway
    testImplementation("org.flywaydb:flyway-core:$flywayVersion")
    testImplementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    testImplementation("io.strikt:strikt-core:0.35.1")
}
