package com.grarcht.shuttle.framework.error

interface ShuttleServiceError : ShuttleError {
    data class ConnectToServiceError<D : Any>(
        val serviceName: String,
        val data: D? = null,
        val errorMessage: String,
        val error: Throwable
    ) : ShuttleServiceError

    data class DisconnectFromServiceError<D : Any>(
        val serviceName: String,
        val data: D? = null,
        val errorMessage: String,
        val error: Throwable
    ) : ShuttleServiceError

    data class HandleMessageError<D : Any>(
        val serviceName: String,
        val data: D? = null,
        val errorMessage: String,
        val error: Throwable
    ) : ShuttleServiceError
}