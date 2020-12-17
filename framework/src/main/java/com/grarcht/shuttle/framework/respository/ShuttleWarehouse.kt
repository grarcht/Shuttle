package com.grarcht.shuttle.framework.respository

import android.os.Parcelable
import android.util.SparseArray
import androidx.lifecycle.LifecycleOwner
import com.grarcht.shuttle.framework.content.ShuttleResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

interface ShuttleWarehouse {
    val id: String

    suspend fun <D : Parcelable> get(
        lookupKey: String,
        parcelableCreator: Parcelable.Creator<D>,
        lifecycleOwner: LifecycleOwner
    ): Channel<ShuttleResult>

    suspend fun <D : Parcelable> save(lookupKey: String, data: D?)

    suspend fun <D : Parcelable> save(lookupKey: String, data: Array<D>?)

    suspend fun <D : Parcelable> save(lookupKey: String, data: ArrayList<D>?)

    suspend fun <D : Parcelable> save(lookupKey: String, data: SparseArray<D>?)
}