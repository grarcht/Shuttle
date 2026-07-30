---
title: Shuttle Setup and Initialization
version: 4.0
domain: setup
status: active
lastUpdated: 2026-07-29
---

# Shuttle Setup and Initialization

## Overview

This spec covers how a consuming Android project adds Shuttle dependencies and wires up the `Shuttle` singleton.

---

## Requirements

### Requirement: Gradle Plugin Declaration

Declare the Shuttle Cargo Gradle plugin in `settings.gradle.kts`. This wires up `@ShuttleCargo` annotation processing at build time.

```kotlin
// settings.gradle.kts
pluginManagement {
    plugins {
        id("com.grarcht.shuttle.cargo") version "4.0.0"
    }
}
```

### Requirement: BOM and Module Dependencies

Add the Shuttle BOM and recommended modules to the app-module `build.gradle.kts`. The `@ShuttleCargo` annotation is the recommended way to mark cargo classes and should always be included.

**Recommended setup:**
```kotlin
plugins {
    id("com.grarcht.shuttle.cargo")
}

dependencies {
    implementation(platform("com.grarcht.shuttle:framework-bom:4.0.0"))
    implementation("com.grarcht.shuttle:framework")
    implementation("com.grarcht.shuttle:framework-integrations-persistence")
    implementation("com.grarcht.shuttle:framework-integrations-extensions-room")
    implementation("com.grarcht.shuttle:framework-annotations")
    ksp("com.grarcht.shuttle:framework-annotations-processor")
}
```

**With Navigation Component (add to the above):**
```kotlin
implementation("com.grarcht.shuttle:framework-addons-navigation-component")
```

### Requirement: Version Catalog

Projects using `libs.versions.toml` should declare Shuttle entries as follows:

```toml
[versions]
shuttle = "4.0.0"

[plugins]
shuttle-cargo = { id = "com.grarcht.shuttle.cargo", version.ref = "shuttle" }

[libraries]
shuttle-bom               = { group = "com.grarcht.shuttle", name = "framework-bom",                             version.ref = "shuttle" }
shuttle-framework         = { group = "com.grarcht.shuttle", name = "framework" }
shuttle-persistence       = { group = "com.grarcht.shuttle", name = "framework-integrations-persistence" }
shuttle-room              = { group = "com.grarcht.shuttle", name = "framework-integrations-extensions-room" }
shuttle-navigation        = { group = "com.grarcht.shuttle", name = "framework-addons-navigation-component" }
shuttle-annotations       = { group = "com.grarcht.shuttle", name = "framework-annotations" }
shuttle-annotations-proc  = { group = "com.grarcht.shuttle", name = "framework-annotations-processor" }
```

### Requirement: Singleton Initialization

`CargoShuttle` must be provided as a singleton. The following Hilt module is the reference implementation.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ShuttleModule {

    @Provides @Singleton
    fun provideShuttle(facade: ShuttleFacade, warehouse: ShuttleWarehouse): Shuttle =
        CargoShuttle(facade, warehouse)

    @Provides @Singleton
    fun provideShuttleFacade(
        @ApplicationContext context: Context,
        warehouse: ShuttleWarehouse
    ): ShuttleFacade = ShuttleCargoFacade(context as Application, warehouse)

    @Provides @Singleton
    fun provideShuttleWarehouse(
        dao: ShuttleDataAccessObject,
        factory: ShuttleDataModelFactory,
        @ApplicationContext context: Context,
        gateway: ShuttleFileSystemGateway
    ): ShuttleWarehouse =
        ShuttleRepository(dao, factory, context.filesDir.absolutePath, gateway)

    @Provides @Singleton
    fun provideShuttleDao(
        @ApplicationContext context: Context
    ): ShuttleDataAccessObject =
        ShuttleRoomDataDb.getInstance(ShuttleRoomDbConfig(context)).shuttleDataAccessObject
}
```

#### Scenario: Hilt injection at usage site
- **GIVEN** the above DI module is registered
- **WHEN** an Activity or ViewModel declares `@Inject lateinit var shuttle: Shuttle`
- **THEN** `shuttle` is a fully initialized `CargoShuttle` instance ready to transport cargo

### Requirement: Custom Warehouse

To use a different storage backend, implement the `ShuttleWarehouse` interface and provide it via DI in place of `ShuttleRepository`.

#### Scenario: Custom persistence backend
- **GIVEN** the consumer implements `ShuttleWarehouse`
- **WHEN** the consumer provides their implementation as the `ShuttleWarehouse` binding
- **THEN** Shuttle uses the custom backend without any changes to transport or pickup call sites
