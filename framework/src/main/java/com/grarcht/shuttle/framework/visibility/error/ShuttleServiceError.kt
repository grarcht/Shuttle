package com.grarcht.shuttle.framework.visibility.error

import android.os.Message
import com.grarcht.shuttle.framework.app.ShuttleService

/**
 * The error type for [ShuttleService].
 */
interface ShuttleServiceError : ShuttleError {
    /**
     * Represents errors that occur when connecting to a service.
     *
     * @param serviceName used for service identification
     * @param data useful data for visibility
     * @param errorMessage the message for the thrown error
     * @param error the thrown error
     */
    data class ConnectToServiceError<D : Any>(
        val serviceName: String,
        val data: D? = null,
        val errorMessage: String = "",
        val error: Throwable
    ) : ShuttleServiceError

    /**
     * Represents errors that occur when disconnecting from a service.
     *
     * @param serviceName used for service identification
     * @param data useful data for visibility
     * @param errorMessage the message for the thrown error
     * @param error the thrown error
     */
    data class DisconnectFromServiceError<D : Any>(
        val serviceName: String,
        val data: D? = null,
        val errorMessage: String = "",
        val error: Throwable
    ) : ShuttleServiceError

    /**
     * Represents errors that occur when handling IPC [Message].
     *
     * @param serviceName used for service identification
     * @param data useful data for visibility
     * @param errorMessage the message for the thrown error
     * @param error the thrown error
     */
    data class HandleMessageError<D : Any>(
        val serviceName: String,
        val data: D? = null,
        val errorMessage: String = "",
        val error: Throwable
    ) : ShuttleServiceError


    /**
     * Represents non-specific errors.
     *
     * @param serviceName used for service identification
     * @param data useful data for visibility
     * @param errorMessage the message for the thrown error
     * @param error the thrown error
     */
    data class GeneralError<D : Any>(
        val serviceName: String,
        val data: D? = null,
        val errorMessage: String = "",
        val error: Throwable
    ) : ShuttleServiceError
}