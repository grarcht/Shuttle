package com.grarcht.shuttle.framework.bundle

import android.os.Parcelable
import android.util.SparseArray
import androidx.lifecycle.LifecycleOwner
import com.grarcht.shuttle.framework.content.ShuttleResult
import com.grarcht.shuttle.framework.respository.ShuttleWarehouse
import kotlinx.coroutines.channels.Channel

open class ShuttleDataWarehouse : ShuttleWarehouse {
    var numberOfSaveInvocations: Int = 0
    lateinit var parcelableToEmit: Parcelable

    private val channel = Channel<ShuttleResult>(0)

    override val id: String
        get() = "ShuttleRepo"

    override suspend fun <D : Parcelable> get(
        lookupKey: String,
        parcelableCreator: Parcelable.Creator<D>,
        lifecycleOwner: LifecycleOwner
    ): Channel<ShuttleResult> {
        channel.send(ShuttleResult.Success(parcelableToEmit))
        return channel
    }

    override suspend fun <D : Parcelable> save(lookupKey: String, data: D?) {
        numberOfSaveInvocations++
    }

    override suspend fun <D : Parcelable> save(lookupKey: String, data: Array<D>?) {
        numberOfSaveInvocations++
    }

    override suspend fun <D : Parcelable> save(lookupKey: String, data: ArrayList<D>?) {
        numberOfSaveInvocations++
    }

    override suspend fun <D : Parcelable> save(lookupKey: String, data: SparseArray<D>?) {
        numberOfSaveInvocations++
    }
}