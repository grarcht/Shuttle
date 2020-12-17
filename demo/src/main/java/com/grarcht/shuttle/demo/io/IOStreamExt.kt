package com.grarcht.shuttle.demo.io

import java.io.Closeable
import java.io.IOException

fun Closeable?.closeQuietly() {
    this?.let {
        try {
            this.close()
        } catch (ioe: IOException) {
            // ignore on purpose
        }
    }
}