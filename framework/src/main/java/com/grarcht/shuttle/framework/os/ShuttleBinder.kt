package com.grarcht.shuttle.framework.os

import android.os.Binder
import com.grarcht.shuttle.framework.app.ShuttleService

/**
 * Binds clients to the [service].
 * @param service - to bind to
 */
data class ShuttleBinder<T : ShuttleService>(private val service: T) : Binder() {
    // Enables clients to call service functions
    fun getService(): T = service
}
