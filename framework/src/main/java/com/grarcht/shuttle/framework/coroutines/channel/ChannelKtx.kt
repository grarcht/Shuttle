package com.grarcht.shuttle.framework.coroutines.channel

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_LOG_TAG = "RetryIfAvailable"
private const val ERROR_UNABLE_TO_CLOSE_CHANNEL = "Unable to close the channel."

/**
 * Relays the flow of data if this [Channel] and the [receiver] [`Channel] are not null.
 * @param receiver to relay the data to
 * @param logTag for log messages if exceptions were to occur with receiving
 */
suspend fun <E> Channel<E>?.relayFlowIfAvailable(receiver: Channel<E>? = null, logTag: String? = null) {
    if (null != this && null != receiver) {
        val tag = logTag ?: DEFAULT_LOG_TAG
        try {
            consumeAsFlow().collectLatest {
                receiver.send(it)
            }
        } catch (e: ClosedReceiveChannelException) {
            Log.w(tag, "Caught when relaying: ", e)
        } catch (e: CancellationException) {
            Log.w(tag, "Caught when relaying: ", e)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Log.w(tag, "Caught when relaying: ", e)
        }
    }
}

fun  <E> Channel<E>?.closeQuietly(scope: CoroutineScope?,
                                  cause: CancellationException? = null,
                                  logTag: String? = null
) {
    scope?.let {
        try {
            scope.cancel(cause)
        } catch (e: IllegalStateException) {
            Log.w(logTag, ERROR_UNABLE_TO_CLOSE_CHANNEL, e)
        }
    }
}
