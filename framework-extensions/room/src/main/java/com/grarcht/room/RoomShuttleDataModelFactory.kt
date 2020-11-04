package com.grarcht.room

import android.os.Parcelable
import com.grarcht.persistence.BlobAdapter
import com.grarcht.persistence.ShuttleDataModel
import com.grarcht.persistence.ShuttleDataModelFactory

class RoomShuttleDataModelFactory(private val blobAdapter: BlobAdapter) : ShuttleDataModelFactory {

    override fun createParcelDataModel(
        lookupKey: String,
        parcelable: Parcelable
    ): ShuttleDataModel {
        val blob = blobAdapter.adaptToByteArray(parcelable)
        return ShuttleData(lookupKey, blob)
    }
}