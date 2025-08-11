import at.aimit.mariella.jakartaVersion
import at.aimit.mariella.slf4jVersion
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
    id("com.github.ben-manes.versions")
    id("com.autonomousapps.dependency-analysis")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("jakarta.persistence:jakarta.persistence-api:$jakartaVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
}

tasks.withType(JavaCompile::class) {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

testing {
    suites {
        // Configure the built-in test suite
        @Suppress("UnstableApiUsage", "Unused") val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.13.4")
        }
    }
}

tasks.withType(Test::class) {
    minHeapSize = "512m"
    maxHeapSize = "1024m"
    failFast = true

    testLogging {
        events(TestLogEvent.FAILED)
        exceptionFormat = TestExceptionFormat.FULL
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
    gradleReleaseChannel = "current"
    outputFormatter = "json"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
}

mavenPublishing {
    coordinates("at.aimit.mariella", project.name, System.getenv("MARIELLA_RELEASE_NAME") ?: "1.0-SNAPSHOT")
    signAllPublications()
    pom {
        name = "Mariella ${project.name}"
        description = "JPA compliant ORM for Java and data class mapper for Kotlin"
        url = "https://github.com/aimit-gmbh/mariella"
        licenses {
            license {
                name = "MIT license"
                url = "https://opensource.org/license/mit"
            }
        }
        developers {
            developer {
                id = "ssadat-guscheh-aimit"
                name = "Sascha Sadat-Guscheh"
                email = "sascha.sadat-guscheh@scinteco.com"
            }
        }
        scm {
            url = "https://github.com/aimit-gmbh/mariella"
        }
    }
}
