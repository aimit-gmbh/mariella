# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Mariella is a JPA-compliant ORM for Java and data class mapper for Kotlin. It supports H2, PostgreSQL, and Oracle
databases with async/reactive support via Kotlin coroutines and Vert.x.

## Build Commands

```bash
# Run all tests (uses H2 by default)
./gradlew test

# Run tests with PostgreSQL
MARIELLA_TEST_DB=POSTGRES ./gradlew test

# Clean build and publish to local Maven
./gradlew clean build publishToMavenLocal

# Check for dependency updates
./gradlew dependencyUpdates
```

**Requirements:** JDK 21, Gradle 9.3.0 (via wrapper)

## Module Structure

- **persistence** - Core ORM engine, entity management, annotations
- **persistence-mapping** - Pure mapping data structures (JPA mapping definitions)
- **persistence-kotlin** - Kotlin async API with coroutines support via Vert.x
- **persistence-jdbc** - JDBC connection providers
- **persistence-h2** / **persistence-postgres** / **persistence-oracle** - Database-specific implementations
- **persistence-test** - Shared test utilities (not published)

## Architecture

### Key Packages (in `persistence`)

- **mapping** - Core ORM layer: `ClassMapping` (class-to-table), `PropertyMapping` (property-to-column),
  `SchemaMapping` (root container)
- **persistor** - Persistence strategies: `ObjectPersistor`, `SimplePersistorStrategy`, `BatchingPersistorStrategy`
- **loader** - Object loading: `ClusterLoader` (efficient relationship loading), `LoadingPolicy`
- **runtime** - Change tracking: `ModificationTracker`, observable collections
- **bootstrap** - Configuration: `Environment`, `ConnectionProvider` implementations
- **annotations_processing** - Schema building: `PersistenceUnitParser`, `ClasspathBrowser`

### Kotlin API (`persistence-kotlin`)

All operations are suspend functions. Main entry points:

- `Database.connect()` / `connectReadOnly()` / `connectAutoCommit()` - Connection factories
- `mariella()` - Entity context for CRUD operations
- `mapper()` - SQL mapper for raw queries
- `read()` / `write()` - Convenience transaction methods

## Testing

- **Framework:** JUnit Jupiter 6.x with Strikt assertions
- **Databases:** H2 (default) and PostgreSQL (set `MARIELLA_TEST_DB=POSTGRES`)
- **Coverage thresholds:** 83% (H2), 85% (PostgreSQL)
- **Example test:** `NoMagicTest.kt` in persistence-kotlin demonstrates full Mariella complexity

## Build Conventions

Build convention plugins in `buildSrc/`:

- `at.aimit.mariella.java-conventions` - Java 21, JUnit, Maven publishing
- `at.aimit.mariella.kotlin-conventions` - Extends Java conventions, adds Kotlin 2.3.0

Version catalog in `gradle/libs.versions.toml` manages all dependency versions.
