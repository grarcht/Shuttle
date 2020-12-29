package com.grarcht.shuttle.demo.core.image

import com.grarcht.shuttle.framework.integrations.extensions.room.ShuttleRoomData
import java.io.Serializable

/**
 * Houses the [imageData] and a corresponding key used to look up the [imageData] at a later time.
 * @param cargoId used to look up the [imageData] at a later time
 * @param imageData to store
 */
class ImageModel(
    override var cargoId: String,
    val imageData: ByteArray
) : ShuttleRoomData(cargoId = cargoId), Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageModel

        if (cargoId != other.cargoId) return false
        if (!imageData.contentEquals(other.imageData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 31 * cargoId.hashCode()
        result = 31 * result + imageData.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "ImageModel(id=$cargoId, imageData=${imageData.contentToString()})"
    }

    // Required
    companion object {
        @JvmStatic
        private val serialVersionUID = -10693L

    }
}
