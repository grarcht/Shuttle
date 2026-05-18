package com.grarcht.shuttle.framework.content

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import com.grarcht.shuttle.framework.ExcludeFromCoverage

private const val ERROR_UNABLE_TO_UNREGISTER_RECEIVER = "Unable to unregister the receiver."
private const val ERROR_UNABLE_TO_REGISTER_RECEIVER = "Unable to register the receiver."

/**
 * Registers a [BroadcastReceiver] with this [Context], suppressing any [IllegalStateException]
 * that may be thrown if the receiver cannot be registered at the time of the call. On Android
 * TIRAMISU (API 33) and above, the receiver is registered as exported so it can receive broadcasts
 * from other applications.
 *
 * @param receiver The [BroadcastReceiver] to register.
 * @param filter The [IntentFilter] that describes the broadcasts the receiver should listen for.
 * @param logTag An optional tag used when logging a warning if registration fails.
 */
@Suppress("unused")
@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context?.registerReceiverQuietly(receiver: BroadcastReceiver, filter: IntentFilter, logTag: String? = null) {
    this?.let {
        try {
            it.registerReceiverByVersion(receiver, filter)
        } catch (e: IllegalStateException) {
            Log.w(logTag, ERROR_UNABLE_TO_REGISTER_RECEIVER, e)
        }
    }
}

@ExcludeFromCoverage
@SuppressLint("UnspecifiedRegisterReceiverFlag")
private fun Context.registerReceiverByVersion(receiver: BroadcastReceiver, filter: IntentFilter) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
    } else {
        registerReceiver(receiver, filter)
    }
}

/**
 * Unregisters a [BroadcastReceiver] from this [Context], suppressing any [IllegalStateException]
 * that may be thrown if the receiver was never registered or has already been unregistered.
 *
 * @param receiver The [BroadcastReceiver] to unregister.
 * @param logTag An optional tag used when logging a warning if unregistration fails.
 */
fun Context?.unregisterReceiverQuietly(receiver: BroadcastReceiver, logTag: String? = null) {
    this?.let {
        try {
            it.unregisterReceiver(receiver)
        } catch (e: IllegalStateException) {
            Log.w(logTag, ERROR_UNABLE_TO_UNREGISTER_RECEIVER, e)
        }
    }
}
