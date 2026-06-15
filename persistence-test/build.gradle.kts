plugins {
    id("at.aimit.mariella.java-conventions")
    jacoco
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
    testRuntimeOnly(libs.logback)
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    // Include source directories for which we want the coverage
    val javaProjects = listOf(
        project(":persistence"),
        project(":persistence-mapping"),
        project(":persistence-jdbc"),
        project(":persistence-h2"),
        project(":persistence-postgres"),
        project(":persistence-oracle")
    )

    additionalSourceDirs.setFrom(javaProjects.flatMap { it.the<SourceSetContainer>()["main"].allSource.srcDirs })
    additionalClassDirs.setFrom(javaProjects.map { it.the<SourceSetContainer>()["main"].output })

    reports {
        xml.required = true
        html.required = true
    }
}
