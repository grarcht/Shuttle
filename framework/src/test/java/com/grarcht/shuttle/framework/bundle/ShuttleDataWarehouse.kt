package com.grarcht.shuttle.framework.bundle

import android.os.Parcelable
import androidx.lifecycle.LifecycleOwner
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import com.grarcht.shuttle.framework.result.ShuttleStoreCargoResult
import com.grarcht.shuttle.framework.warehouse.ShuttleWarehouse
import kotlinx.coroutines.channels.Channel

open class ShuttleDataWarehouse : ShuttleWarehouse {
    var numberOfSaveInvocations: Int = 0
    lateinit var parcelableToEmit: Parcelable

    private val pickupCargoChannel = Channel<ShuttlePickupCargoResult>(0)
    private val storeCargoChannel = Channel<ShuttleStoreCargoResult>(0)
    private val removeCargoChannel = Channel<ShuttleRemoveCargoResult>(0)

    override val id: String
        get() = "ShuttleRepo"

    override suspend fun <D : Parcelable> pickup(
        cargoId: String,
        parcelableCreator: Parcelable.Creator<D>,
        lifecycleOwner: LifecycleOwner
    ): Channel<ShuttlePickupCargoResult> {
        pickupCargoChannel.send(ShuttlePickupCargoResult.Success(parcelableToEmit))
        return pickupCargoChannel
    }

    override suspend fun <D : Parcelable> store(cargoId: String, data: D?): Channel<ShuttleStoreCargoResult> {
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