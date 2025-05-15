plugins {
    id("at.aimit.mariella.java-conventions")
}

dependencies {
    implementation(project(":persistence-mapping"))

    implementation("org.javassist:javassist:3.30.2-GA")
    implementation("com.google.guava:guava:33.4.8-jre")
}
