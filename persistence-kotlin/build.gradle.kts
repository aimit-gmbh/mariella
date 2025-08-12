import at.aimit.mariella.*

plugins {
    id("at.aimit.mariella.kotlin-conventions")
}

dependencies {
    api(project(":persistence"))
    api("io.vertx:vertx-sql-client:$vertxVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    implementation(project(":persistence-mapping"))
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${coroutinesVersion}")
    testRuntimeOnly(project(":persistence-h2"))
    testRuntimeOnly(project(":persistence-postgres"))
    testImplementation("jakarta.persistence:jakarta.persistence-api:$jakartaVersion")

    testRuntimeOnly("ch.qos.logback:logback-classic:$logbackVersion")

    //h2
    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("com.zaxxer:HikariCP:$hikariVersion")
    testImplementation("io.vertx:vertx-jdbc-client:$vertxVersion")

    // postgres
    testRuntimeOnly("org.postgresql:postgresql:$postgresVersion")
    testRuntimeOnly("com.ongres.scram:scram-client:$scramClientVersion")
    testImplementation("io.vertx:vertx-pg-client:$vertxVersion")

    // flyway
    testImplementation("org.flywaydb:flyway-core:$flywayVersion")
    testRuntimeOnly("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    testImplementation("io.strikt:strikt-core:0.35.1")
}

kover {
    reports {
        verify {
            rule {
                bound {
                    minValue = 86
                }
            }
        }
    }
}