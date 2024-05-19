package com.grarcht.shuttle.framework.visibility.error

/**
 * The default error type for errors that occur during customer journeys.
 */
interface ShuttleDefaultError : ShuttleError {

    /**
     * @param context provides context into the error. Do NOT use
     * the Android context in string format.  This needs to be a
     * human-readable string for obfuscated apps.
     *
     * @param errorMessage the message for the error
     *
     * @param error the observed error
     */
    data class ObservedError(
        val context: String,
        val errorMessage: String = "",
        val error: Throwable
    ) : ShuttleDefaultError
}