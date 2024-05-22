package com.grarcht.shuttle.framework.result

sealed class ShuttleStoreCargoResult {
    /**
     * The state for when Shuttle has successfully picked up the cargo.
     * @param cargoId for the stored cargo
     */
    @Suppress("MemberVisibilityCanBePrivate")
    open class Success(val cargoId: String) : ShuttleStoreCargoResult()

    /**
     * The state for when Shuttle has failed to pick up the cargo.
     * @param cargoId for the cargo that could not be stored
     * @param throwable for the error that occurred when picking up the cargo
     */
    @Suppress("MemberVisibilityCanBePrivate")
    open class Error<T>(
        val cargoId: String,
        val message: String? = null,
        val throwable: T? = null
    ) : ShuttleStoreCargoResult() where T : Throwable

    /**
     * The state for when Shuttle has started storing the cargo.
     * @param cargoId for the cargo being stored
     */
    @Suppress("MemberVisibilityCanBePrivate")
    class Storing(val cargoId: String) : ShuttleStoreCargoResult()

    /**
     * The initial state for Shuttle not storing the cargo yet.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    data class NotStoringCargoYet(val cargoId: String) : ShuttleStoreCargoResult()
}
