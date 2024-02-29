package com.grarcht.shuttle.framework.warehouse

import android.os.Parcelable
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import com.grarcht.shuttle.framework.result.ShuttleStoreCargoResult
import kotlinx.coroutines.channels.Channel
import java.io.Serializable

/**
 * This contractual interface enables picking up and storing cargo within the warehouse.
 */
interface ShuttleWarehouse {

    /**
     * Picks up the cargo as [ShuttlePickupCargoResult] using a [cargoId].
     * @param cargoId Used to get the cargo.
     * @return the channel for the results
     */
    suspend fun <D : Serializable> pickup(cargoId: String): Channel<ShuttlePickupCargoResult>

    /**
     * Stores the cargo in the warehouse.
     * @param cargoId Used to get the cargo.
     * @param data The [Parcelable] cargo to store.
     */
    suspend fun <D : Serializable> store(cargoId: String, data: D?): Channel<ShuttleStoreCargoResult>

    /**
     * Removes the cargo for [cargoId] from the warehouse.
     * @param cargoId denotes which cargo to remove
     */
    suspend fun removeCargoBy(cargoId: String): Channel<ShuttleRemoveCargoResult>

    /**
     * Removes all cargo from the warehouse.
     */
    suspend fun removeAllCargo(): Channel<ShuttleRemoveCargoResult>
}
