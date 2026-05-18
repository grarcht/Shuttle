package com.grarcht.shuttle.demo.core.image

import com.grarcht.shuttle.framework.ShuttleCargo
import com.grarcht.shuttle.framework.integrations.extensions.room.ShuttleRoomData

/**
 * Houses the [imageData] and a corresponding key used to look up the [imageData] at a later time.
 * @param cargoId used to look up the [imageData] at a later time
 * @param imageData to store
 *
 * [@ShuttleCargo] marks this class as Shuttle-transportable. External consumers applying
 * `id("com.grarcht.shuttle.cargo")` get [com.grarcht.shuttle.framework.ShuttleCargoData]
 * injected automatically; here it is already provided transitively by [ShuttleRoomData].
 */
@ShuttleCargo
class ImageModel(
    override var cargoId: String,
    val imageData: ByteArray
) : ShuttleRoomData(cargoId = cargoId) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageModel

        if (cargoId != other.cargoId) return false
        return imageData.contentEquals(other.imageData)
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
        private const val serialVersionUID: Long = -10693
    }
}
