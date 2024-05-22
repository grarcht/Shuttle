package com.grarcht.shuttle.framework.content.serviceconnection.lifecycleaware

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.grarcht.shuttle.framework.app.ShuttleService
import com.grarcht.shuttle.framework.content.serviceconnection.ShuttleServiceConnection
import com.grarcht.shuttle.framework.os.ShuttleBinder

/**
 * A lifecycle aware service connection, which connects to the [ShuttleService] when [onStart] is called and disconnects the service
 * when [onStop] is called.
 *
 * @param config
 */
class ShuttleLifecycleAwareServiceConnection<S : ShuttleService, B : ShuttleBinder<S>>(
    private val config: ShuttleLifecycleAwareServiceConnectionConfig<S>
) : ShuttleServiceConnection<S, B>(
    config.serviceConnectionFactory.createShuttleServiceConnectionConfig(config)
), DefaultLifecycleObserver {

    init {
        config.lifecycle.addObserver(this)
    }

    /**
     * Connects to the [ShuttleService].
     * @param owner used for lifecycle state checks in connecting to the service
     */
    override fun onStart(owner: LifecycleOwner) {
        context?.let {
            super.connectToService(
                context as Context,
                config.serviceClazz,
                lifecycle = owner.lifecycle
            )
        }
    }

    /**
     * Disconnects from the [ShuttleService].
     * @param owner unused
     */
    override fun onStop(owner: LifecycleOwner) {
        disconnectFromService()
    }

    /**
     * Stops observing the [Lifecycle].
     * @param owner used for observing the lifecycle
     */
    override fun onDestroy(owner: LifecycleOwner) {
        config.lifecycle.removeObserver(this)
    }
}
