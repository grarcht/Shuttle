package com.grarcht.shuttle.demo.mvvmwithaservice.visibility

import android.util.Log
import com.grarcht.shuttle.framework.visibility.ShuttleVisibilityData
import com.grarcht.shuttle.framework.visibility.ShuttleVisibilityReporter
import com.grarcht.shuttle.framework.visibility.error.ShuttleDefaultError
import com.grarcht.shuttle.framework.visibility.error.ShuttleServiceError
import com.grarcht.shuttle.framework.visibility.information.ShuttleVisibilityFeedback

private const val LOG_TAG = "VisibilityReporter"

/**
 * Reports the visibility data to the logger.  With some apps one may choose to hook it up to
 * Firebase, Datadog, Kibana, Mixpanel, or other systems.
 */
class DefaultLoggerVisibilityReporter(
    private val logVisibilityInformation: Boolean = true
) : ShuttleVisibilityReporter {

    /**
     * Reports the visibility data to the logger.
     */
    override fun <D : ShuttleVisibilityData> reportForVisibilityWith(visibilityData: D) {
        if (!logVisibilityInformation) return

        when (visibilityData) {
            is ShuttleVisibilityFeedback.Information<*> -> Log.i(LOG_TAG, visibilityData.message)
            is ShuttleDefaultError.ObservedError -> Log.e(LOG_TAG, visibilityData.errorMessage, visibilityData.error)
            is ShuttleServiceError.GeneralError<*> -> Log.e(LOG_TAG, visibilityData.errorMessage, visibilityData.error)
            is ShuttleServiceError.ConnectToServiceError<*> -> Log.e(
                LOG_TAG,
                visibilityData.errorMessage,
                visibilityData.error
            )
            is ShuttleServiceError.DisconnectFromServiceError<*> -> Log.e(
                LOG_TAG,
                visibilityData.errorMessage,
                visibilityData.error
            )
            is ShuttleServiceError.HandleMessageError<*> -> Log.e(
                LOG_TAG,
                visibilityData.errorMessage,
                visibilityData.error
            )
        }
    }
}
