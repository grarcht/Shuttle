package com.grarcht.shuttle.demo.mviwithcompose.intent

import com.grarcht.shuttle.framework.Shuttle

/**
 * Represents the set of user or system actions that can be dispatched to trigger
 * cargo pickup operations in the second view. Each subtype corresponds to a
 * distinct operation that the view model can act on.
 */
sealed class CargoPickupIntent {
    /**
     * Requests that previously stored cargo be retrieved from the Shuttle warehouse
     * using the provided cargo identifier.
     *
     * @property shuttle the Shuttle instance used to perform the cargo pickup.
     * @property cargoId the identifier of the cargo to retrieve from the warehouse.
     */
    data class LoadCargo(val shuttle: Shuttle, val cargoId: String) : CargoPickupIntent()
}
