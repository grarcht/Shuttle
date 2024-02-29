package com.grarcht.shuttle.demo.mvvm.model

import android.content.Context
import android.content.Intent
import android.os.Message
import androidx.annotation.RawRes
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.core.io.RawResourceGateway
import com.grarcht.shuttle.demo.mvvm.error.DemoError
import com.grarcht.shuttle.framework.CARGO_ID_KEY
import com.grarcht.shuttle.framework.NO_CARGO_ID
import com.grarcht.shuttle.framework.app.ShuttleService
import com.grarcht.shuttle.framework.app.ShuttleServiceConfig
import com.grarcht.shuttle.framework.coroutines.scope.cancelScopeQuietly
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// private to avoid overhead with extra objects created when component objects are created
private const val ERROR_UNABLE_TO_GET_IMAGE = "Unable to get image."
private const val SERVICE_NAME = "DemoService"

class DemoService(
    config: ShuttleServiceConfig
) : ShuttleService(config) {
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(job + Dispatchers.IO)

    override fun getServiceName(): String = SERVICE_NAME

    override fun onDestroy() {
        cancelScopeQuietly(scope = coroutineScope, context = SERVICE_NAME, errorObservable = config.errorObservable)
        super.onDestroy()
    }

    fun getImageBytesWithoutShuttle(@RawRes imageId: Int) {
        val flow: Flow<IOResult> = RawResourceGateway
            .with(resources)
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
                        intent.putExtra(KEY_IMAGE_RESULT, ImageResult.LOADING)
                        sendBroadcast(intent)
                    }

                    is IOResult.Success<*> -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.putExtra(KEY_IMAGE_RESULT, ImageResult.SUCCESS)
                        intent.putExtra(KEY_IMAGE_DATA, ioResult.data as ByteArray)
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

    override fun onReceiveMessage(context: Context, messageWhat: Int, msg: Message) {
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
                        val error = DemoError(
                            SERVICE_NAME,
                            cargoId,
                            errorMessage = ioResult.throwable.message ?: ERROR_UNABLE_TO_GET_IMAGE,
                            error = ioResult.throwable
                        )
                        config.errorObservable.onError(error)
                    }

                    IOResult.Loading -> {
                        // Ignore
                    }

                    is IOResult.Success<*> -> {
                        val byteArray = ioResult.data as ByteArray
                        val imageModel = ImageModel(ImageMessageType.ImageData.value, byteArray)
                        transportCargoWithShuttle(cargoId, imageModel)
                    }

                    IOResult.Unknown -> {
                        // Ignore
                    }
                }
            }
        }
    }

    companion object {
        // Only public consts are put here.  Private ones are made file private to avoid overhead with creating
        // extra consts.
        const val ACTION_GET_IMAGE = 0
        const val KEY_IMAGE_ID = "key_image_id"
        const val KEY_IMAGE_RESULT = "key_image_result"
        const val KEY_IMAGE_DATA = "key_image_data"
        const val KEY_ERROR_MESSAGE = "key_error_message"
    }
}