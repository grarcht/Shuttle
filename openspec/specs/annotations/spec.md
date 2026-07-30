---
title: Shuttle @ShuttleCargo Annotation
version: 4.0
domain: annotations
status: active
lastUpdated: 2026-07-29
---

# Shuttle @ShuttleCargo Annotation

## Overview

This spec covers the `@ShuttleCargo` annotation, the KSP processor, and the Gradle plugin that together let data classes be transported through Shuttle without any manual `Serializable` boilerplate.

---

## Requirements

### Requirement: Annotation Declaration

Annotate any data class with `@ShuttleCargo` to make it eligible for Shuttle transport. The annotation processor and compiler plugin automatically inject the `ShuttleCargoData` supertype (and transitively `java.io.Serializable`) at compile time. No explicit `implements Serializable` or `extends ShuttleCargoData` declaration is needed.

#### Scenario: Annotated data class
- **GIVEN** a data class annotated with `@ShuttleCargo`
- **WHEN** the project is compiled with the Shuttle Cargo Gradle plugin active
- **THEN** the class gains `ShuttleCargoData` at the bytecode level and is usable with `shuttle.transport()`

```kotlin
@ShuttleCargo
data class ImageModel(
    val id: String,
    val title: String,
    val byteArray: ByteArray
)
```

### Requirement: Build Configuration

Apply the Shuttle Cargo Gradle plugin and add the annotation dependencies to the project.

**`settings.gradle.kts`:**
```kotlin
pluginManagement {
    plugins {
        id("com.grarcht.shuttle.cargo") version "4.0.0"
    }
}
```

**`build.gradle.kts`:**
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

The Gradle plugin configures KSP and registers the Shuttle compiler plugin automatically. Without it, `@ShuttleCargo`-annotated classes will not generate the required serialization code.

### Requirement: Annotation vs Manual Serializable

| Approach | When to Use |
|---|---|
| `@ShuttleCargo` | New model classes owned by the consuming project |
| Manual `Serializable` | Third-party or legacy classes already implementing `Serializable` |

Either approach works with the same `shuttle.transport()` and `shuttle.pickupCargo<T>()` call sites. The annotation approach removes the boilerplate for new classes.

### Requirement: No Runtime Reflection

All serialization code is generated at compile time. `@ShuttleCargo` produces no runtime reflection overhead.

#### Scenario: Compile-time code generation
- **GIVEN** `@ShuttleCargo` is applied to a data class
- **WHEN** the KSP processor and compiler plugin run during the build
- **THEN** the generated code is present in the compiled output with no annotation processing at runtime
