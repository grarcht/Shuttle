@file:Suppress("LateinitVarOverridesLateinitVar")

package com.grarcht.shuttle.demo.mvvmwithaservice.model

import android.content.Intent
import android.os.Message
import android.util.Log
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.core.io.RawResourceGateway
import com.grarcht.shuttle.framework.CARGO_ID_KEY
import com.grarcht.shuttle.framework.NO_CARGO_ID
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.app.ShuttleService
import com.grarcht.shuttle.framework.app.ShuttleServiceConfig
import com.grarcht.shuttle.framework.coroutines.scope.cancelScopeQuietly
import com.grarcht.shuttle.framework.visibility.error.ShuttleServiceError
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Named

// private to avoid overhead with extra objects created when component objects are created
private const val ERROR_UNABLE_TO_GET_IMAGE = "Unable to get image."
private const val UNSUPPORTED_MESSAGING_ACTION = "Unsupported messaging action. Action:"

/**
 * Demonstrates using Shuttle with a remote service to transport the large cargo.
 */
@AndroidEntryPoint
class RemoteService : ShuttleService() {
    @Inject
    @Named("RemoteServiceConfig")
    override lateinit var config: ShuttleServiceConfig

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(job + Dispatchers.IO)

    /**
     * Provides a human-readable string for logging. For obfuscated apps, typical usage involves reflection with
     * the class name. When reflection is used to get the name of the class in obfuscated apps, the string becomes
     * garbled. When looking at log tags in log management systems, the tag will not be readable. The recommended
     * approach is to use the name of your service in a string.  For instance, if your service is named
     * MyService, then return "MyService".
     */
    override fun getServiceName(): String = SERVICE_NAME

    /**
     * Releases resources.
     */
    override fun onDestroy() {
        cancelScopeQuietly(scope = coroutineScope, context = SERVICE_NAME, errorObservable = config.errorObservable)
        super.onDestroy()
    }

    /**
     * Provides the cargo intent, used for transporting cargo with [Shuttle]. What is provided below combines the
     * default with extra information for the receiver in this demo.
     */
    override fun <D : Serializable> getCargoIntentForTransport(
        cargoId: String,
        cargo: D?
    ): Intent {
        val cargoIntent = super.getCargoIntentForTransport(cargoId, cargo)
        cargoIntent.putExtra(KEY_IMAGE_RESULT, ImageResult.SUCCESS.state)
        cargoIntent.putExtra(CARGO_ID_KEY, cargoId)
        return cargoIntent
    }

    /**
     * Handles IPC messaging.
     *
     * Since this service supports local binding and messenger binding for IPC, and this function is for the
     * latter, then this function is open and not abstract.
     *
     *  @param messageWhat see [Message.what]
     *  @param msg see [Message]
     */
    override fun onReceiveMessage(messageWhat: Int, msg: Message) {
        Log.v(SERVICE_NAME, "onReceiveMessage")

        when (MessagingAction.getActionWith(messageWhat)) {
            MessagingAction.TRANSPORT_IMAGE_CARGO_WITH_SHUTTLE -> {
                Log.v(SERVICE_NAME, "onReceiveMessage -> TRANSPORT_IMAGE_CARGO_WITH_SHUTTLE")
                transportImageBytesUsingShuttle(msg)
            }

            MessagingAction.TRANSPORT_IMAGE_CARGO_WITHOUT_SHUTTLE -> {
                Log.v(SERVICE_NAME, "onReceiveMessage -> TRANSPORT_IMAGE_CARGO_WITHOUT_SHUTTLE")
                transportImageBytesWithoutShuttle(msg)
            }

            MessagingAction.UNKNOWN_DO_NOT_USE -> {
                Log.w(SERVICE_NAME, "$UNSUPPORTED_MESSAGING_ACTION $messageWhat")
            }
        }
    }

