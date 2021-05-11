package com.grarcht.shuttle.framework.bundle

import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import com.grarcht.shuttle.framework.result.ShuttleStoreCargoResult
import com.grarcht.shuttle.framework.warehouse.ShuttleWarehouse
import kotlinx.coroutines.channels.Channel
import java.io.Serializable

open class ShuttleDataWarehouse : ShuttleWarehouse {
    var numberOfSaveInvocations: Int = 0
    lateinit var serializableToEmit: Serializable

    private val pickupCargoChannel = Channel<ShuttlePickupCargoResult>(0)
    private val storeCargoChannel = Channel<ShuttleStoreCargoResult>(0)
    private val removeCargoChannel = Channel<ShuttleRemoveCargoResult>(0)

    override suspend fun <D : Serializable> pickup(cargoId: String): Channel<ShuttlePickupCargoResult> {
        pickupCargoChannel.send(ShuttlePickupCargoResult.Success(serializableToEmit))
        return pickupCargoChannel
    }

    override suspend fun <D : Serializable> store(cargoId: String, data: D?): Channel<ShuttleStoreCargoResult> {
        numberOfSaveInvocations++
        return storeCargoChannel
    }

    override suspend fun removeCargoBy(cargoId: String): Channel<ShuttleRemoveCargoResult> {
        return removeCargoChannel
    }

    override suspend fun removeAllCargo(): Channel<ShuttleRemoveCargoResult> {
        return removeCargoChannel
    }
}