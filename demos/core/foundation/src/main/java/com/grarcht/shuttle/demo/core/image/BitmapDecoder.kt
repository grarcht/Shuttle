package com.grarcht.shuttle.demo.core.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Decodes [ByteArray] into [Bitmap] objects.
 */
object BitmapDecoder {

    /**
     * Decodes [ByteArray] into [Bitmap] objects.
     * @param imageBytes to decode
     * @return the decoded [Bitmap] or null if an exception was thrown
     */
    fun decodeBitmap(imageBytes: ByteArray): Bitmap? {
        @Suppress("SwallowedException")
        return try {
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
