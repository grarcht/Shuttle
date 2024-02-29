package com.grarcht.shuttle.framework.os.messenger

import android.content.Context
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import com.grarcht.shuttle.framework.CARGO_ID_KEY
import com.grarcht.shuttle.framework.NO_CARGO_ID
import com.grarcht.shuttle.framework.app.ShuttleService
import com.grarcht.shuttle.framework.error.ShuttleErrorObservable
import com.grarcht.shuttle.framework.error.ShuttleServiceError
import com.grarcht.shuttle.framework.validator.ShuttleServiceMessageValidator

private const val UNABLE_TO_HANDLE_MESSAGE = "Unable to handle the message: "

open class ShuttleMessengerDecorator(
    looper: Looper,
    private val serviceName: String,
    private val context: Context,
    private val shuttleService: ShuttleService,
    private val errorObservable: ShuttleErrorObservable
) {
    val cargoIds: MutableList<String> = mutableListOf()
    val rawMessenger: Messenger = Messenger(Handler(looper, Callback()))

    fun clearCargoIds() {
        if(cargoIds.isNotEmpty()){
            cargoIds.clear()
        }
    }

    fun getBinder(): IBinder? = rawMessenger.binder

    inner class Callback : Handler.Callback {
        private val messageValidator = ShuttleServiceMessageValidator()

        override fun handleMessage(msg: Message): Boolean {
            var handled = false
            val isMessageValid = messageValidator.validate(msg)

            if (isMessageValid) {
                try {
                    shuttleService.onReceiveMessage(context, msg.what, msg)
                    val cargoId = msg.data.getString(CARGO_ID_KEY, NO_CARGO_ID)
                    cargoIds.add(cargoId)
                    handled = true
                } catch (t: Throwable) {
                    val message = "$UNABLE_TO_HANDLE_MESSAGE ${t.message}"
                    val error = ShuttleServiceError.HandleMessageError(serviceName, msg.what, message, t)
                    errorObservable.onError(error)
                }
            }
            return handled
        }
    }
}