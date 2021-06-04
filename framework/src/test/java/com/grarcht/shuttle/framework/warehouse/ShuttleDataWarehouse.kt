package com.grarcht.shuttle.framework.warehouse

import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import com.grarcht.shuttle.framework.result.ShuttleStoreCargoResult
import kotlinx.coroutines.channels.Channel
import java.io.Serializable

@Suppress("UNCHECKED_CAST")
open class ShuttleDataWarehouse : ShuttleWarehouse {
    var numberOfStoreInvocations: Int = 0
    var numberOfRemoveInvocations: Int = 0
    private val cache: MutableMap<String, Serializable> = mutableMapOf()
    private val pickupCargoChannel = Channel<ShuttlePickupCargoResult>(3)
    private val storeCargoChannel = Channel<ShuttleStoreCargoResult>(3)
    val removeCargoChannel = Channel<ShuttleRemoveCargoResult>(4)

    override suspend fun <D : Serializable> pickup(cargoId: String): Channel<ShuttlePickupCargoResult> {
        pickupCargoChannel.send(ShuttlePickupCargoResult.Loading)
        pickupCargoChannel.send(ShuttlePickupCargoResult.Success(cache[cargoId] as D))
        return pickupCargoChannel
    }

    override suspend fun <D : Serializable> store(cargoId: String, data: D?): Channel<ShuttleStoreCargoResult> {
        storeCargoChannel.send(ShuttleStoreCargoResult.Storing(cargoId))
        cache[cargoId] = data as Serializable
        storeCargoChannel.send(ShuttleStoreCargoResult.Success(cargoId))
        numberOfStoreInvocations++
        return storeCargoChannel
    }

    override suspend fun removeCargoBy(cargoId: String): Channel<ShuttleRemoveCargoResult> {
        cache.remove(cargoId)
        removeCargoChannel.send(ShuttleRemoveCargoResult.Removed(cargoId))
        numberOfRemoveInvocations++
        return removeCargoChannel
    }

    override suspend fun removeAllCargo(): Channel<ShuttleRemoveCargoResult> {
        cache.clear()
        removeCargoChannel.send(ShuttleRemoveCargoResult.Removed(ShuttleRemoveCargoResult.ALL_CARGO))
        numberOfRemoveInvocations++
        return removeCargoChannel
    }
}