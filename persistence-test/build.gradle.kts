import at.aimit.mariella.*

plugins {
    id("at.aimit.mariella.java-conventions")
}

dependencies {
    testImplementation(project(":persistence-mapping"))
    testImplementation(project(":persistence"))
    testImplementation(project(":persistence-jdbc"))
    testRuntimeOnly(project(":persistence-h2"))
    testRuntimeOnly(project(":persistence-postgres"))

    testRuntimeOnly("ch.qos.logback:logback-classic:$logbackVersion")

    //h2
    testImplementation("com.h2database:h2:$h2Version")

    // postgres
    testRuntimeOnly("org.postgresql:postgresql:$postgresVersion")
    testRuntimeOnly("com.ongres.scram:scram-client:$scramClientVersion")

    testImplementation("jakarta.persistence:jakarta.persistence-api:$jakartaVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.slf4j:slf4j-api:$slf4jVersion")

}
