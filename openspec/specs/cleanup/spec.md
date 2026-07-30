---
title: Shuttle Cargo Cleanup
version: 4.0
domain: cleanup
status: active
lastUpdated: 2026-07-29
---

# Shuttle Cargo Cleanup

## Overview

This spec covers how cargo is removed from the warehouse. Cleanup prevents unbounded warehouse growth and ensures stored objects do not accumulate after they have been delivered.

---

## Requirements

### Requirement: Automatic Cleanup on Back-Navigation

Chain `cleanShuttleOnReturnTo` at the source before calling `.deliver()`. Shuttle will automatically remove the cargo when the user navigates back from the destination. This is the preferred strategy because it requires no cleanup code at the destination.

#### Scenario: Back-navigation cleanup
- **GIVEN** `.cleanShuttleOnReturnTo(SourceActivity, DestinationActivity, cargoId)` was chained during transport
- **WHEN** the user finishes `DestinationActivity` and returns to `SourceActivity`
- **THEN** Shuttle automatically removes the cargo from the warehouse

```kotlin
shuttle.intentCargoWith(context, DestinationActivity::class.java)
    .transport(cargoId, cargoData)
    .cleanShuttleOnReturnTo(
        SourceActivity::class.java,
        DestinationActivity::class.java,
        cargoId
    )
    .deliver(context)
```

### Requirement: Manual Cleanup by Cargo ID

Call `cleanShuttleFromDeliveryFor(cargoId)` to manually remove a specific cargo item. The returned `Channel<ShuttleRemoveCargoResult>` can be observed or ignored for fire-and-forget behavior.

#### Scenario: Explicit removal after pickup
- **GIVEN** a destination that does not use `cleanShuttleOnReturnTo`
- **WHEN** the destination calls `cleanShuttleFromDeliveryFor(cargoId)` after a successful pickup
- **THEN** the cargo is removed from the warehouse

```kotlin
// Fire-and-forget
shuttle.cleanShuttleFromDeliveryFor(cargoId)

// Observed
lifecycleScope.launch {
    shuttle.cleanShuttleFromDeliveryFor(cargoId)
        .consumeAsFlow()
        .collect { result ->
            when (result) {
                is ShuttleRemoveCargoResult.Removed           -> log("Cleaned up $cargoId")
                is ShuttleRemoveCargoResult.UnableToRemove<*> -> log("Cleanup failed: ${result.throwable?.message}")
                is ShuttleRemoveCargoResult.DoesNotExist      -> log("Already removed")
                is ShuttleRemoveCargoResult.Removing          -> { /* in progress */ }
                else                                          -> { /* NotRemovingCargoYet */ }
            }
        }
}
```

### Requirement: End-of-Lifecycle Cleanup

Call `cleanShuttleFromAllDeliveries()` once at a lifecycle boundary such as user logout, account switch, or full app reset. This empties the entire warehouse in one operation. It is not for per-delivery cleanup. Use `cleanShuttleFromDeliveryFor(cargoId)` for that instead.

#### Scenario: Logout or app reset
- **GIVEN** the user logs out or the app reaches a full lifecycle reset boundary
- **WHEN** `cleanShuttleFromAllDeliveries()` is called once at that boundary
- **THEN** the entire warehouse is emptied and all previously stored cargo is purged

```kotlin
shuttle.cleanShuttleFromAllDeliveries()
```

### Requirement: Cleanup Result States

When observing a cleanup channel, handle the following `ShuttleRemoveCargoResult` states:

| State | Meaning |
|---|---|
| `ShuttleRemoveCargoResult.Removing` | Deletion in progress |
| `ShuttleRemoveCargoResult.Removed` | Cargo successfully deleted |
| `ShuttleRemoveCargoResult.UnableToRemove` | Deletion failed; `result.throwable` contains the cause |
| `ShuttleRemoveCargoResult.DoesNotExist` | Cargo ID not found in warehouse (already cleaned up) |

### Cleanup Strategy Selection

| Scenario | Recommended Strategy |
|---|---|
| Standard Activity-to-Activity navigation | `cleanShuttleOnReturnTo` at source |
| Fragment-to-Fragment with NavComponent | `cleanShuttleOnReturnTo` at source |
| One-way delivery (no back-navigation) | `cleanShuttleFromDeliveryFor` at destination after pickup |
| App logout or full reset | `cleanShuttleFromAllDeliveries` at the lifecycle boundary |
| Long-running Service delivering to multiple consumers | `cleanShuttleFromDeliveryFor` per cargo ID after each consumer picks up |
