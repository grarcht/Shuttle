package com.grarcht.shuttle.framework.content

import android.content.Intent
import android.os.Parcelable
import androidx.lifecycle.LifecycleOwner
import com.grarcht.shuttle.framework.model.ShuttleParcelPackage
import com.grarcht.shuttle.framework.respository.ShuttleWarehouse
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

private const val MESSAGE_SHUTTLE_INTENT_NOT_USED =
    "The required ShuttleIntent was not used to marshal the data."

class ShuttleDataExtractor(
    private val intent: Intent,
    private val repository: ShuttleWarehouse
) {
    private var disposableHandle: DisposableHandle? = null

    /**
     * Extracts the parcel data as a [ShuttleResult].
     * @param key used to find the stored [ShuttleResult]
     *
     */
    suspend fun <D : Parcelable> extractParcelData(
        key: String,
        parcelableCreator: Parcelable.Creator<D>,
        lifecycleOwner: LifecycleOwner
    ): Channel<ShuttleResult> {
        val parcelPackage: ShuttleParcelPackage? = intent.extras?.getParcelable(key)

        return if (null == parcelPackage) {
            val channel = Channel<ShuttleResult>(5)
            val result =
                ShuttleResult.Error<Exception>(IllegalStateException(MESSAGE_SHUTTLE_INTENT_NOT_USED))
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