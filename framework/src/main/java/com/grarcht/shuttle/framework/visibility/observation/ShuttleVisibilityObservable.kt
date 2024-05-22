package com.grarcht.shuttle.framework.visibility.observation

import com.grarcht.shuttle.framework.visibility.ShuttleVisibilityData
import kotlinx.coroutines.channels.Channel

/**
 * Observes visibility data, used to provide insight into the customer experience.
 */
interface ShuttleVisibilityObservable {
    /**
     * Adds a channel used for sending observed visibility data to.
     * @param channel to send the data to
     *
     * @return the reference to this object
     */
    fun add(channel: Channel<ShuttleVisibilityData>): ShuttleVisibilityObservable

    /**
     * Disposes the channels.
     *
     * @return the reference to this object
     */
    fun dispose(): ShuttleVisibilityObservable

    /**
     * Observes visibility data updates.
     *
     * @param visibilityData errors, information, etc.
     */
    fun <D : ShuttleVisibilityData> observe(visibilityData: D): ShuttleVisibilityObservable
}
