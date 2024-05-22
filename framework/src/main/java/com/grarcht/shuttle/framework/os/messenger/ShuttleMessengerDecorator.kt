package com.grarcht.shuttle.framework.os.messenger

import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import com.grarcht.shuttle.framework.CARGO_ID_KEY
import com.grarcht.shuttle.framework.NO_CARGO_ID
import com.grarcht.shuttle.framework.app.ShuttleService
import com.grarcht.shuttle.framework.validator.ShuttleValidator
import com.grarcht.shuttle.framework.visibility.error.ShuttleServiceError
import com.grarcht.shuttle.framework.visibility.information.ShuttleVisibilityFeedback.Information
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable

private const val INVALID_CARGO = "Unable to process the message with invalid cargo:"
private const val UNABLE_TO_HANDLE_MESSAGE = "Unable to handle the message: "
private const val VISIBILITY_CONTEXT = ""

/**
 * Decorates a [Messenger] with cargo validation.  This class is based on the Decorator Design Pattern.
 * For more information, refer to:
 * <a href="https://www.tutorialspoint.com/design_pattern/decorator_pattern.htm">Decorator Design Pattern</a>
 *
 * @param looper used to create the handler for the messenger
 * @param serviceName used for error handling and logging
 * @param messageReceiver receives and handles messages
 * @param visibilityObservable provides visibility into errors
 * @param messageValidator: ShuttleValidator<Message>
 */
open class ShuttleMessengerDecorator(
    looper: Looper,
    private val serviceName: String,
    private val messageReceiver: ShuttleMessageReceiver,
    private val visibilityObservable: ShuttleVisibilityObservable,
    private val messageValidator: ShuttleValidator<Message>
) {
    val cargoIds: MutableList<String> = mutableListOf()
    private val rawMessenger: Messenger = Messenger(Handler(looper, Callback()))

    /**
     * Provides validated cargoIds.
     */
    fun clearCargoIds() {
        if (cargoIds.isNotEmpty()) {
            cargoIds.clear()
        }
    }

    /**
     * Provides the messenger binder for binding to the [ShuttleService]'s messenger.
     */
    fun getBinder(): IBinder? = rawMessenger.binder

    private inner class Callback : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            var handled = false
            val isMessageValid = messageValidator.validate(msg)
            val cargoId = msg.data.getString(CARGO_ID_KEY, NO_CARGO_ID)

            if (isMessageValid) {
                try {
                    cargoIds.add(cargoId)

                    messageReceiver.onReceiveMessage(msg.what, msg)

                    handled = true
                } catch (t: Throwable) {
                    val message = "$UNABLE_TO_HANDLE_MESSAGE ${t.message}"
                    val error = ShuttleServiceError.HandleMessageError(serviceName, msg.what, message, t)
                    visibilityObservable.observe(error)
                }
            } else {
                val information = Information<Unit>(VISIBILITY_CONTEXT, message = "$INVALID_CARGO $cargoId")
                visibilityObservable.observe(information)
            }
            return handled
        }
    }
}
