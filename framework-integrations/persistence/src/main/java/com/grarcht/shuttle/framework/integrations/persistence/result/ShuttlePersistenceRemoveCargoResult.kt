package com.grarcht.shuttle.framework.integrations.persistence.result

/**
 * Represents states from attempts to remove the cargo.
 */
sealed class ShuttlePersistenceRemoveCargoResult {
    /**
     * The state for when the cargo does not exist.
     */
    object DoesNotExist : ShuttlePersistenceRemoveCargoResult()

    /**
     * The state for when Shuttle has successfully picked up the cargo.
     */
    object Removed : ShuttlePersistenceRemoveCargoResult()

    /**
     * The state for when Shuttle has started removing the cargo.
     */
    object UnableToRemove : ShuttlePersistenceRemoveCargoResult()
}
