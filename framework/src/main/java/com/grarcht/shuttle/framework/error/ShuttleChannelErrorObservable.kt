package com.grarcht.shuttle.framework.error

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

private const val ERROR_ADD = "Unable to add channel while iterating over channels."
private const val ERROR_HANDLE_ERROR = "Unable to handle the error while adding a channel."
private const val TAG = "ShuttleChannelErrorObservable"

class ShuttleChannelErrorObservable(
    private val coroutineScope: CoroutineScope
) : ShuttleErrorObservable {
    private val channels: MutableList<SendChannel<ShuttleError>> = mutableListOf()

    override fun add(channel: SendChannel<ShuttleError>): ShuttleErrorObservable {
        try {
            channels.add(channel)
        } catch (e: ConcurrentModificationException) {
            Log.w(TAG, ERROR_ADD)
        }
        return this
    }

    override fun <E : ShuttleError> onError(error: E): ShuttleErrorObservable {
        coroutineScope.launch {
            try {
                channels.forEach {
                    it.send(error)
                }
            } catch (e: ConcurrentModificationException) {
                Log.w(TAG, ERROR_HANDLE_ERROR)
            }
        }
        return this
    }

    override fun dispose(): ShuttleErrorObservable {
        channels.clear()
        return this
    }
}