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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.grarcht.shuttle.demo.core.compose.ui.rawPainterResource
import com.grarcht.shuttle.demo.core.image.BitmapDecoder
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.os.getParcelableWith
import com.grarcht.shuttle.demo.mvvmwithcompose.R
import com.grarcht.shuttle.demo.mvvmwithcompose.viewmodel.SecondViewModel
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.model.ShuttleParcelCargo
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult

private const val ANIMATION_TWEEN_MILLIS = 900
private const val ANIMATE_FLOAT_LABEL = ""
private const val ERROR_CONTENT_DESCRIPTION = "Failure loading the image."
private const val INFINITE_TRANSITION_LABEL = "InfiniteTransition"
private const val INITIAL_VALUE = 0f
private const val SUCCESS_CONTENT_DESCRIPTION = "Successfully loaded the image."
private const val TAG = "MVVMSecondView"
private const val TARGET_VALUE = 1.0f
private val CIRCULAR_PROGRESS_SIZE = 50.dp
private val ERROR_IMAGE_ID = com.grarcht.shuttle.demo.core.R.raw.broken_soccer_ball
private val LOADING_IMAGE_ID = com.grarcht.shuttle.demo.core.R.raw.loading

class MVVMSecondView(
    private val context: Context,
    private val viewModel: SecondViewModel,
    private val shuttle: Shuttle
) {
    private val bitmapDecoder = BitmapDecoder()
    private var storedCargoId: String? = null

    @Composable
    fun SetViewContent(
        savedInstanceState: Bundle? = null,
        extras: Bundle? = null
    ) {
        extractArgsFrom(savedInstanceState, extras)

        val cargoId = storedCargoId ?: ""
        val pickupState by viewModel.pickupCargoState.collectAsState()

        LaunchedEffect(cargoId) {
            if (cargoId.isNotEmpty()) {
                viewModel.loadImage(shuttle, cargoId)
            }
        }

        Column(modifier = Modifier.systemBarsPadding()) {
            when {
                cargoId.isEmpty() || pickupState is ShuttlePickupCargoResult.Error<*> -> ShowErrorImage()
                pickupState is ShuttlePickupCargoResult.Success<*> -> {
                    val imageModel = (pickupState as ShuttlePickupCargoResult.Success<*>).data as? ImageModel
                    if (imageModel != null) ShowSuccessImage(imageModel) else ShowErrorImage()
                }
                else -> ShowLoadingViews()
            }
        }
    }

    private fun extractArgsFrom(savedInstanceState: Bundle?, arguments: Bundle?) {
        val bundle: Bundle? = savedInstanceState ?: arguments
        bundle?.let {
            val cargo: ShuttleParcelCargo? =
                it.getParcelableWith(ImageMessageType.ImageData.value, ShuttleParcelCargo::class.java)
            storedCargoId = cargo?.cargoId
        }
    }

    fun getSavedInstanceState(shuttle: Shuttle, outState: Bundle): Bundle {
        val imageModel = (viewModel.pickupCargoState.value as? ShuttlePickupCargoResult.Success<*>)
            ?.data as? ImageModel
        return shuttle
            .bundleCargoWith(outState)
            .logTag(TAG)
            .transport(ImageMessageType.ImageData.value, imageModel)
            .create()
    }

    fun cleanUpViewResources() {
        // State is managed by the ViewModel; nothing to release here.
    }

    @Composable
    private fun ShowLoadingViews() {
        Box {
            Image(
                rawPainterResource(id = LOADING_IMAGE_ID),
                contentDescription = SUCCESS_CONTENT_DESCRIPTION,
                contentScale = ContentScale.FillBounds
            )
            val progress by rememberInfiniteTransition(label = INFINITE_TRANSITION_LABEL).animateFloat(
                initialValue = INITIAL_VALUE,
                targetValue = TARGET_VALUE,
                animationSpec = infiniteRepeatable(animation = tween(ANIMATION_TWEEN_MILLIS)),
                label = ANIMATE_FLOAT_LABEL
            )
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(CIRCULAR_PROGRESS_SIZE).align(Alignment.Center)
            )
        }
    }

    @Composable
    private fun ShowSuccessImage(imageModel: ImageModel) {
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

    @Composable
    private fun ShowErrorImage() {
        Image(
            painter = rawPainterResource(id = ERROR_IMAGE_ID),
            contentDescription = ERROR_CONTENT_DESCRIPTION
        )
        Text(text = context.resources.getString(R.string.unable_to_get_image))
    }
}
