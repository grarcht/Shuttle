package com.grarcht.room

import android.os.Parcelable
import com.grarcht.persistence.ShuttleDatabaseBlobAdapter
import com.grarcht.persistence.ShuttleDataModel
import com.grarcht.persistence.ShuttleDataModelFactory

class RoomShuttleDataModelFactory(private val shuttleDatabaseBlobAdapter: ShuttleDatabaseBlobAdapter) : ShuttleDataModelFactory {

    override fun createParcelDataModel(
        lookupKey: String,
        parcelable: Parcelable
    ): ShuttleDataModel {
        val blob = shuttleDatabaseBlobAdapter.adaptToByteArray(parcelable)
        return ShuttleData(lookupKey, blob)
    }
}