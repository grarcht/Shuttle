package com.grarcht.shuttle.demo.core.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * This class decodes [ByteArray] into [Bitmap] objects.
 */
class BitmapDecoder {

    /**
     * Decodes [ByteArray] into [Bitmap] objects.
     * @param imageBytes to decode
     * @return the decoded [Bitmap] or null if an exception was thrown
     */
    fun decodeBitmap(imageBytes: ByteArray): Bitmap? {
        return try {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            bitmap
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
