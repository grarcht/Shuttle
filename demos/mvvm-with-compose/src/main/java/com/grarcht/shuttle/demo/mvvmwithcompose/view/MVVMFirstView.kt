package com.grarcht.shuttle.demo.mvvmwithcompose.view

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.mvvmwithcompose.R
import com.grarcht.shuttle.demo.mvvmwithcompose.viewmodel.FirstViewModel
import com.grarcht.shuttle.framework.Shuttle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import java.io.Serializable

private val SMALL_PADDING = 8.dp
private val LARGE_PADDING = 16.dp
private val TOP_PADDING = 64.dp
private val BUTTON_CONTENT_PADDING = PaddingValues(SMALL_PADDING)
private const val TAG = "MVVMFirstView"
private const val UNABLE_TO_GET_IMAGE_BYTES_ERROR_MESSAGE = "Unable to get the image byte array."
private const val UNABLE_TO_GET_IMAGE_MODEL_ERROR_MESSAGE = "Caught when getting the image model."

class MVVMFirstView(
    private val context: Context,
    private val viewModel: FirstViewModel,
    private val shuttle: Shuttle
) {
    private var imageGatewayDisposableHandle: DisposableHandle? = null
    private var imageModel: ImageModel? = null

    @Composable
    fun SetViewContent() {
        var buttonsEnabled by remember { mutableStateOf(false) }
        val title = context.resources.getString(R.string.mvvm_first_view_title)
        val largePaddingModifier = Modifier
            .padding(LARGE_PADDING)
            .fillMaxHeight()
            .fillMaxWidth()

        Text(
            text = title,
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center,
            modifier = largePaddingModifier
        )

        Column(
            modifier = largePaddingModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = navigateWithShuttle(),
                enabled = buttonsEnabled,
                contentPadding = BUTTON_CONTENT_PADDING,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SMALL_PADDING)
            ) {
                Text(
                    context.resources.getString(R.string.navigate_using_shuttle),
                    style = MaterialTheme.typography.h6
                )
            }

            Text(
                text = context.getString(R.string.the_app_will_not_crash_using_shuttle),
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center,
                color = Color(context.getColor(android.R.color.holo_green_dark)),
                fontFamily = FontFamily.SansSerif
            )

            Button(
                onClick = navigateNormally(),
                enabled = buttonsEnabled,
                contentPadding = BUTTON_CONTENT_PADDING,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = SMALL_PADDING, top = TOP_PADDING, end = SMALL_PADDING, bottom = SMALL_PADDING)
            ) {
                Text(
                    context.resources.getString(R.string.navigate_normally),
                    style = MaterialTheme.typography.h6
                )
            }

            Text(
                text = context.getString(R.string.the_app_will_crash_without_using_shuttle),
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center,
                color = Color(context.getColor(android.R.color.holo_red_dark)),
                fontFamily = FontFamily.SansSerif
            )
        }

        LaunchedEffect(true) {
            getImageData(stateUpdate = { buttonsEnabled = it is IOResult.Success<*> })
        }
    }


    fun cleanUpViewResources() {
        imageGatewayDisposableHandle?.dispose()
        // Ensure all persisted cargo data is removed.
        shuttle.cleanShuttleFromAllDeliveries()
    }

    private fun getImageData(stateUpdate: (IOResult) -> Unit): MVVMFirstView {
        imageGatewayDisposableHandle = MainScope().async {
            viewModel.getImage(context.resources, R.raw.tower)
                .collectLatest {
                    when (it) {
                        is IOResult.Unknown -> {
                            stateUpdate.invoke(IOResult.Unknown)
                        }
                        is IOResult.Loading -> {
                            stateUpdate.invoke(IOResult.Loading)
                        }
                        is IOResult.Success<*> -> {
                            val byteArray = it.data as ByteArray
                            imageModel = ImageModel(ImageMessageType.ImageData.value, byteArray)
                            stateUpdate.invoke(IOResult.Success(true))
                            cancel()
                        }
                        is IOResult.Error<*> -> {
                            val errorMessage = it.throwable.message ?: UNABLE_TO_GET_IMAGE_BYTES_ERROR_MESSAGE
                            Log.e(TAG, errorMessage, it.throwable)
                            stateUpdate.invoke(IOResult.Error(it.throwable))
                            cancel()
                        }
                    }
                }
        }.invokeOnCompletion {
            it?.let {
                if (it !is CancellationException) {
                    Log.w(TAG, UNABLE_TO_GET_IMAGE_MODEL_ERROR_MESSAGE, it)
                }
            }
        }
        return this
    }

    private fun navigateWithShuttle(): () -> Unit {
        return {
            val cargoId = ImageMessageType.ImageData.value
            val startClass = MVVMFirstViewActivity::class.java
            val destinationClass = MVVMSecondViewActivity::class.java

            shuttle.intentCargoWith(context, destinationClass)
                .logTag(TAG)
                .transport(cargoId, imageModel)
                .cleanShuttleOnReturnTo(startClass, destinationClass, cargoId)
                .deliver(context)
        }
    }

    private fun navigateNormally(): () -> Unit {
        return {
            val cargoId = ImageMessageType.ImageData.value
            val destinationClass = MVVMSecondViewActivity::class.java
            val intent = Intent(context, destinationClass.javaClass)
            intent.putExtra(cargoId, imageModel as Serializable)
            context.startActivity(intent)
        }
    }
}
