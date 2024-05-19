package com.grarcht.shuttle.demo.mvvmwithaservice.viewmodel

import android.content.Context
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import androidx.annotation.RawRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.mvvmwithaservice.model.MessagingAction
import com.grarcht.shuttle.demo.mvvmwithaservice.model.Receiver
import com.grarcht.shuttle.demo.mvvmwithaservice.model.RemoteService
import com.grarcht.shuttle.framework.CARGO_ID_KEY
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.app.ShuttleConnectedServiceModel
import com.grarcht.shuttle.framework.content.service.ShuttleLifecycleAwareServiceConnection
import com.grarcht.shuttle.framework.content.service.ShuttleServiceConnection
import com.grarcht.shuttle.framework.os.ShuttleBinder
import com.grarcht.shuttle.framework.visibility.ShuttleVisibilityReporter
import com.grarcht.shuttle.framework.visibility.error.ShuttleDefaultError
import com.grarcht.shuttle.framework.visibility.observation.ShuttleChannelVisibilityObservable
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CONTEXT_GET_IMAGE = "Get image"
private const val SERVICE_NAME = "DemoService"
private const val UNABLE_TO_SEND_MESSAGE = "Unable to send message using IPC."

/**
 * The MVVM ViewModel used to connect with the [RemoteService].
 *
 * @param shuttle used to store and retrieve cargo
 */
@HiltViewModel
class DemoViewModel @Inject constructor(
    reporter: ShuttleVisibilityReporter,
    private val shuttle: Shuttle
) : ViewModel() {
    private val remoteServiceChannel = Channel<ShuttleConnectedServiceModel<RemoteService>>()
    private val visibilityObservable: ShuttleVisibilityObservable = ShuttleChannelVisibilityObservable(reporter, viewModelScope)
    private var ipcServiceConnection: ShuttleServiceConnection<RemoteService, ShuttleBinder<RemoteService>>? = null
    private var ipcServiceReceiver: Receiver? = null
    private var ipcMessenger: Messenger? = null

    fun initMessaging(context: Context?, lifecycle: Lifecycle) {
        initMessenger()
        initServiceConnection(context, lifecycle)
    }

    private fun initMessenger() {
        // Get the image data from the service
        viewModelScope.launch {
            remoteServiceChannel.consumeAsFlow().collect { model ->
                ipcMessenger = model.ipcMessenger
            }
        }
    }

    private fun initServiceConnection(context: Context?, lifecycle: Lifecycle) {
        // Start and connect to the service
        if (ipcServiceConnection == null) {
            ipcServiceConnection = ShuttleLifecycleAwareServiceConnection(
                RemoteService::class.java,
                context,
                lifecycle,
                SERVICE_NAME,
                visibilityObservable,
                useWithIPC = true,
                viewModelScope,
                remoteServiceChannel
            )
        } else {
            ipcServiceConnection
        }
    }

    private fun initReceiver(context: Context?, handleNoResponseReceived: Boolean = false) {
        ipcServiceReceiver?.unregisterReceiverQuietly()
        ipcServiceReceiver = Receiver(shuttle, viewModelScope, visibilityObservable, handleNoResponseReceived)
        ipcServiceReceiver?.register(context)
    }

    /**
     * Transports the image cargo WITHOUT using Shuttle and is used to demonstrate process crashes when transporting oversized cargo.
     *
     * @param context for registering a [Receiver]
     * @param cargoId for the cargo to transport and pick up
     * @param imageId for the image cargo to load
     */
    fun transportImageCargoWithoutUsingShuttle(
        context: Context?,
        cargoId: String,
        @RawRes imageId: Int
    ): Flow<IOResult>? {
        initReceiver(context, true)

        return if (ipcMessenger != null) {
            val messageWhat = MessagingAction.TRANSPORT_IMAGE_CARGO_WITHOUT_SHUTTLE.actionValue
            val msg: Message = Message.obtain(null, messageWhat, 0, 0)
            msg.data.putString(CARGO_ID_KEY, cargoId)
            msg.data.putInt(RemoteService.KEY_IMAGE_ID, imageId)

            try {
                ipcServiceConnection?.ipcServiceMessenger?.send(msg)
            } catch (e: RemoteException) {
                val message = "$UNABLE_TO_SEND_MESSAGE ${e.message}"
                val error = ShuttleDefaultError.ObservedError(CONTEXT_GET_IMAGE, message, e)
                visibilityObservable.observe(error)
            }

            return ipcServiceReceiver?.flow as Flow<IOResult>
        } else {
            null
        }
    }

    /**
     * Transports the image cargo WITH using Shuttle and is used to demonstrate successful transportation of oversized cargo.
     *
     * @param context for registering a [Receiver]
     * @param cargoId for the cargo to transport and pick up
     * @param imageId for the image cargo to load
     */
    fun transportImageCargoUsingShuttleAndIPC(
        context: Context?,
        cargoId: String,
        @RawRes imageId: Int
    ): Flow<IOResult>? {
        initReceiver(context)

        return if (ipcMessenger != null) {
            val msg: Message = Message.obtain(null, MessagingAction.TRANSPORT_IMAGE_CARGO_WITH_SHUTTLE.actionValue, 0, 0)
            msg.data.putString(CARGO_ID_KEY, cargoId)
            msg.data.putInt(RemoteService.KEY_IMAGE_ID, imageId)

            try {
                ipcServiceConnection?.ipcServiceMessenger?.send(msg)
            } catch (e: RemoteException) {
                val message = "$UNABLE_TO_SEND_MESSAGE ${e.message}"
                val error = ShuttleDefaultError.ObservedError(CONTEXT_GET_IMAGE, message, e)
                visibilityObservable.observe(error)
            }

            return ipcServiceReceiver?.flow as Flow<IOResult>
        } else {
            null
        }
    }

    /**
     * Releases resources.
     */
    override fun onCleared() {
        remoteServiceChannel.close()
        ipcServiceConnection?.disconnectFromService()
        ipcServiceReceiver?.releaseResources()
        super.onCleared()
    }

    companion object {
        const val LOG_TAG = "DemoViewModel"
    }
}
