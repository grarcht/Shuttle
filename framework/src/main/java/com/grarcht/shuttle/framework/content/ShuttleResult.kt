package com.grarcht.shuttle.framework.content

sealed class ShuttleResult {
    object Loading : ShuttleResult()
    class Success<D>(val data: D) : ShuttleResult()
    class Error<T>(val throwable: T) : ShuttleResult() where T : Throwable
}