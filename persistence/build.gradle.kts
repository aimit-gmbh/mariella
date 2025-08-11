plugins {
    id("at.aimit.mariella.java-conventions")
}

dependencies {
    api(project(":persistence-mapping"))
    api("org.slf4j:slf4j-api:2.0.17")
    api("jakarta.persistence:jakarta.persistence-api:3.2.0")

    implementation("org.javassist:javassist:3.30.2-GA")
    implementation("com.google.guava:guava:33.4.8-jre")
}
