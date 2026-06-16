# mariella

JPA inspired ORM framework for java with a JDBC implementation

# mariella kotlin

more info [here](/persistence-kotlin)

# development

## dependencies

JDK >= 21

IntelliJ IDEA / Eclipse (limited kotlin support)

## gradle tasks

`./gradlew test` to run the tests with an H2 database

`MARIELLA_TEST_DB=POSTGRES ./gradlew test` to run the tests with a PostgreSQL database

`docker run -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres` to start a PostgreSQL database for tests in docker

`./gradlew clean build publishToMavenLocal` to publish the jars to the local maven repo

## tests

java tests can be found [here](persistence-test/src/test/java/org/mariella/test)

kotlin tests can be found [here](persistence-kotlin/src/test/kotlin/org/mariella/persistence/kotlin)
