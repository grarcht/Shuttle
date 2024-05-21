package com.grarcht.shuttle.framework.result

import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult.Companion.ALL_CARGO

sealed class ShuttlePickupCargoResult {
    /**
     * The state for when Shuttle has successfully picked up the cargo.
     * @param data the retrieved cargo data
     */
    @Suppress("MemberVisibilityCanBePrivate")
    open class Success<D>(val data: D) : ShuttlePickupCargoResult()

    /**
     * The state for when Shuttle has failed to pick up the cargo.
     * @param cargoId for the cargo being removed.  It can be [ALL_CARGO].
     * @param throwable for the error that occurred when picking up the cargo
     */
    @Suppress("MemberVisibilityCanBePrivate")
    open class Error<T>(
        val cargoId: String,
        val message: String? = null,
        val throwable: T? = null
    ) : ShuttlePickupCargoResult() where T : Throwable

    /**
     * The state for when Shuttle has started picking up the cargo.
     */
    data object Loading : ShuttlePickupCargoResult()
}
