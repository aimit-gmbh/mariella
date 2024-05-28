plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin", "2.0.0"))
    implementation("com.github.ben-manes:gradle-versions-plugin:0.51.0")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.7.6")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.28.0")
}
