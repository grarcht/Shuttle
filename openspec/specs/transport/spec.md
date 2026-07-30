---
title: Shuttle Cargo Transport
version: 4.0
domain: transport
status: active
lastUpdated: 2026-07-29
---

# Shuttle Cargo Transport

## Overview

This spec covers how source components transport large `Serializable` objects to destination components using Shuttle, keeping binder transactions within safe size limits.

---

## Requirements

### Requirement: Intent-Based Transport

Use `ShuttleIntent` when the destination is an `Activity`, `Service`, or `BroadcastReceiver` launched by an `Intent`.

#### Scenario: Basic intent transport
- **GIVEN** a source Fragment or Activity with an injected `Shuttle` instance
- **WHEN** the developer calls `intentCargoWith` and chains `.transport()`, `.cleanShuttleOnReturnTo()`, and `.deliver()`
- **THEN** the large object is stored in the warehouse, a small cargo ID is embedded in the Intent, and the destination Activity is started

```kotlin
val cargoId = DataMessageType.ImageData.value  // use a named constant or enum value

shuttle.intentCargoWith(context, DestinationActivity::class.java)
    .transport(cargoId, cargoData)
    .cleanShuttleOnReturnTo(
        SourceFragment::class.java,
        DestinationActivity::class.java,
        cargoId
    )
    .deliver(context)
```

#### Scenario: Intent with action and URI
- **GIVEN** the developer needs a custom action or data URI on the intent
- **WHEN** the developer uses the `intentCargoWith(action, uri)` overload
- **THEN** the resulting `ShuttleIntent` carries both the action/URI and the cargo ID

#### Scenario: Intent chooser transport
- **GIVEN** the developer wants to show an app chooser
- **WHEN** the developer uses `intentChooserCargoWith(target, title)` or `intentChooserCargoWith(target, title, sender)`
- **THEN** a chooser intent is constructed with the cargo ID embedded in the target

### Requirement: Bundle-Based Transport

Use `ShuttleBundle` when passing data within a Fragment transaction or as Activity result extras.

#### Scenario: Bundle transport
- **GIVEN** a source that uses a `Bundle` to pass arguments to a Fragment
- **WHEN** the developer calls `bundleCargoWith()` and chains `.transport()`
- **THEN** the large object is stored in the warehouse and the bundle contains only the cargo ID

```kotlin
val args = shuttle.bundleCargoWith()
    .transport(cargoId, cargoData)
    .create()
MyFragment.newInstance(args)
```

### Requirement: Navigation Component Transport

Use `navigateWithShuttle` when the project uses Jetpack Navigation Component for screen navigation.

Requires the `framework-addons-navigation-component` module.

#### Scenario: Navigation Component transport
- **GIVEN** a source Fragment with an injected `Shuttle` and a `NavController`
- **WHEN** the developer calls `navController.navigateWithShuttle(shuttle, destinationId)`
- **THEN** the cargo is stored in the warehouse and navigation proceeds with a safe-sized argument bundle

```kotlin
navController.navigateWithShuttle(shuttle, R.id.destinationFragment)
    ?.logTag(LOG_TAG)
    ?.transport(cargoId, cargoData)
    ?.cleanShuttleOnReturnTo(
        SourceFragment::class.java,
        DestinationActivity::class.java,
        cargoId
    )
    ?.deliver()
```

### Requirement: Cargo ID Naming

Cargo IDs must be stable, named constants rather than anonymous string literals, so source and destination always agree on the ID without tight coupling.

#### Scenario: Enum-based cargo ID
- **GIVEN** a sealed class or enum defines cargo ID constants
- **WHEN** both source and destination reference the same constant
- **THEN** no string literal mismatch is possible

```kotlin
enum class DataMessageType(val value: String) {
    UserProfile("user_profile_cargo"),
    ImageData("image_data_cargo")
}
```

### Reference: Store Result States

`ShuttleStoreCargoResult` is the sealed class used internally by the warehouse during a store operation. Its states are:

| State | Meaning |
|---|---|
| `ShuttleStoreCargoResult.Storing` | Write in progress |
| `ShuttleStoreCargoResult.Success` | Object stored; cargo ID is valid |
| `ShuttleStoreCargoResult.Error` | Storage failed |

Note: `.transport()` on `ShuttleIntent`, `ShuttleBundle`, and `ShuttleNavController` returns the fluent builder for chaining, not this channel. The store result channel is internal to the warehouse layer and not directly observable through the standard transport API.
