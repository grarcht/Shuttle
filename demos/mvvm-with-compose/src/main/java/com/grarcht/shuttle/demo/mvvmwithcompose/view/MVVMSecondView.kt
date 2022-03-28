package com.grarcht.shuttle.demo.mvvmwithcompose.view

import android.content.Context
import android.os.Bundle
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.grarcht.shuttle.demo.core.image.BitmapDecoder
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.mvvmwithcompose.R
import com.grarcht.shuttle.demo.mvvmwithcompose.ui.rawPainterResource
import com.grarcht.shuttle.demo.mvvmwithcompose.viewmodel.SecondViewModel
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.model.ShuttleParcelCargo
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

private const val ANIMATION_TWEEN_MILLIS = 900
private val CIRCULAR_PROGRESS_SIZE = 50.dp
private const val ERROR_CONTENT_DESCRIPTION = "Failure loading the image."
private const val INITIAL_VALUE = 0f
private const val SUCCESS_CONTENT_DESCRIPTION = "Successfully loaded the image."
private const val TAG = "MVVMSecondView"
private const val TARGET_VALUE = 1.0f

class MVVMSecondView(
    private val context: Context,
    private val viewModel: SecondViewModel,
    private val shuttle: Shuttle,
    backgroundThreadDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val backgroundThreadScope = CoroutineScope(backgroundThreadDispatcher)
    private val bitmapDecoder = BitmapDecoder()
    private var deferredImageLoad: Deferred<Unit>? = null
    private var imageModel: ImageModel? = null
    private var storedCargoId: String? = null

    @Suppress("PreviewMustBeTopLevelFunction")
    @Composable
    fun SetViewContent(
        savedInstanceState: Bundle? = null,
        extras: Bundle? = null
    ) {
        extractArgsFrom(savedInstanceState, extras)

        Column {
            val cargoId = storedCargoId ?: ""
            var stateUpdate: IOResult by remember { mutableStateOf(IOResult.Loading) }

            when (stateUpdate) {
                IOResult.Unknown -> {
                    if (cargoId.isEmpty()) {
                        ShowErrorImage()
                        return
                    }
                }
                is IOResult.Error<*> -> {
                    ShowErrorImage()
                }
                IOResult.Loading -> {
                    ShowLoadingViews()
                }
                is IOResult.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val imageModel = (stateUpdate as IOResult.Success<ImageModel>).data
                    ShowSuccessImage(imageModel)
                }
            }

            LaunchedEffect(true) {
                getCargo(cargoId, stateUpdate = { stateUpdate = it })
            }
        }
    }

    private fun extractArgsFrom(savedInstanceState: Bundle?, extras: Bundle?): MVVMSecondView {
        if (null != savedInstanceState) {
            val cargo: ShuttleParcelCargo? = savedInstanceState.getParcelable(ImageMessageType.ImageData.value)
            storedCargoId = cargo?.cargoId
        } else if (null != extras) {
            val cargo: ShuttleParcelCargo? = extras.getParcelable(ImageMessageType.ImageData.value)
            storedCargoId = cargo?.cargoId
        }
        return this
    }

    private fun getCargo(cargoId: String, stateUpdate: (IOResult) -> Unit) {
        backgroundThreadScope.launch {
            viewModel
                .loadImage(shuttle, cargoId)
                .consumeAsFlow()
                .collectLatest {
                    when (it) {
                        is ShuttlePickupCargoResult.Loading -> {
                            stateUpdate.invoke(IOResult.Loading)
                        }
                        is ShuttlePickupCargoResult.Success<*> -> {
                            stateUpdate.invoke(IOResult.Success(it.data as ImageModel))
                            cancel()
                        }
                        is ShuttlePickupCargoResult.Error<*> -> {
                            stateUpdate.invoke(IOResult.Error(it.throwable as Throwable))
                            cancel()
                        }
                    }
                }
        }
    }

    fun getSavedInstanceState(shuttle: Shuttle, outState: Bundle): Bundle {
        return shuttle
            .bundleCargoWith(outState)
            .logTag(TAG)
            .transport(ImageMessageType.ImageData.value, imageModel)
            .create()
    }

    fun cleanUpViewResources() {
        deferredImageLoad?.cancel()
    }

    @Composable
    private fun ShowLoadingViews() {
        Box {
            // Background loading image
            Image(
                rawPainterResource(id = R.raw.loading),
                contentDescription = SUCCESS_CONTENT_DESCRIPTION,
                contentScale = ContentScale.FillBounds
            )

            // Circular progress loading indicator
            val progress by rememberInfiniteTransition().animateFloat(
                initialValue = INITIAL_VALUE,
                targetValue = TARGET_VALUE,
                animationSpec = infiniteRepeatable(animation = tween(ANIMATION_TWEEN_MILLIS))
            )
            val modifier = Modifier
                .size(CIRCULAR_PROGRESS_SIZE)
                .align(Alignment.Center)

            CircularProgressIndicator(progress, modifier)
        }
    }

    @Composable
    private fun ShowSuccessImage(imageModel: ImageModel) {
        if (this.imageModel == null) {
            this.imageModel = imageModel
            val bitmap = bitmapDecoder.decodeBitmap(imageModel.imageData)
            bitmap?.let {
                Box {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = SUCCESS_CONTENT_DESCRIPTION,
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
        }
    }

    @Composable
    private fun ShowErrorImage() {
        Image(
            painter = rawPainterResource(id = R.raw.broken_soccer_ball),
            contentDescription = ERROR_CONTENT_DESCRIPTION
        )

        val errorMessage = context.resources.getString(R.string.unable_to_get_image)
        Text(text = errorMessage)
    }
}
