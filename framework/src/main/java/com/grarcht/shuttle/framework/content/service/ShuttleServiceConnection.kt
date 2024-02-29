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
import com.grarcht.shuttle.framework.error.ShuttleErrorObservable
import com.grarcht.shuttle.framework.error.ShuttleServiceError
import com.grarcht.shuttle.framework.os.ShuttleBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

private const val UNABLE_TO_CONNECT_MESSAGE = "Unable to connect to the service: "
private const val UNABLE_TO_DISCONNECT_MESSAGE = "Unable to disconnect from the service: "
private const val UNKNOWN_STATE_NAME = "Unknown state."

/**
 * @param connectedService
 * @param isConnected
 */
@Suppress("MemberVisibilityCanBePrivate")
open class ShuttleServiceConnection<S : ShuttleService, B : ShuttleBinder<S>>(
    private val serviceName: String,
    private val errorObservable: ShuttleErrorObservable,
    private val useWithIPC: Boolean = false,
    private val coroutineScope: CoroutineScope
) : ServiceConnection {
    var localService: S? = null
    var ipcServiceMessenger: Messenger? = null

    protected var context: Context? = null
    protected var isConnected: Boolean = false
    open var serviceChannel: Channel<ShuttleConnectedServiceModel<S>>? = null

    fun isConnectedToService(): Boolean = isConnected

    @Suppress("UNCHECKED_CAST")
    override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
        val binder = service as B

        if (useWithIPC) {
            ipcServiceMessenger = Messenger(service)
            emitConnectedServiceModel(ipcServiceMessenger = ipcServiceMessenger)
        } else { // non-IPC service (service is in the app process)
            localService = binder.getService()
            emitConnectedServiceModel(localService)
        }

        isConnected = true
    }

    private fun emitConnectedServiceModel(localService: S? = null, ipcServiceMessenger: Messenger? = null) {
        serviceChannel?.let {
            coroutineScope.launch {
                val model = ShuttleConnectedServiceModel<S>(localService, ipcServiceMessenger)
                it.send(model)
            }
        }
    }

    override fun onServiceDisconnected(className: ComponentName?) {
        isConnected = false
        localService = null
        ipcServiceMessenger = null
    }

    @Suppress("UNCHECKED_CAST")
    open fun connectToService(
        context: Context,
        serviceClazz: Class<S>,
        serviceChannel: Channel<ShuttleConnectedServiceModel<S>>
    ): ShuttleServiceConnection<S, ShuttleBinder<S>> {
        connectToService(context, serviceClazz, lifecycle = null, serviceChannel)
        return this as ShuttleServiceConnection<S, ShuttleBinder<S>>
    }

    @Suppress("UNCHECKED_CAST")
    open fun connectToService(
        context: Context,
        serviceClazz: Class<S>,
        lifecycle: Lifecycle?,
        serviceChannel: Channel<ShuttleConnectedServiceModel<S>>
    ): ShuttleServiceConnection<S, ShuttleBinder<S>> {
        this.serviceChannel = serviceChannel

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
            errorObservable.onError(error)
        }
        return this as ShuttleServiceConnection<S, ShuttleBinder<S>>
    }

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
                errorObservable.onError(error)
            }
        }
    }
}