package com.grarcht.shuttle.demo.mvvm.viewmodel

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Message
import android.os.RemoteException
import androidx.annotation.RawRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.mvvm.model.DemoService
import com.grarcht.shuttle.demo.mvvm.model.ImageResult
import com.grarcht.shuttle.framework.CARGO_ID_KEY
import com.grarcht.shuttle.framework.app.ShuttleConnectedServiceModel
import com.grarcht.shuttle.framework.content.service.ShuttleLifecycleAwareServiceConnection
import com.grarcht.shuttle.framework.content.service.ShuttleServiceConnection
import com.grarcht.shuttle.framework.content.unregisterReceiverQuietly
import com.grarcht.shuttle.framework.coroutines.channel.closeQuietly
import com.grarcht.shuttle.framework.error.ShuttleChannelErrorObservable
import com.grarcht.shuttle.framework.error.ShuttleDefaultError
import com.grarcht.shuttle.framework.error.ShuttleErrorObservable
import com.grarcht.shuttle.framework.os.ShuttleBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

private const val CONTEXT_GET_IMAGE = "Get image"
private const val SERVICE_NAME = "DemoService"
private const val UNABLE_TO_SEND_MESSAGE = "Unable to send message using IPC."

/**
 * The MVVM ViewModel used with the First View and corresponding model.
 */
class DemoViewModel : ViewModel() {
    val serviceChannel = Channel<ShuttleConnectedServiceModel<DemoService>>()
    val errorObservable: ShuttleErrorObservable = ShuttleChannelErrorObservable(viewModelScope)

    private var ipcServiceConnection: ShuttleServiceConnection<DemoService, ShuttleBinder<DemoService>>? = null
    private var ipcServiceReceiver: Receiver? = null
    private var localServiceConnection: ShuttleServiceConnection<DemoService, ShuttleBinder<DemoService>>? = null
    private var localServiceReceiver: Receiver? = null


    // This function shows using the view model without databinding. This is one of the ways that MVVM is applied in
    // apps.  For a demonstration with Databinding, look at the SecondViewModel class.
    fun getImageWithLocalServiceAndWithoutUsingShuttle(context: Context?, @RawRes imageId: Int): Flow<IOResult>? {
        return if (context != null) {
            // Get ready to receive the image data, sent via a broadcast
            val logTag = "getImageWithLocalServiceAndWithoutUsingShuttle"
            localServiceReceiver = if (localServiceReceiver == null) Receiver(viewModelScope, logTag) else localServiceReceiver
            localServiceReceiver?.register(context)

            // Get the image data from the service
            viewModelScope.launch {
                serviceChannel.consumeAsFlow().collectLatest { model ->
                    model.localService?.let {
                        val demoService: DemoService = it
                        demoService.getImageBytesWithoutShuttle(imageId)
                    }
                }
            }

            // Start and connect to the service
            if (localServiceConnection == null) {
                localServiceConnection = ShuttleLifecycleAwareServiceConnection(
                    DemoService::class.java,
                    context,
                    SERVICE_NAME,
                    errorObservable,
                    useWithIPC = false,
                    viewModelScope
                )
            } else {
                localServiceConnection
            }

            if (localServiceConnection?.isConnectedToService() == false) {
                localServiceConnection?.connectToService(context, DemoService::class.java, serviceChannel)
            }

            return localServiceReceiver?.flow as Flow<IOResult>
        } else {
            null
        }
    }

    fun getImageUsingShuttleAndIPC(context: Context?, cargoId: String, @RawRes imageId: Int): Flow<IOResult>? {
        return if (context != null) {
            // Get ready to receive the image data, sent via a broadcast
            val logTag = "getImageUsingShuttleAndIPC"
            ipcServiceReceiver = if (ipcServiceReceiver == null) Receiver(viewModelScope, logTag) else ipcServiceReceiver
            ipcServiceReceiver?.register(context)

            // Get the image data from the service
            viewModelScope.launch {
                serviceChannel.consumeAsFlow().collectLatest { model ->
                    model.ipcMessenger?.let {
                        val msg: Message = Message.obtain(null, DemoService.ACTION_GET_IMAGE, 0, 0)
                        msg.data.putString(CARGO_ID_KEY, cargoId)
                        msg.data.putInt(DemoService.KEY_IMAGE_ID, imageId)

                        try {
                            ipcServiceConnection?.ipcServiceMessenger?.send(msg)
                        } catch (e: RemoteException) {
                            val message = "$UNABLE_TO_SEND_MESSAGE ${e.message}"
                            val error = ShuttleDefaultError.ObservedError(CONTEXT_GET_IMAGE, message, e)
                            errorObservable.onError(error)
                        }
                    }
                }
            }

            // Start and connect to the service
            if (ipcServiceConnection == null) {
                ipcServiceConnection = ShuttleLifecycleAwareServiceConnection(
                    DemoService::class.java,
                    context,
                    SERVICE_NAME,
                    errorObservable,
                    useWithIPC = true,
                    viewModelScope
                )
            } else {
                ipcServiceConnection
            }

            if (ipcServiceConnection?.isConnectedToService() == false) {
                ipcServiceConnection?.connectToService(context, DemoService::class.java, serviceChannel)
            }

            return ipcServiceReceiver?.flow as Flow<IOResult>
        } else {
            null
        }
    }

    private class Receiver(private val scope: CoroutineScope, private val logTag: String) : BroadcastReceiver() {
        private var context: Context? = null
        private val channel = Channel<IOResult>()
        val flow: Flow<IOResult> = channel.consumeAsFlow()

        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val imageResultState = intent.getIntExtra(DemoService.KEY_IMAGE_RESULT, ImageResult.UNKNOWN.state)
                when (ImageResult.getImageResult(imageResultState)) {
                    ImageResult.UNKNOWN -> {
                        scope.launch {
                            channel.send(IOResult.Unknown)
                        }
                    }

                    ImageResult.LOADING -> {
                        scope.launch {
                            channel.send(IOResult.Loading)
                        }
                    }

                    ImageResult.SUCCESS -> {
                        scope.launch {
                            val imageData = it.getByteArrayExtra(DemoService.KEY_IMAGE_DATA)
                            channel.send(IOResult.Success(imageData))
                        }
                    }

                    ImageResult.ERROR -> {
                        scope.launch {
                            val message = it.getStringExtra(DemoService.KEY_ERROR_MESSAGE)
                            channel.send(IOResult.Error(throwable = Throwable(message)))
                        }
                    }
                }
            }
        }

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        fun register(context: Context?) {
            this.context = context
            context?.let {
                try {
                    val filter = IntentFilter(Intent.ACTION_GET_CONTENT)

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        it.registerReceiver(this, filter, Context.RECEIVER_EXPORTED)
                    } else {
                        it.registerReceiver(this, filter)
                    }
                } catch (t: Throwable) {

                }
            }
        }

        fun releaseResources() {
            context?.unregisterReceiverQuietly(this, logTag)
            channel.closeQuietly(scope, logTag = logTag)
        }
    }

    override fun onCleared() {
        localServiceConnection?.disconnectFromService()
        localServiceReceiver?.releaseResources()
        super.onCleared()
    }
}
