package com.grarcht.persistence

import android.os.Parcelable

/**
 *
 */
interface ShuttleDataModelFactory {

    /**
     *
     */
    fun createParcelDataModel(
        lookupKey: String,
        parcelable: Parcelable
    ): ShuttleDataModel
}