    /**
     * Transports the image bytes using [Shuttle] prior to calling sendBroadcast.
     */
    private fun transportImageBytesUsingShuttle(msg: Message) {
        val imageId: Int = msg.data.getInt(KEY_IMAGE_ID)

        val flow: Flow<IOResult> = RawResourceGateway
            .with(resources)
            .bytesFromRawResource(imageId)
            .create()

        val cargoId = msg.data.getString(CARGO_ID_KEY, NO_CARGO_ID)

        coroutineScope.launch {
            flow.collect { ioResult: IOResult ->
                when (ioResult) {
                    is IOResult.Error<*> -> {
                        val error = ShuttleServiceError.GeneralError(
                            SERVICE_NAME,
                            cargoId,
                            errorMessage = ioResult.throwable.message ?: ERROR_UNABLE_TO_GET_IMAGE,
                            error = ioResult.throwable
                        )
                        config.errorObservable.observe(error)

                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.putExtra(KEY_IMAGE_RESULT, ImageResult.ERROR.state)
                        intent.putExtra(KEY_ERROR_MESSAGE, ioResult.message)
                        sendBroadcast(intent)
                    }

                    IOResult.Loading -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.putExtra(KEY_IMAGE_RESULT, ImageResult.LOADING.state)
                        intent.putExtra(CARGO_ID_KEY, NO_CARGO_ID)
                        sendBroadcast(intent)
                    }

                    is IOResult.Success<*> -> {
                        Log.v(SERVICE_NAME, "sending broadcast with image WITH shuttle -> cargoId: $cargoId")
                        // ========================================================
                        //           No Transaction Too Large Exception
                        // ========================================================
                        // Sending this broadcast will NOT crash this remote
                        // service process as what would typically occur. The cargo
                        // is too large for normal transport. Since the cargo is
                        // transported with Shuttle, the uncatchable Transaction
                        // Too Large Exception will be avoided and the app will not
                        // crash from that exception.
                        //
                        // To show how the exception will be thrown, try creating
                        // an intent and passing the image through sendBroadcast.
                        //
                        // The DemoRemoteService shows how not using Shuttle to
                        // transport the cargo and using typical sendBroadcast
                        // implementations will crash the app.
                        // =======================================================
                        val byteArray = ioResult.data as ByteArray
                        val imageModel = ImageModel(ImageMessageType.ImageData.value, byteArray)
                        transportCargoWithShuttle(cargoId, imageModel)
                    }

                    IOResult.Unknown -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.putExtra(KEY_IMAGE_RESULT, ImageResult.UNKNOWN)
                        sendBroadcast(intent)
                    }
                }
            }
        }
    }

    /**
     * Transports the image cargo and sends it without using Shuttle for cargo
     * transport prior to calling [sendBroadcast].
     */
    private fun transportImageBytesWithoutShuttle(msg: Message) {
        val imageId: Int = msg.data.getInt(KEY_IMAGE_ID)
        val cargoId = msg.data.getString(CARGO_ID_KEY, NO_CARGO_ID)

        val flow: Flow<IOResult> = RawResourceGateway.with(resources)
            .bytesFromRawResource(imageId)
            .create()

        coroutineScope.launch {
            flow.collect { ioResult: IOResult ->
                when (ioResult) {
                    is IOResult.Error<*> -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.putExtra(KEY_IMAGE_RESULT, ImageResult.ERROR)
                        intent.putExtra(KEY_ERROR_MESSAGE, ioResult.message)
                        sendBroadcast(intent)
                    }

                    IOResult.Loading -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.putExtra(KEY_IMAGE_RESULT, ImageResult.LOADING.state)
                        intent.putExtra(CARGO_ID_KEY, NO_CARGO_ID)
                        sendBroadcast(intent)
                    }

                    is IOResult.Success<*> -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.putExtra(KEY_IMAGE_RESULT, ImageResult.SUCCESS)

                        val imageData = ioResult.data as ByteArray
                        val imageModel = ImageModel(cargoId, imageData)
                        intent.putExtra(KEY_IMAGE_DATA, imageModel)

                        Log.v(SERVICE_NAME, "sending broadcast with image WITHOUT shuttle -> cargoId: $cargoId")

                        // =======================================================
                        //             Transaction Too Large Exception
                        // =======================================================
                        // Sending this broadcast will crash the app as expected.
                        // The cargo is too large and the uncatchable Transaction
                        // Too Large Exception will be thrown.
                        //
                        // The DemoRemoteService shows how to avoid this exception
                        // by using Shuttle to transport the cargo.
                        // =======================================================
                        sendBroadcast(intent)
                    }

                    IOResult.Unknown -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.putExtra(KEY_IMAGE_RESULT, ImageResult.UNKNOWN)
                        sendBroadcast(intent)
                    }
                }
            }
        }
    }

    companion object {
        // Only public consts are put here.  Private ones are made file private to avoid overhead with creating
        // extra consts.
        const val KEY_IMAGE_DATA = "key_image_data"
        const val KEY_IMAGE_ID = "key_image_id"
        const val KEY_IMAGE_RESULT = "key_image_result"
        const val KEY_ERROR_MESSAGE = "key_error_message"
        const val SERVICE_NAME = "DemoRemoteService"
    }
}
