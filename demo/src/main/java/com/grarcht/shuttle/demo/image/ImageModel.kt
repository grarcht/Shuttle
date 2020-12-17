package com.grarcht.shuttle.demo.image

import android.os.Parcel
import android.os.Parcelable
import com.grarcht.room.ShuttleData

class ImageModel(override var lookupKey: String, val imageData: ByteArray) :
    ShuttleData(lookupKey = lookupKey, data = imageData), Parcelable {

//    constructor(parcel: Parcel) : this(
//        parcel.readString() ?: "",
//        parcel.createByteArray() ?: ByteArray(0)
//    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageModel

        if (lookupKey != other.lookupKey) return false
        if (!imageData.contentEquals(other.imageData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lookupKey.hashCode()
        result = 31 * result + imageData.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "ImageModel(id=$lookupKey, imageData=${imageData.contentToString()})"
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(lookupKey)
        val size = imageData.size
        parcel.writeInt(size)
        parcel.writeByteArray(imageData)
    }

    companion object CREATOR : Parcelable.Creator<ImageModel> {
        override fun createFromParcel(parcel: Parcel): ImageModel {
            val lookupKey = parcel.readString() ?: ""
            val size = parcel.readInt()
            val imageData = ByteArray(size)
            parcel.readByteArray(imageData)
            return ImageModel(lookupKey, imageData)
        }

        override fun newArray(size: Int): Array<ImageModel?> {
            return arrayOfNulls(size)
        }
    }
}
