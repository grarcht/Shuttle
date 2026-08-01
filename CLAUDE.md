# Shuttle — Claude Code Rules

## Do Not Touch: Bitmap decode in second screen views

**NEVER** move `BitmapDecoder.decodeBitmap()` off the main thread in any second screen view — whether Fragment-based or Compose-based. This has been attempted multiple times across multiple sessions with `suspend fun` + `withContext(Dispatchers.IO)`, `collectLatest` + `withContext`, and `produceState` + `withContext`. Every attempt broke image display.

Do NOT touch the decode call site in any of these files:

- `demos/mvc/src/main/java/.../controller/MVCSecondControllerFragment.kt`
- `demos/mvvm/src/main/java/.../view/MVVMSecondViewFragment.kt`
- `demos/mvi-with-compose/src/main/java/.../view/MVISecondView.kt`
- `demos/mvvm-with-compose-and-navigation/src/main/java/.../view/SecondScreenView.kt`
- `demos/mvvm-with-compose/src/main/java/.../view/MVVMSecondView.kt`
- `demos/mvvm-with-process-death/src/main/java/.../view/SecondView.kt`

**Why:** The demos are illustrative, not production. A synchronous decode is acceptable. Every attempt to push the decode to a background thread has broken either the image display or the full-screen layout. Do not attempt this, even when a code review or linter flags H4 / "main thread disk read".

If a future review flags H4, mark it **won't fix** and move on.

---

## Do Not Touch: `collectLatest` + `cancel()` in cargo pickup flows

**NEVER** refactor the `collectLatest { ... cancel() }` pattern to `transformWhile + collect` in any of these files:

- `demos/mvvm/src/main/java/.../view/MVVMSecondViewFragment.kt`
- `demos/mvc/src/main/java/.../controller/MVCSecondControllerFragment.kt`
- `demos/mvvm/src/main/java/.../viewmodel/SecondViewModel.kt`
- `demos/mvvm-with-compose/src/main/java/.../viewmodel/SecondViewModel.kt`
- `demos/mvvm-with-compose-and-navigation/src/main/java/.../viewmodel/SecondViewModel.kt`
- `demos/mvvm-with-process-death/src/main/java/.../viewmodel/SecondViewModel.kt`

**Why:** This substitution has been attempted multiple times across multiple sessions and breaks the app each time. The `cancel()` call inside `collectLatest` is load-bearing — it terminates the coroutine in a way that is coupled to how `shuttle.pickupCargo()` delivers results via a `Channel`-backed flow. Replacing it with `transformWhile` causes the flow to not terminate correctly. Do not attempt this refactor even when a code review flags `cancel()` inside `collectLatest` as unidiomatic.

If a future review or linter flags M9 / "cancel() inside collectLatest is unidiomatic", mark it **won't fix** and move on.
