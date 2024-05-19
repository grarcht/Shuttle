package com.grarcht.shuttle.framework.content

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.util.Log

private const val ERROR_UNABLE_TO_UNREGISTER_RECEIVER = "Unable to unregister the receiver."
private const val ERROR_UNABLE_TO_REGISTER_RECEIVER = "Unable to register the receiver."

@Suppress("unused")
@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context?.registerReceiverQuietly(receiver: BroadcastReceiver, filter: IntentFilter, logTag: String? = null) {
    this?.let {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                it.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                it.registerReceiver(receiver, filter)
            }
        } catch (e: IllegalStateException) {
            Log.w(logTag, ERROR_UNABLE_TO_REGISTER_RECEIVER, e)
        }
    }
}

fun Context?.unregisterReceiverQuietly(receiver: BroadcastReceiver, logTag: String? = null) {
    this?.let {
        try {
            it.unregisterReceiver(receiver)
        } catch (e: IllegalStateException) {
            Log.w(logTag, ERROR_UNABLE_TO_UNREGISTER_RECEIVER, e)
        }
    }
}