plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin", "2.2.10"))
    implementation("com.github.ben-manes:gradle-versions-plugin:0.52.0")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.9.1")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.34.0")
    implementation("com.autonomousapps:dependency-analysis-gradle-plugin:2.19.0")
}
