package com.grarcht.shuttle.framework.visibility.observation

import com.grarcht.shuttle.framework.coroutines.channel.closeQuietly
import com.grarcht.shuttle.framework.visibility.ShuttleVisibilityData
import com.grarcht.shuttle.framework.visibility.ShuttleVisibilityReporter
import com.grarcht.shuttle.framework.visibility.error.ShuttleDefaultError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel

private const val CONTEXT = "Customer Experience Visibility."
private const val ERROR_ADDING_CHANNEL = "Unable to add the channel:"
private const val ERROR_CLOSING_CHANNELS = "Unable to close the channels."
private const val ERROR_CLEARING_CHANNELS = "Unable to clear the channels."

/**
 * Observes visibility data, used to provide insight into the customer experience.
 */
open class ShuttleChannelVisibilityObservable(
    private val reporter: ShuttleVisibilityReporter,
    private val coroutineScope: CoroutineScope
) : ShuttleVisibilityObservable {
    private val channels: MutableList<Channel<ShuttleVisibilityData>> = mutableListOf()

    /**
     * Adds a channel used for sending observed visibility data to.
     * @param channel to send the data to
     *
     * @return the reference to this object
     */
    override fun add(channel: Channel<ShuttleVisibilityData>): ShuttleVisibilityObservable {
        try {
            channels.add(channel)
        } catch (e: ConcurrentModificationException) {
            val message = "$ERROR_ADDING_CHANNEL $channel. ${e.message}"
            val error = ShuttleDefaultError.ObservedError(CONTEXT, message, e)
            reporter.reportForVisibilityWith(error)
        }
        return this
    }

    /**
     * Observes visibility data updates.
     *
     * @param visibilityData errors, information, etc.
     *
     * @return the reference to this object
     */
    override fun <E : ShuttleVisibilityData> observe(visibilityData: E): ShuttleVisibilityObservable {
        reporter.reportForVisibilityWith(visibilityData)
        return this
    }

    /**
     * Close the channels and clears the channels list.
     *
     * @return the reference to this object
     */
    override fun dispose(): ShuttleVisibilityObservable {
        closeChannels()
        clearChannels()
        return this
    }

    private fun closeChannels() {
        try {
            channels.forEach {
                it.closeQuietly(scope = coroutineScope)
            }
        } catch (e: ConcurrentModificationException) {
            val message = "$ERROR_CLOSING_CHANNELS ${e.message}"
            val error = ShuttleDefaultError.ObservedError(CONTEXT, message, e)
            reporter.reportForVisibilityWith(error)
        }
    }

    private fun clearChannels() {
        try {
            channels.clear()
        } catch (e: ConcurrentModificationException) {
            val message = "$ERROR_CLEARING_CHANNELS ${e.message}"
            val error = ShuttleDefaultError.ObservedError(CONTEXT, message, e)
            reporter.reportForVisibilityWith(error)
        }
    }
}
