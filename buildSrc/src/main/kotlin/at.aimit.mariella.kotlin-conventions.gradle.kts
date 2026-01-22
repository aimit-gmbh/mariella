import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
    id("at.aimit.mariella.java-conventions")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")!!

dependencies {
    // Align versions of all Kotlin components
    implementation(platform(libs.findLibrary("kotlin-bom").get()))

    // Use the Kotlin JDK 8 standard library
    implementation(libs.findLibrary("kotlin-stdlib-jdk8").get())
    implementation(libs.findLibrary("kotlin-reflect").get())
}

tasks.withType(KotlinCompile::class) {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjsr305=strict")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
}
