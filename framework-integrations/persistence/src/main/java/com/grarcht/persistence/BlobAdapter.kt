package com.grarcht.persistence

import android.os.Parcel
import android.os.Parcelable
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectOutputStream

private const val DATA_POSITION = 0
private const val WRITE_TO_PARCEL_FLAGS = 0

class BlobAdapter {

    fun adaptToByteArray(parcelable: Parcelable): ByteArray {
        val parcel = Parcel.obtain()
        parcelable.writeToParcel(parcel, 0)
        parcel.setDataPosition(DATA_POSITION)
        val bytes = parcel.marshall()
        parcel.recycle()
        return bytes
    }

    fun <D : Parcelable> adaptToParcelable(
        bytes: ByteArray,
        parcelableCreator: Parcelable.Creator<D>
    ): Parcelable {
        val parcel: Parcel = adaptToParcel(bytes)
        val parcelable = parcelableCreator.createFromParcel(parcel)
        parcel.recycle()
        return parcelable
    }

    private fun adaptToParcel(bytes: ByteArray): Parcel {
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(DATA_POSITION)
        return parcel
    }
}