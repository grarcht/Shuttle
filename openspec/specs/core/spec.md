---
title: Shuttle Core Framework
version: 4.0
domain: core
status: active
lastUpdated: 2026-07-29
---

# Shuttle Core Framework

## Overview

Shuttle is a modern, layered Solution Building Block (SBB) framework and solution. Each layer has a clear responsibility and forces no technology choices on consumers. The `Shuttle` interface is the single entry point for all cargo transport operations. Together, the `Shuttle` interface, `CargoShuttle` implementation, and the warehouse abstraction structurally prevent `TransactionTooLargeException` crashes. The crash class is eliminated by design, not governed against through code review.

---

## Requirements

### Requirement: Shuttle Interface as Single Entry Point

All cargo operations flow through the `Shuttle` interface. Consumer code MUST depend on the interface, not on `CargoShuttle` directly.

#### Scenario: Interface injection
- **GIVEN** `Shuttle` is the declared type at all injection points
- **WHEN** the DI framework provides a `CargoShuttle` instance
- **THEN** swapping in a test double or alternative implementation requires no changes to consumer call sites

### Requirement: CargoShuttle as Default Implementation

`CargoShuttle` is the production implementation of `Shuttle`. It requires a `ShuttleFacade` and a `ShuttleWarehouse` at construction time.

```kotlin
val shuttle: Shuttle = CargoShuttle(shuttleFacade, shuttleWarehouse)
```

### Requirement: ShuttleWarehouse Abstraction

`ShuttleWarehouse` is an interface. The Room-backed `ShuttleRepository` is the default implementation. Consumers may provide their own implementation to use a different storage backend.

#### Scenario: Default Room-backed warehouse
- **GIVEN** `ShuttleRepository` is provided via DI with a valid `ShuttleDataAccessObject`
- **WHEN** Shuttle stores or retrieves cargo
- **THEN** the data is persisted in and read from a Room database in the app's private file directory

### Requirement: ShuttleFacade for Screen Lifecycle

Shuttle uses `ShuttleFacade` (implemented by `ShuttleCargoFacade`) to register Activity lifecycle callbacks. This is what makes automatic cleanup via `cleanShuttleOnReturnTo` work without any destination-side code.

### Requirement: LCE Results

All async operations return `Channel`-backed sealed class results following the Loading-Content-Error (LCE) pattern.

| Operation | Return Type | Terminal States |
|---|---|---|
| Store cargo | `Channel<ShuttleStoreCargoResult>` | `Success`, `Error` |
| Pick up cargo | `Channel<ShuttlePickupCargoResult>` | `Success`, `Error` |
| Remove cargo | `Channel<ShuttleRemoveCargoResult>` | `Removed`, `UnableToRemove`, `DoesNotExist` |

Remove channels close automatically when a terminal state is emitted, via `CargoShuttle`'s `finally` block. Pickup channels with `timeoutMs > 0` also close automatically on timeout. Pickup channels without a timeout and Store channels do not close automatically. Consumers must call `cancel()` inside `collectLatest` to terminate after a terminal result. See CLAUDE.md for full context.

### Requirement: No Reactive Library Dependency

All async communication uses Kotlin Coroutines and Channels only. No RxJava, LiveData, or other reactive libraries are bundled as transitive dependencies.

### Requirement: Binder Transaction Safety

No `Serializable` object larger than a safe threshold is ever placed directly in a binder transaction. Shuttle stores objects in the warehouse and passes only a small string cargo ID through the binder layer.

#### Scenario: Large object transport
- **GIVEN** a `Serializable` object whose serialized size would exceed Android's binder buffer limit (around 1 MB) under memory pressure
- **WHEN** the developer uses `shuttle.intentCargoWith(...).transport(cargoId, obj).deliver(context)`
- **THEN** the object is written to the warehouse and only the cargo ID (a few dozen bytes) travels in the `Intent` extra, keeping the binder transaction within safe limits

#### Scenario: Crash-free delivery in production
- **GIVEN** Shuttle is used consistently for all large-object inter-component transport
- **WHEN** production memory pressure reduces the available binder buffer below 1 MB
- **THEN** no `TransactionTooLargeException` is thrown, because no large object is in the binder transaction

### Requirement: Module Dependency Isolation

Modules are layered so consumers depend only on what they need.

```
framework (core)
    └── framework-integrations-persistence (abstraction)
            └── framework-integrations-extensions-room (Room impl, swappable)
framework-addons-navigation-component (optional)
framework-annotations + processor + gradle plugin (recommended)
```

No module introduces a circular dependency or forces an unused transitive dependency on consumers.
