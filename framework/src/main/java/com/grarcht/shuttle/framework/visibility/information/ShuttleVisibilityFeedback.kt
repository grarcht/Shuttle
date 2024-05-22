package com.grarcht.shuttle.framework.visibility.information

import com.grarcht.shuttle.framework.visibility.ShuttleVisibilityData

interface ShuttleVisibilityFeedback : ShuttleVisibilityData {
    /**
     * Represents errors that occur when connecting to a service.
     *
     * @param context provides context into the error. Do NOT use
     * the Android context in string format.  This needs to be a
     * human-readable string for obfuscated apps.
     *
     * @param data useful data for visibility
     *
     * @param message an informational message
     */
    data class Information<D : Any>(
        val context: String,
        val data: D? = null,
        val message: String = ""
    ) : ShuttleVisibilityFeedback
}
