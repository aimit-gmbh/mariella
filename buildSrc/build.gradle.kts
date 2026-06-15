plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin", "2.4.0"))
    implementation("com.github.ben-manes:gradle-versions-plugin:0.53.0")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.9.8")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.36.0")
    implementation("com.autonomousapps:dependency-analysis-gradle-plugin:3.6.1")
}
