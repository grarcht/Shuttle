package com.grarcht.shuttle.demo.mviwithcompose.view

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import com.grarcht.shuttle.demo.core.image.BitmapDecoder
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.os.getParcelableWith
import com.grarcht.shuttle.demo.mviwithcompose.R
import com.grarcht.shuttle.demo.mviwithcompose.intent.CargoPickupIntent
import com.grarcht.shuttle.demo.mviwithcompose.ui.rawPainterResource
import com.grarcht.shuttle.demo.mviwithcompose.viewmodel.SecondViewModel
import com.grarcht.shuttle.framework.model.ShuttleParcelCargo

private const val ANIMATION_TWEEN_MILLIS = 900
private const val ANIMATE_FLOAT_LABEL = ""
private const val ERROR_CONTENT_DESCRIPTION = "Failure loading the image."
private const val INFINITE_TRANSITION_LABEL = "InfiniteTransition"
private const val INITIAL_VALUE = 0f
private const val NO_CARGO_ID = ""
private const val SUCCESS_CONTENT_DESCRIPTION = "Successfully loaded the image."
private const val TAG = "MVISecondView"
private const val TARGET_VALUE = 1.0f
private const val WARN_EMPTY_CARGO_ID = "Cargo ID is empty; skipping cargo pickup."
private val CIRCULAR_PROGRESS_SIZE = 50.dp
private val ERROR_IMAGE_ID = com.grarcht.shuttle.demo.core.R.raw.broken_soccer_ball
private val LOADING_IMAGE_ID = com.grarcht.shuttle.demo.core.R.raw.loading

/**
 * The Composable view for the second screen in the MVI with Compose demo. It retrieves
 * cargo from the Shuttle warehouse using a cargo ID passed from the first screen and
 * displays the resulting image, a loading indicator, or an error state accordingly.
 *
 * @param context the context used to access resources and string values.
 * @param viewModel the view model that processes intents and exposes the UI state.
 */
class MVISecondView(
    private val context: Context,
    private val viewModel: SecondViewModel
) {
    private val bitmapDecoder = BitmapDecoder()

    fun extractCargoId(savedInstanceState: Bundle?, arguments: Bundle?): String {
        val bundle: Bundle? = savedInstanceState ?: arguments
        val cargo: ShuttleParcelCargo? = bundle?.getParcelableWith(
            ImageMessageType.ImageData.value,
            ShuttleParcelCargo::class.java
        )
        return cargo?.cargoId ?: NO_CARGO_ID
    }

    @Composable
    fun SetViewContent(cargoId: String) {
        val uiState by viewModel.uiState.collectAsState()

        LaunchedEffect(cargoId) {
            if (cargoId.isNotEmpty()) {
                viewModel.processIntent(CargoPickupIntent.LoadCargo(cargoId))
            } else {
                Log.w(TAG, WARN_EMPTY_CARGO_ID)
            }
        }

        Column(modifier = Modifier.systemBarsPadding()) {
            when {
                uiState.error != null || cargoId.isEmpty() -> ShowErrorImage()
                uiState.isLoading -> ShowLoadingViews()
                uiState.imageModel != null -> ShowSuccessImage(uiState.imageModel!!)
            }
        }
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
