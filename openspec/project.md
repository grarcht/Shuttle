---
title: Shuttle Project
version: 4.0
status: active
lastUpdated: 2026-07-29
---

# Shuttle

## What Shuttle Is

Prevent `TransactionTooLargeException` crashes. For good. Shuttle eliminates the crash class by design so teams stop spending time and money on crash triage, hotfixes, and code review governance that never fully holds. It stores large `Serializable` objects in a local warehouse and passes only a small cargo ID through the binder layer, keeping every transaction within safe limits in dev, QA, and production. No custom DB setup. No table management. Ship with confidence.

Shuttle is a modern, layered Solution Building Block (SBB) framework and solution. Each module layer has a clear responsibility and forces no technology choices on consumers.

## The Problem

Android's binder transaction buffer is limited to roughly 1 MB. Passing large `Serializable` or `Parcelable` objects directly in `Intent` extras or `Bundle`s silently succeeds in dev and QA environments but crashes in production when memory pressure reduces the buffer. The crash is non-deterministic and difficult to reproduce. Code review governance does not reliably catch it.

## The Solution

Shuttle replaces direct binder-layer object transport with a warehouse pattern:

1. The source component stores the large `Serializable` in the warehouse and receives a cargo ID.
2. Only the cargo ID (a small string) is passed in the `Intent` or `Bundle`.
3. The destination component retrieves the full object from the warehouse using that ID.
4. Cargo is automatically or explicitly removed from the warehouse after pickup.

## Modules

| Module | Artifact ID | Status |
|---|---|---|
| Core interfaces and transport logic | `framework` | Required |
| Persistence abstraction interfaces | `framework-integrations-persistence` | Required |
| Room-backed persistence implementation | `framework-integrations-extensions-room` | Default (swappable) |
| Navigation Component integration | `framework-addons-navigation-component` | Optional |
| `@ShuttleCargo` annotation | `framework-annotations` | Recommended |
| KSP annotation processor | `framework-annotations-processor` | Recommended |
| Gradle plugin (wires KSP automatically) | `com.grarcht.shuttle.cargo` plugin | Recommended |

## Group ID and Current Version

```
Group: com.grarcht.shuttle
BOM:   com.grarcht.shuttle:framework-bom:4.0.0
```

## Tech Stack

- Kotlin 2.2.10
- Android minSdk 26
- Kotlin Coroutines and Channels (no RxJava, no LiveData)
- Room (default persistence; swappable via abstraction layer)
- KSP (required only for `@ShuttleCargo` annotation)
- Java 21

## Key Design Decisions

- **Structural prevention**: the crash class is eliminated by design, not policed by code review governance.
- **SBB layering**: each module is a Solution Building Block. Consumers build on top without being locked into any layer's implementation.
- **Single source of truth**: the `Shuttle` interface is the one entry point for all cargo transport operations.
- **No reactive library bundling**: async results flow through `Channel`-backed sealed classes only.
- **Swappable persistence**: `ShuttleWarehouse` is an interface. Room is the default, not a hard dependency.
- **LCE pattern**: all async results are sealed classes with `Loading`, `Content/Success`, and `Error` states.
- **Singleton Shuttle**: `CargoShuttle` is meant to be provided as a singleton (Hilt, Koin, or manual).
