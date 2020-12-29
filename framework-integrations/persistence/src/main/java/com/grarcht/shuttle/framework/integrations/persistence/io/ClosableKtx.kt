package com.grarcht.shuttle.framework.integrations.persistence.io

import java.io.Closeable
import java.io.IOException

/**
 * Closes [Closeable]s without logging exceptions when an [IOException] occurs with closing the [Closeable].
 * Additionally, if the closable reference is null, no action will be taken.
 */
fun Closeable?.closeQuietly() {
    this?.let {
        try {
            this.close()
        } catch (ioe: IOException) {
            // ignore on purpose
        }
    }
}
