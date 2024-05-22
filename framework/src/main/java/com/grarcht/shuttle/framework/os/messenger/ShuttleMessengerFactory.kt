package com.grarcht.shuttle.framework.os.messenger

import android.os.Looper
import android.os.Message
import com.grarcht.shuttle.framework.validator.ShuttleValidator
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable

/**
 * Creates [ShuttleMessengerDecorator]s. This interface is based the factory design pattern. For
 * more information, refer to:
 * <a href="https://www.tutorialspoint.com/design_pattern/factory_pattern.htm">Factory Design Pattern</a>
 */
interface ShuttleMessengerFactory {

    /**
     * Creates a [ShuttleMessengerDecorator].
     *
     * @param looper used to create the handler for the messenger
     * @param serviceName used for error handling and logging
     * @param messageReceiver receives and handles messages
     * @param errorObservable provides visibility into errors
     * @param messageValidator: ShuttleValidator<Message>
     */
    fun createMessenger(
        looper: Looper,
        serviceName: String,
        messageReceiver: ShuttleMessageReceiver,
        errorObservable: ShuttleVisibilityObservable,
        messageValidator: ShuttleValidator<Message>
    ): ShuttleMessengerDecorator
}
