import at.aimit.mariella.vertxVersion

plugins {
    id("at.aimit.mariella.kotlin-conventions")
}

dependencies {
    implementation(project(":persistence-mapping"))
    implementation(project(":persistence"))
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    implementation("io.vertx:vertx-sql-client:$vertxVersion")
}
