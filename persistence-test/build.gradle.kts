import at.aimit.mariella.*

plugins {
    id("at.aimit.mariella.java-conventions")
}

dependencies {
    testImplementation(project(":persistence-mapping"))
    testImplementation(project(":persistence"))
    testImplementation(project(":persistence-jdbc"))
    testImplementation(project(":persistence-h2"))
    testImplementation(project(":persistence-postgres"))

    testImplementation("ch.qos.logback:logback-classic:$logbackVersion")

    //h2
    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("com.zaxxer:HikariCP:$hikariVersion")

    // postgres
    testImplementation("org.postgresql:postgresql:$postgresVersion")
    testImplementation("com.ongres.scram:client:$scramClientVersion")

    // flyway
    testImplementation("org.flywaydb:flyway-core:$flywayVersion")
    testImplementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
}
