plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin", "2.4.20"))
    implementation("com.github.ben-manes.versions:com.github.ben-manes.versions.gradle.plugin:0.61.0")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.9.9")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.36.0")
    implementation("com.autonomousapps:dependency-analysis-gradle-plugin:3.19.1")
}
