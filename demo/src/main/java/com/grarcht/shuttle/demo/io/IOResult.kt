package com.grarcht.shuttle.demo.io

sealed class IOResult {
    object Loading : IOResult()
    class Success<D>(val data: D) : IOResult()
    class Error<T>(val throwable: T) : IOResult() where T : Throwable
}