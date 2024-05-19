package com.grarcht.shuttle.framework.content.service

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.grarcht.shuttle.framework.app.ShuttleConnectedServiceModel
import com.grarcht.shuttle.framework.app.ShuttleService
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import com.grarcht.shuttle.framework.os.ShuttleBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel

/**
 * A lifecycle aware service connection, which connects to the [ShuttleService] when [onStart] is called and disconnects the service
 * when [onStop] is called.
 *
 * @param serviceClazz the [ShuttleService] class to connect to
 * @param context used for service binding
 * @param lifecycle used to connect to and disconnect from the [ShuttleService]
 * @param serviceName used for logging
 * @param errorObserver provides visibility in to possible errors
 * @param useWithIPC true, if this service should be remote and use interprocess communication
 * @param coroutineScope used to emit a [ShuttleConnectedServiceModel] over a [Channel]
 * @param serviceChannel emits a [ShuttleConnectedServiceModel]
 */
class ShuttleLifecycleAwareServiceConnection<S : ShuttleService, B : ShuttleBinder<S>>(
    private val serviceClazz: Class<S>,
    context: Context? = null,
    private val lifecycle: Lifecycle,
    serviceName: String,
    errorObserver: ShuttleVisibilityObservable,
    useWithIPC: Boolean = false,
    coroutineScope: CoroutineScope,
    serviceChannel: Channel<ShuttleConnectedServiceModel<S>>
) : ShuttleServiceConnection<S, B>(serviceName, errorObserver, useWithIPC, coroutineScope, serviceChannel), DefaultLifecycleObserver {

    init {
        super.context = context
        lifecycle.addObserver(this)
    }

    /**
     * Connects to the [ShuttleService].
     * @param owner used for lifecycle state checks in connecting to the service
     */
    override fun onStart(owner: LifecycleOwner) {
        context?.let {
            super.connectToService(
                context as Context,
                serviceClazz,
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
     * Stops observing the [lifecycle].
     * @param owner used for observing the lifecycle
     */
    override fun onDestroy(owner: LifecycleOwner) {
        lifecycle.removeObserver(this)
    }
}