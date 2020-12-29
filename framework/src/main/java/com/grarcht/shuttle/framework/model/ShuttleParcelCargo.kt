package com.grarcht.shuttle.framework.model

import android.os.Parcel
import android.os.Parcelable

const val NO_FILE_DESCRIPTOR = 0
const val NO_CARGO_ID = "no cargo id"

/**
 * This is the [Parcelable] object that is supplied by Shuttle for use with Android Transactions.
 * @param cargoId this is the identifier for this cargo and to use with Shuttle to pickup the large cargo
 */
open class ShuttleParcelCargo(val cargoId: String) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString() ?: NO_CARGO_ID)

    override fun describeContents(): Int {
        return NO_FILE_DESCRIPTOR
    }

    override fun writeToParcel(parcel: Parcel?, flags: Int) {
        parcel?.writeString(cargoId)
    }

    companion object CREATOR : Parcelable.Creator<ShuttleParcelCargo> {
        override fun createFromParcel(parcel: Parcel): ShuttleParcelCargo {
            return ShuttleParcelCargo(parcel)
        }

        override fun newArray(size: Int): Array<ShuttleParcelCargo?> {
            return arrayOfNulls(size)
        }
    }
}

