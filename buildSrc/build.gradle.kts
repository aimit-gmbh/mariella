plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin", "1.9.23"))
    implementation("com.github.ben-manes:gradle-versions-plugin:0.50.0")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.7.4")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.27.0")
}
