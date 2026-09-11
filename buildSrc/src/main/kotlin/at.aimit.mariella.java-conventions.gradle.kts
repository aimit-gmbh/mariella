import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
    id("io.github.ben-manes.versions")
    id("com.autonomousapps.dependency-analysis")
}

repositories {
    mavenCentral()
}

tasks.withType(JavaCompile::class) {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

@Suppress("UnstableApiUsage")
testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter(libs.findLibrary("junit-jupiter-api").get().get().version!!)
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

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    gradleReleaseChannel = "current"
    outputFormatter = "json"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
    checkConstraints = true
    filterConfigurations = Spec<Configuration> {
        it.name == "runtimeClasspath" || it.name == "compileClasspath" || it.name == "testRuntimeClasspath" || it.name == "testCompileClasspath"
    }

    rejectVersionIf {
        candidate.version.isNonStable()
    }
}

fun String.isNonStable(): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r|-jre|-android)?$".toRegex()
    val isStable = stableKeyword || regex.matches(this)
    return isStable.not()
}


mavenPublishing {
    coordinates("at.aimit.mariella", project.name, System.getenv("MARIELLA_RELEASE_NAME") ?: "1.0-SNAPSHOT")
    signAllPublications()
    pom {
        name.set("Mariella ${project.name}")
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

pluginManager.withPlugin("com.autonomousapps.dependency-analysis") {
    configure<com.autonomousapps.DependencyAnalysisSubExtension> {
        issues {
            onUnusedDependencies {
                exclude("org.junit.jupiter:junit-jupiter")
            }
        }
    }
}
