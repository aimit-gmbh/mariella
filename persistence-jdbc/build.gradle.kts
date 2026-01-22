plugins {
    id("at.aimit.mariella.java-conventions")
}

dependencies {
    api(project(":persistence"))
    implementation(libs.jakarta.persistence.api)
    implementation(libs.slf4j)
}
