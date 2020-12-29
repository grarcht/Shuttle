package com.grarcht.shuttle.framework.screen

/**
 * This is a contractual interface for a facade that  hides functionality, such as removing cargo after a delivery
 * (data is removed from the db after the user presses the back button on the screen where the data was delivered).
 */
interface ShuttleFacade {

    /**
     * Removes the cargo in the warehouse after it has been delivered.
     * @param currentScreenClass where the cargo is first used with Shuttle
     * @param nextScreenClass the screen where app is navigating to next
     * @param cargoId denotes the cargo to remove
     */
    fun removeCargoAfterDelivery(currentScreenClass: Class<*>, nextScreenClass: Class<*>, cargoId: String)
}
