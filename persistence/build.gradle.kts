plugins {
    id("at.aimit.mariella.java-conventions")
}

dependencies {
    api(project(":persistence-mapping"))
    api(libs.slf4j)
    api(libs.jakarta.persistence.api)

    implementation(libs.javassist)
    implementation(libs.guava)
}
