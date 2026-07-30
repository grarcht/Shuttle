---
title: Shuttle Cargo Pickup
version: 4.0
domain: pickup
status: active
lastUpdated: 2026-07-29
---

# Shuttle Cargo Pickup

## Overview

This spec covers how destination components retrieve cargo from the warehouse after a cargo ID arrives via Intent, Bundle, or Navigation argument.

---

## Requirements

### Requirement: Cargo Pickup via Channel

Call `shuttle.pickupCargo<T>(cargoId)` to retrieve cargo. It returns a `Channel<ShuttlePickupCargoResult>` that emits one or more state values.

Channel closing behavior:
- With `timeoutMs > 0`: the channel closes automatically after a terminal result or when the timeout expires.
- Without a timeout: the channel does not close automatically. Consumers must call `cancel()` inside `collectLatest` to terminate the coroutine after a terminal result.

#### Scenario: Pickup in a Fragment or Activity
- **GIVEN** a destination Fragment with a known cargo ID
- **WHEN** the developer launches a coroutine in `lifecycleScope` and collects the channel
- **THEN** the cargo is delivered as a `ShuttlePickupCargoResult.Success` result

```kotlin
lifecycleScope.launch {
    shuttle.pickupCargo<MyModel>(cargoId = cargoId)
        .consumeAsFlow()
        .collectLatest { result ->
            when (result) {
                is ShuttlePickupCargoResult.Loading     -> showLoading()
                is ShuttlePickupCargoResult.Success<*>  -> { render(result.data as MyModel); cancel() }
                is ShuttlePickupCargoResult.Error<*>    -> { showError(); cancel() }
                else                                    -> { /* NotPickingUpCargoYet: await */ }
            }
        }
}
```

> **Important:** The `cancel()` call inside `collectLatest` is load-bearing. It terminates the coroutine after a terminal result in a way that is coupled to how the warehouse delivers results through the Channel-backed flow. Do not replace this pattern with `transformWhile + collect`. See `CLAUDE.md` for full context.

#### Scenario: Pickup in a ViewModel
- **GIVEN** a ViewModel with an injected `Shuttle` and a known cargo ID
- **WHEN** the ViewModel calls `pickupCargo` in `viewModelScope`
- **THEN** the result is posted to a `MutableStateFlow` or `MutableLiveData` observed by the View

```kotlin
viewModelScope.launch {
    shuttle.pickupCargo<MyModel>(cargoId = cargoId)
        .consumeAsFlow()
        .collectLatest { result ->
            _cargoState.value = result
            when (result) {
                is ShuttlePickupCargoResult.Success<*>,
                is ShuttlePickupCargoResult.Error<*> -> cancel()
                else -> { /* still loading */ }
            }
        }
}
```

### Requirement: Pickup Result States

Handle all three `ShuttlePickupCargoResult` states when collecting a pickup channel.

| State | Class | Meaning |
|---|---|---|
| Loading | `ShuttlePickupCargoResult.Loading` | Warehouse read in progress |
| Success | `ShuttlePickupCargoResult.Success<*>` | `result.data` contains the retrieved object |
| Error | `ShuttlePickupCargoResult.Error<*>` | Retrieval failed; `result.throwable` contains the cause |

The `Loading` state gives the UI a hook to show a progress indicator before the object is available. This matters because `Serializable` deserialization takes a moment.

### Requirement: Bounded Pickup with Timeout

Pass a `timeoutMs` value to limit how long `pickupCargo` waits for a terminal result. If the warehouse does not deliver within that window, a `ShuttlePickupCargoResult.Error` is emitted and the channel closes.

#### Scenario: Pickup with 5-second timeout
- **GIVEN** a slow or unavailable warehouse
- **WHEN** the developer passes `timeoutMs = 5_000L`
- **THEN** after 5 seconds without a terminal result, an error is emitted and the channel closes automatically

```kotlin
shuttle.pickupCargo<MyModel>(cargoId = cargoId, timeoutMs = 5_000L)
    .consumeAsFlow()
    .collectLatest { result -> /* handle LCE */ }
```

### Requirement: Cargo ID at Destination

The destination does not need to extract the cargo ID from the intent or bundle. Because cargo IDs must be defined as shared named constants (see the transport spec), the destination references the same constant directly in `pickupCargo()`.

#### Scenario: Pickup using shared constant (standard pattern)
```kotlin
// Both source and destination share this constant, no intent extraction required
val cargoId = DataMessageType.UserProfile.value

viewModelScope.launch {
    shuttle.pickupCargo<MyModel>(cargoId = cargoId)
        .consumeAsFlow()
        .collectLatest { result -> /* handle LCE */ }
}
```

#### Scenario: Extract ID from Activity intent (Service messenger path only)
When using the Service messenger path rather than the standard `ShuttleIntent.transport()` path, the cargo ID is stored as a Parcelable under the user-defined cargo ID key:
```kotlin
val parcel = intent.getParcelableExtra(MY_CARGO_ID_CONSTANT, ShuttleParcelCargo::class.java)
val cargoId = parcel?.cargoId ?: return
```
