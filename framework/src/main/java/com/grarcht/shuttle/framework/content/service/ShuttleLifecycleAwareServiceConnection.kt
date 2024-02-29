package com.grarcht.shuttle.framework.content.service

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.grarcht.shuttle.framework.app.ShuttleConnectedServiceModel
import com.grarcht.shuttle.framework.app.ShuttleService
import com.grarcht.shuttle.framework.error.ShuttleErrorObservable
import com.grarcht.shuttle.framework.os.ShuttleBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel

class ShuttleLifecycleAwareServiceConnection<S : ShuttleService, B : ShuttleBinder<S>>(
    private val serviceClazz: Class<S>,
    context: Context? = null,
    serviceName: String,
    errorObserver: ShuttleErrorObservable,
    useWithIPC: Boolean = false,
    coroutineScope: CoroutineScope
) : ShuttleServiceConnection<S, B>(serviceName, errorObserver, useWithIPC, coroutineScope), DefaultLifecycleObserver {

    init {
        super.context = context
    }

    override fun onStart(owner: LifecycleOwner) {
        if (context != null && serviceChannel != null) {
            super.connectToService(
                context as Context,
                serviceClazz,
                lifecycle = owner.lifecycle,
                serviceChannel as Channel<ShuttleConnectedServiceModel<S>>
            )
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.disconnectFromService()
    }
}