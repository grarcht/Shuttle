package com.grarcht.shuttle.framework.os.messenger.factory

import android.os.Looper
import android.os.Message
import com.grarcht.shuttle.framework.os.messenger.ShuttleMessageReceiver
import com.grarcht.shuttle.framework.os.messenger.ShuttleMessengerDecorator
import com.grarcht.shuttle.framework.validator.ShuttleValidator
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable

/**
 * Creates [ShuttleMessengerDecorator]s. This class is based the factory design pattern. For
 * more information, refer to:
 * <a href="https://www.tutorialspoint.com/design_pattern/factory_pattern.htm">Factory Design Pattern</a>
 */
class ShuttleServiceMessengerFactory : ShuttleMessengerFactory {

    /**
     * Creates a [ShuttleMessengerDecorator].
     *
     * @param looper used to create the handler for the messenger
     * @param serviceName used for error handling and logging
     * @param messageReceiver receives and handles messages
     * @param errorObservable provides visibility into errors
     * @param messageValidator validates received messages
     */
    override fun createMessenger(
        looper: Looper,
        serviceName: String,
        messageReceiver: ShuttleMessageReceiver,
        errorObservable: ShuttleVisibilityObservable,
        messageValidator: ShuttleValidator<Message>
    ): ShuttleMessengerDecorator {
        return ShuttleMessengerDecorator(
            looper,
            serviceName,
            messageReceiver,
            errorObservable,
            messageValidator
        )
    }
}
