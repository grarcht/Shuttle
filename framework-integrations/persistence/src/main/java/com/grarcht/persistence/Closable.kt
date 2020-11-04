package com.grarcht.persistence

import java.io.Closeable
import java.io.IOException

fun Closeable?.closeQuietly() {
    try {
        this?.close()
    } catch (e: IOException) {
        // ignore
    }
}