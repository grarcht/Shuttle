package com.grarcht.shuttle.framework.model

import android.os.Parcel
import android.os.Parcelable

const val NO_FILE_DESCRIPTOR = 0
const val NO_REPOSITORY_ID = "no repository id"
const val NO_PARCEL_ID = "no parcel id"

class ShuttleParcelPackage(
    val repositoryId: String,
    val parcelId: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: NO_REPOSITORY_ID,
        parcel.readString() ?: NO_PARCEL_ID
    ) {
        println("ParcelPackage: constructor repositoryId: $repositoryId\n parcelId $parcelId")
    }

    override fun describeContents(): Int {
        return NO_FILE_DESCRIPTOR
    }

    override fun writeToParcel(parcel: Parcel?, flags: Int) {
        println("ParcelPackage: parcel: $parcel\n flags: $flags")
        parcel?.let {
            it.writeString(repositoryId)
            it.writeString(parcelId)
        }
    }

    companion object CREATOR : Parcelable.Creator<ShuttleParcelPackage> {
        override fun createFromParcel(parcel: Parcel): ShuttleParcelPackage {
            return ShuttleParcelPackage(parcel)
        }

        override fun newArray(size: Int): Array<ShuttleParcelPackage?> {
            return arrayOfNulls(size)
        }
    }
}