package com.grarcht.shuttle.framework.os.messenger

import android.content.Context
import android.os.Looper
import com.grarcht.shuttle.framework.app.ShuttleService
import com.grarcht.shuttle.framework.error.ShuttleErrorObservable

class ShuttleServiceMessengerFactory : ShuttleMessengerFactory {
    override fun createMessenger(
        looper: Looper,
        serviceName: String,
        context: Context,
        shuttleService: ShuttleService,
        errorObserver: ShuttleErrorObservable
    ): ShuttleMessengerDecorator {
        return ShuttleMessengerDecorator(
            looper,
            serviceName,
            context,
            shuttleService,
            errorObserver
        )
    }
}