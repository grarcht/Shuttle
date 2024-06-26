package com.grarcht.shuttle.framework.result

sealed class ShuttleRemoveCargoResult {

    /**
     * The state for when Shuttle has failed to pick up the cargo.
     * @param cargoId for the cargo being removed.  It can be [ALL_CARGO].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    open class DoesNotExist(val cargoId: String) : ShuttleRemoveCargoResult()

    /**
     * The state for when Shuttle has successfully picked up the cargo.
     * @param cargoId for the cargo being removed.  It can be [ALL_CARGO].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    open class Removed(val cargoId: String) : ShuttleRemoveCargoResult()

    /**
     * The state for when Shuttle has started removing the cargo.
     * @param cargoId for the cargo being removed.  It can be [ALL_CARGO].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    class Removing(val cargoId: String) : ShuttleRemoveCargoResult()

    /**
     * The state for when Shuttle has failed to pick up the cargo.
     * @param cargoId for the cargo being removed.  It can be [ALL_CARGO].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    open class UnableToRemove<T>(
        val cargoId: String,
        val message: String? = null,
        val throwable: T? = null
    ) : ShuttleRemoveCargoResult() where T : Throwable

    /**
     * The initial state for Shuttle not removing the cargo yet.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    open class NotRemovingCargoYet(val cargoId: String) : ShuttleRemoveCargoResult()

    companion object {
        const val ALL_CARGO = "All Cargo"
    }
}
