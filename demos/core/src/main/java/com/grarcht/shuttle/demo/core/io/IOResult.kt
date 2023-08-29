package com.grarcht.shuttle.demo.core.io

/**
 * This class represents the LCE (Loading-Content-Error) states for IO bound work.
 */
sealed class IOResult {
    /**
     * State for other than [Loading], [Success], and [Error].
     */
    object Unknown : IOResult()

    /**
     * State for loading data.
     */
    object Loading : IOResult()

    /**
     * State for successful IO work.
     * @param data retrieved/sent data
     */
    class Success<D>(val data: D) : IOResult()

    /**
     * State for unsuccessful IO work.
     * @param throwable caught with IO work
     */
    class Error<T>(val throwable: T) : IOResult() where T : Throwable
}
