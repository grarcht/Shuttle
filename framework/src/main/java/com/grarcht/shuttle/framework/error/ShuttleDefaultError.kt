package com.grarcht.shuttle.framework.error

interface ShuttleDefaultError : ShuttleError {
    data class ObservedError(
        val context: String,
        val errorMessage: String,
        val error: Throwable
    ) : ShuttleDefaultError

}