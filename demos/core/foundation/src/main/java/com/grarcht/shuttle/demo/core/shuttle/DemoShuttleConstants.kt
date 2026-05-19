package com.grarcht.shuttle.demo.core.shuttle

/** Maximum milliseconds to wait for a cargo pickup before emitting a timeout error. */
const val CARGO_PICKUP_TIMEOUT_MS = 5_000L

/** Cargo older than this many milliseconds is considered an orphan and will be purged (24 hours). */
const val CARGO_ORPHAN_TTL_MS = 86_400_000L
