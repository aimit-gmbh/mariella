plugins {
    id("at.aimit.mariella.java-conventions")
}

dependencies {
    testImplementation(project(":persistence-mapping"))
    testImplementation(project(":persistence"))
    testImplementation(project(":persistence-jdbc"))
    testRuntimeOnly(project(":persistence-h2"))
    testRuntimeOnly(project(":persistence-postgres"))

    testRuntimeOnly(libs.postgres)

    //h2
    testImplementation(libs.h2)

    // postgres
    testRuntimeOnly(libs.postgres)
    testRuntimeOnly(libs.scram.client)

    testImplementation(libs.jakarta.persistence.api)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.slf4j)

}
