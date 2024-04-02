plugins {
    id("com.gradleup.nmcp") version "0.0.4"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

nmcp {
    publishAllProjectsProbablyBreakingProjectIsolation {
        username = System.getenv("MAVEN_CENTRAL_USER")
        password = System.getenv("MAVEN_CENTRAL_PW")
        publicationType = "AUTOMATIC"
    }
}