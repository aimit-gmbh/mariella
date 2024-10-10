plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin", "2.0.21"))
    implementation("com.github.ben-manes:gradle-versions-plugin:0.51.0")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.8.3")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.29.0")
}
