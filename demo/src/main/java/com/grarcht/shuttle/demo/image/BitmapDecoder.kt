package com.grarcht.shuttle.demo.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory

class BitmapDecoder {

    fun decodeBitmap(imageBytes: ByteArray): Bitmap? {
        return try {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            bitmap
        } catch (e: IllegalArgumentException) {
            null
        } catch (t: Throwable) {
            null
        }
    }

}