package com.grarcht.shuttle.framework.content.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Messenger
import androidx.lifecycle.Lifecycle
import com.grarcht.shuttle.framework.app.ShuttleConnectedServiceModel
import com.grarcht.shuttle.framework.app.ShuttleService
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import com.grarcht.shuttle.framework.visibility.error.ShuttleServiceError
import com.grarcht.shuttle.framework.os.ShuttleBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

private const val UNABLE_TO_CONNECT_MESSAGE = "Unable to connect to the service: "
private const val UNABLE_TO_DISCONNECT_MESSAGE = "Unable to disconnect from the service: "
private const val UNKNOWN_STATE_NAME = "Unknown state."

/**
 * Connects to a [ShuttleService], either remote service with IPC or a local service.
 * @param serviceName used for logging
 * @param errorObservable provides visibility in to possible errors
 * @param useWithIPC true, if this service should be remote and use interprocess communication
 * @param coroutineScope used to emit a [ShuttleConnectedServiceModel] over a [Channel]
 * @param serviceChannel emits a [ShuttleConnectedServiceModel]
 */
@Suppress("MemberVisibilityCanBePrivate")
open class ShuttleServiceConnection<S : ShuttleService, B : ShuttleBinder<S>>(
    private val serviceName: String,
    private val errorObservable: ShuttleVisibilityObservable,
    private val useWithIPC: Boolean = false,
    private val coroutineScope: CoroutineScope,
    private val serviceChannel: Channel<ShuttleConnectedServiceModel<S>>
) : ServiceConnection {
    var localService: S? = null
    var ipcServiceMessenger: Messenger? = null

    protected var context: Context? = null
    protected var isConnected: Boolean = false

    /**
     * Sets the reference for [ipcServiceMessenger] or [localService] and emits a [ShuttleConnectedServiceModel].
     *
     * @param componentName The concrete component name of the service that has been connected.
     * @param service The IBinder of the Service's communication channel, which you can now make calls on.
     *
     * @see [ServiceConnection.onServiceConnected]
     */
    @Suppress("UNCHECKED_CAST")
    override fun onServiceConnected(componentName: ComponentName?, service: IBinder?) {
        if (useWithIPC) { // a remote (non-local) service
            ipcServiceMessenger = Messenger(service)
            emitConnectedServiceModel(ipcServiceMessenger = ipcServiceMessenger)
        } else { // non-IPC service (service is in the app process)
            // If this error (android.os.BinderProxy cannot be cast to ShuttleBinder) is thrown, it
            // means that the app is trying to bind to a remote service and useWithIPC was set to false.
            // UseIPC needs to be set to true or a local service should be used.
            val binder = service as B
            localService = binder.getService()
            emitConnectedServiceModel(localService)
        }

        isConnected = true
    }

    private fun emitConnectedServiceModel(localService: S? = null, ipcServiceMessenger: Messenger? = null) {
        serviceChannel.let {
            coroutineScope.launch {
                val model = ShuttleConnectedServiceModel<S>(localService, ipcServiceMessenger)
                it.send(model)
            }
        }
    }

    /**
     * Resets references.
     *
     * @param componentName The concrete component name of the service whose connection has been lost.
     *
     * @see [ServiceConnection.onServiceDisconnected]
     */
    override fun onServiceDisconnected(componentName: ComponentName?) {
        isConnected = false
        localService = null
        ipcServiceMessenger = null
    }

    /**
     * Connects to the [ShuttleService].
     *
     * @param context used for binding to the service
     * @param serviceClazz for the [ShuttleService] to connect to
     */
    @Suppress("UNCHECKED_CAST")
    open fun connectToService(
        context: Context,
        serviceClazz: Class<S>
    ): ShuttleServiceConnection<S, ShuttleBinder<S>> {
        connectToService(context, serviceClazz, lifecycle = null)
        return this as ShuttleServiceConnection<S, ShuttleBinder<S>>
    }

    /**
     * Connects to the [ShuttleService].
     *
     * @param context used for binding to the service
     * @param serviceClazz for the [ShuttleService] to connect to
     * @param lifecycle used for lifecycle state checks in connecting to the service
     */
    @Suppress("UNCHECKED_CAST")
    open fun connectToService(
        context: Context,
        serviceClazz: Class<S>,
        lifecycle: Lifecycle?
    ): ShuttleServiceConnection<S, ShuttleBinder<S>> {

        try {
            val shouldConnect = lifecycle == null || lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            if (shouldConnect) {
                Intent(context, serviceClazz).also { intent ->
                    context.bindService(intent, this, Context.BIND_AUTO_CREATE)
                }
            }
            this.context = context
        } catch (e: Exception) {
            val lifecycleStateName = lifecycle?.currentState?.name ?: UNKNOWN_STATE_NAME
            val message = "$UNABLE_TO_CONNECT_MESSAGE ${e.message}"
            val error = ShuttleServiceError.ConnectToServiceError(serviceName, lifecycleStateName, message, e)
            errorObservable.observe(error)
        }
        return this as ShuttleServiceConnection<S, ShuttleBinder<S>>
    }

    /**
     * Disconnects from the [ShuttleService].
     */
    open fun disconnectFromService() {
        if (isConnected) {
            try {
                context?.let {
                    it.unbindService(this)
                    context = null
                }
            } catch (e: Exception) {
                val message = "$UNABLE_TO_DISCONNECT_MESSAGE ${e.message}"
                val error = ShuttleServiceError.DisconnectFromServiceError(serviceName, Unit, message, e)
                errorObservable.observe(error)
            }
        }
    }
}