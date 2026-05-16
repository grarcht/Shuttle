package com.grarcht.shuttle.demo.mviwithcompose.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import com.grarcht.shuttle.demo.core.compose.ui.DemoSecondScreenLayout
import com.grarcht.shuttle.demo.core.compose.ui.rawPainterResource
import com.grarcht.shuttle.demo.core.image.BitmapDecoder
import com.grarcht.shuttle.demo.core.image.IMAGE_CARGO_ID
import com.grarcht.shuttle.demo.core.os.getParcelableWith
import com.grarcht.shuttle.demo.mviwithcompose.R
import com.grarcht.shuttle.demo.mviwithcompose.intent.CargoPickupIntent
import com.grarcht.shuttle.demo.mviwithcompose.viewmodel.SecondViewModel
import com.grarcht.shuttle.framework.model.ShuttleParcelCargo

private const val ERROR_CONTENT_DESCRIPTION = "Failure loading the image."
private const val NO_CARGO_ID = ""
private const val TAG = "MVISecondView"
private const val WARN_EMPTY_CARGO_ID = "Cargo ID is empty; skipping cargo pickup."
private val ERROR_IMAGE_ID = com.grarcht.shuttle.demo.core.R.raw.broken_soccer_ball
private val PLACEHOLDER_COLOR = Color(0xFFD1C7BD)

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

    fun extractCargoId(savedInstanceState: Bundle?, arguments: Bundle?): String {
        val bundle: Bundle? = savedInstanceState ?: arguments
        val cargo: ShuttleParcelCargo? = bundle?.getParcelableWith(
            IMAGE_CARGO_ID,
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

        if (uiState.error != null || cargoId.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().background(PLACEHOLDER_COLOR).systemBarsPadding()) {
                ShowErrorImage()
            }
        } else {
            val imageModel = uiState.imageModel
            val bitmap = imageModel?.let { BitmapDecoder.decodeBitmap(it.imageData)?.asImageBitmap() }
            DemoSecondScreenLayout(bitmap = bitmap, fileSizeBytes = imageModel?.imageData?.size?.toLong())
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
