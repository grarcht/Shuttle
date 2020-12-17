package com.grarcht.shuttle.framework.content

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.LifecycleOwner
import com.grarcht.shuttle.framework.model.ShuttleParcelPackage
import com.grarcht.shuttle.framework.respository.ShuttleWarehouse
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

private const val MESSAGE_SHUTTLE_INTENT_NOT_USED =
    "The required ShuttleIntent was not used to marshal the data."

open class ShuttleDataExtractor(private val repository: ShuttleWarehouse) {
    private var disposableHandle: DisposableHandle? = null

    /**
     * Extracts the parcel data as a [ShuttleResult].
     * @param key used to find the stored [ShuttleResult]
     *
     */
    suspend fun <D : Parcelable> extractParcelData(
        bundle: Bundle?,
        key: String,
        parcelableCreator: Parcelable.Creator<D>,
        lifecycleOwner: LifecycleOwner
    ): Channel<ShuttleResult> {
        val parcelPackage: ShuttleParcelPackage? = bundle?.getParcelable(key)

        return if (null == parcelPackage) {
            val exception = IllegalStateException(MESSAGE_SHUTTLE_INTENT_NOT_USED)
            val result = ShuttleResult.Error<Exception>(exception)
            val channel = Channel<ShuttleResult>(5)
            channel.send(result)
            channel
        } else {
            repository.get(parcelPackage.parcelId, parcelableCreator, lifecycleOwner)
        }
    }

    /**
     *
     */
    fun tearDown() {
        disposableHandle?.dispose()
    }
}