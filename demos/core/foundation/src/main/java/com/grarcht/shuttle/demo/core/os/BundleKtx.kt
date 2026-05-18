package com.grarcht.shuttle.demo.core.os

import android.os.Bundle

/**
 * Retrieves a [android.os.Parcelable] value from this [Bundle] in a version-safe manner,
 * using the typed [android.os.Bundle.getParcelable] overload on Android TIRAMISU (API 33) and
 * above, and falling back to the deprecated untyped overload on earlier versions.
 *
 * @param key the key associated with the parcelable in the bundle.
 * @param clazz the expected class of the parcelable value.
 * @return the parcelable value, or null if the key is not present or the value is not of type T.
 */
fun <T> Bundle.getParcelableWith(key: String?, clazz: Class<T>): T? {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        getParcelable(key)
    }
}
