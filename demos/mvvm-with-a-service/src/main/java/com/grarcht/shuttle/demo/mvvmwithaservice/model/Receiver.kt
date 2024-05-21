package com.grarcht.shuttle.demo.mvvmwithaservice.model

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.framework.CARGO_ID_KEY
import com.grarcht.shuttle.framework.NO_CARGO_ID
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.content.unregisterReceiverQuietly
import com.grarcht.shuttle.framework.coroutines.channel.closeQuietly
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import com.grarcht.shuttle.framework.visibility.error.ShuttleDefaultError
import com.grarcht.shuttle.framework.visibility.observation.ShuttleVisibilityObservable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import kotlin.system.exitProcess

private const val CONTEXT = "Cargo Receiver"
private const val LOG_TAG = "CargoBroadcastReceiver"
private const val TIMER_DELAY = 5000L
private const val UNABLE_TO_RECEIVE_CARGO = "Unable to receive the cargo."

/**
 * A receiver that receives and processes broadcasts from the [RemoteService].
 *
 * @param shuttle used to store and pickup oversized cargo from the warehouse
 * @param scope for emitting [IOResult]s via [Channel]s
 * @param handleNoResponseReceived true to handle the situation when the
 * app keeps showing the loading screen after a transaction too large exception
 * is thrown in the service process
 */
class Receiver(
    private val shuttle: Shuttle,
    private val scope: CoroutineScope,
    private val visibilityObservable: ShuttleVisibilityObservable,
    private val handleNoResponseReceived: Boolean = false
) : BroadcastReceiver() {
    private var context: Context? = null
    private val channel = Channel<IOResult>()
    private var responseReceived = false

    val flow: Flow<IOResult> = channel.consumeAsFlow()

    /**
     * Receives and processes broadcasts from the [RemoteService].
     *
     * @see [BroadcastReceiver.onReceive]
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val cargoId = intent.extras?.getString(CARGO_ID_KEY, NO_CARGO_ID) ?: NO_CARGO_ID

            val imageResultState = intent.getIntExtra(RemoteService.KEY_IMAGE_RESULT, ImageResult.UNKNOWN.state)
            when (ImageResult.getImageResult(imageResultState)) {
                ImageResult.UNKNOWN -> {
                    scope.launch {
                        channel.send(IOResult.Unknown)
                    }
                }

                ImageResult.LOADING -> {
                    if (handleNoResponseReceived) {
                        sendErrorForResponseNotReceived()
                    }

                    scope.launch {
                        channel.send(IOResult.Loading)
                    }
                }

                ImageResult.SUCCESS -> {
                    responseReceived = true

                    scope.launch {
                        shuttle.pickupCargo<ImageModel>(cargoId).consumeAsFlow().collectLatest {
                            when (it) {
                                is ShuttlePickupCargoResult.Error<*> -> {
                                    channel.send(IOResult.Error(throwable = Throwable(it.message)))
                                }
                                ShuttlePickupCargoResult.Loading -> {
                                    // Ignore.  The state should already be the loading state.
                                }
                                is ShuttlePickupCargoResult.Success<*> -> {
                                    val imageModel = it.data as ImageModel
                                    channel.send(IOResult.Success(imageModel))
                                }
                                ShuttlePickupCargoResult.NotPickingUpCargoYet -> {
                                    // Ignore
                                }
                            }
                        }
                    }
                }

                ImageResult.ERROR -> {
                    responseReceived = true

                    scope.launch {
                        val message = it.getStringExtra(RemoteService.KEY_ERROR_MESSAGE)
                        channel.send(IOResult.Error(throwable = Throwable(message)))
                    }
                }
            }
        }
    }

    /**
     * Registers the receiver.
     *
     * @param context used for registration
     */
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
                val message = "$UNABLE_TO_RECEIVE_CARGO ${t.message}"
                val error = ShuttleDefaultError.ObservedError(CONTEXT, message, t)
                visibilityObservable.observe(error)
            }
        }
    }

    /**
     * Unregisters the receiver, catching a logging associated errors.
     */
    fun unregisterReceiverQuietly() {
        context?.unregisterReceiverQuietly(this, LOG_TAG)
    }

    /**
     * Releases the resources.
     */
    fun releaseResources() {
        channel.closeQuietly(scope, logTag = LOG_TAG)
        context?.unregisterReceiverQuietly(this, LOG_TAG)
    }

    private fun sendErrorForResponseNotReceived() {
        // Sometimes, after the transaction too large exception is thrown on the emulator, the app crashes and sometimes only the remote process
        // stops responding. To improve the experience with the demo app when the remote service stops responding and the app keeps showing the
        // loading screen, if a response is not received after 5 seconds, the process is exited.
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                if (!responseReceived) {
                    scope.launch {
                        Log.e(LOG_TAG, "Unable to retrieve the image after the Transaction Too Large Exception was thrown in the remote process.")
                        exitProcess(0)
                    }
                }
                cancel()
                timer.cancel()
            }
        }, TIMER_DELAY)
    }
}
