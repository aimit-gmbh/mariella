plugins {
    id("at.aimit.mariella.kotlin-conventions")
}

dependencies {
    api(project(":persistence"))
    api(libs.vertx.sql.client)
    implementation(libs.kotlinx.coroutines.core)

    implementation(project(":persistence-mapping"))
    implementation(libs.vertx.lang.kotlin.coroutines)
    implementation(libs.vertx.core)
    implementation(libs.slf4j)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(project(":persistence-h2"))
    testRuntimeOnly(project(":persistence-postgres"))
    testImplementation(libs.jakarta.persistence.api)

    testRuntimeOnly(libs.logback)

    //h2
    testImplementation(libs.h2)
    testImplementation(libs.hikari.cp)
    testImplementation(libs.vertx.jdbc.client)

    // postgres
    testRuntimeOnly(libs.postgres)
    testRuntimeOnly(libs.scram.client)
    testImplementation(libs.vertx.pg.client)

    // flyway
    testImplementation(libs.flyway.core)
    testRuntimeOnly(libs.flyway.database.postgresql)

    testImplementation(libs.strikt.core)
}


// tests with postgres have more coverage because of several postgres specific features
val coverage = if ("POSTGRES" == System.getenv("MARIELLA_TEST_DB")) 85 else 82

kover {
    reports {
        verify {
            rule {
                bound {
                    minValue = coverage
                }
            }
        }
    }
}