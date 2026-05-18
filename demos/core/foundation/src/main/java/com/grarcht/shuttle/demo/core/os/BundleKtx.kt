package com.grarcht.shuttle.demo.core.os

import android.os.Bundle

/**
 * Gets the parcelable from the bundle.
 * @return the parcelable or null
 */
fun <T> Bundle.getParcelableWith(key: String?, clazz: Class<T>): T? {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        getParcelable(key)
    }
}
