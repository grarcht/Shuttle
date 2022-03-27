package com.grarcht.shuttle.framework.coroutines.channel

import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_LOG_TAG = "RetryIfAvailable"

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
            Log.w(tag, "Caught when relaying.", e)
        } catch (e: CancellationException) {
            Log.w(tag, "Caught when relaying.", e)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Log.w(tag, "Caught when relaying.", e)
        }
    }
}
