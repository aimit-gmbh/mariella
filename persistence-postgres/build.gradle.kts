plugins {
    id("at.aimit.mariella.java-conventions")
}

dependencies {
    implementation(project(":persistence-mapping"))
    implementation(project(":persistence"))
    implementation(project(":persistence-jdbc"))
}
