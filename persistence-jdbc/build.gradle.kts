import at.aimit.mariella.jakartaVersion
import at.aimit.mariella.slf4jVersion

plugins {
    id("at.aimit.mariella.java-conventions")
}

dependencies {
    api(project(":persistence"))
    implementation("jakarta.persistence:jakarta.persistence-api:$jakartaVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
}
