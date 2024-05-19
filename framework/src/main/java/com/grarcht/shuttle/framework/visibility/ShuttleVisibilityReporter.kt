package com.grarcht.shuttle.framework.visibility

/**
 * Responsible for reporting data for visibility.
 */
interface ShuttleVisibilityReporter {

    /**
     * Reports data for visibility.
     *
     * @param visibilityData
     */
    fun <D : ShuttleVisibilityData> reportForVisibilityWith(visibilityData: D)
}