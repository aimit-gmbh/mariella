import at.aimit.mariella.jakartaVersion
import at.aimit.mariella.slf4jVersion

plugins {
    id("at.aimit.mariella.java-conventions")
}

dependencies {
    api(project(":persistence-mapping"))
    api("org.slf4j:slf4j-api:$slf4jVersion")
    api("jakarta.persistence:jakarta.persistence-api:$jakartaVersion")

    implementation("org.javassist:javassist:3.30.2-GA")
    implementation("com.google.guava:guava:33.4.8-jre")
}
