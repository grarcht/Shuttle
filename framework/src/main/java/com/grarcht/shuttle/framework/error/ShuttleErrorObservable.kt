package com.grarcht.shuttle.framework.error

import kotlinx.coroutines.channels.SendChannel

interface ShuttleErrorObservable {
    fun add(channel: SendChannel<ShuttleError>): ShuttleErrorObservable

    fun dispose(): ShuttleErrorObservable

    fun <E : ShuttleError> onError(error: E): ShuttleErrorObservable
}