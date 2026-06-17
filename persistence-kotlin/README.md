# mariella kotlin

coroutines based kotlin API for mariella using the vert.x SQL client

supports kotlin.uuid.Uuid and kotlin.time.Instant (useful for multiplatform projects)

## JPA annotated classes

examples for mapped classes can be found in [here](/persistence-kotlin/src/test/kotlin/org/mariella/persistence/kotlin/entities)

usage can be found [here](/persistence-kotlin/src/test/kotlin/org/mariella/persistence/kotlin/BasicMariellaFeaturesTest.kt)
and [here](/persistence-kotlin/src/test/kotlin/org/mariella/persistence/kotlin/NoMagicTest.kt)

## SQL to data class mapper

a very simple mapping mechanism for SQL statements. check examples [here](/persistence-kotlin/src/test/kotlin/org/mariella/persistence/kotlin/MapperTest.kt)