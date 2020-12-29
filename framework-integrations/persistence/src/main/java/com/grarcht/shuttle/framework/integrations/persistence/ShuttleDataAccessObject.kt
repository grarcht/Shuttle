package com.grarcht.shuttle.framework.integrations.persistence

import com.grarcht.shuttle.framework.integrations.persistence.datamodel.ShuttleDataModel

/**
 * The contractual interface used to access persisted data.
 */
interface ShuttleDataAccessObject {
    /**
     * Gets the number of cargo items
     * @return the number of cargo items
     */
    fun getNumberOfCargoItems(): Int

    /**
     * @return the cargo associated with the [cargoId]
     */
    suspend fun getCargoBy(cargoId: String): ShuttleDataModel?

    /**
     * Inserts the cargo, [ShuttleDataModel], into the DB.
     * @return the value for the insert
     */
    fun insertCargo(data: ShuttleDataModel): Long

    /**
     * Removes the cargo, denoted by [cargoId] from the DB.
     */
    fun deleteCargoBy(cargoId: String): Int

    /**
     * Removes all cargo from the DB.
     */
    fun deleteAllCargoData(): Int

    companion object {
        const val STORE_CARGO_FAILED = -1L
        const val REMOVE_CARGO_FAILED = -1
    }
}
