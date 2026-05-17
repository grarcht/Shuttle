package com.grarcht.shuttle.demo.mvvmcomposewithnavigation.view

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
import androidx.compose.ui.res.stringResource
import com.grarcht.shuttle.demo.core.compose.ui.DemoSecondScreenLayout
import com.grarcht.shuttle.demo.core.compose.ui.rawPainterResource
import com.grarcht.shuttle.demo.core.image.BitmapDecoder
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.mvvmcomposewithnavigation.R
import com.grarcht.shuttle.demo.mvvmcomposewithnavigation.viewmodel.SecondViewModel
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult

private val ERROR_IMAGE_ID = com.grarcht.shuttle.demo.core.R.raw.broken_soccer_ball
private val PLACEHOLDER_COLOR = Color(0xFFD1C7BD)

@Composable
fun SecondScreen(
    viewModel: SecondViewModel,
    shuttle: Shuttle,
    cargoId: String
) {
    val pickupState by viewModel.pickupCargoState.collectAsState()
    val imageModel = (pickupState as? ShuttlePickupCargoResult.Success<*>)?.data as? ImageModel

    LaunchedEffect(cargoId) {
        if (cargoId.isNotEmpty()) {
            viewModel.loadImage(shuttle, cargoId)
        }
    }

    if (cargoId.isEmpty() || pickupState is ShuttlePickupCargoResult.Error<*>) {
        Column(modifier = Modifier.fillMaxSize().background(PLACEHOLDER_COLOR).systemBarsPadding()) {
            ShowErrorImage()
        }
    } else {
        val bitmap = imageModel?.let { BitmapDecoder.decodeBitmap(it.imageData)?.asImageBitmap() }
        DemoSecondScreenLayout(bitmap = bitmap, fileSizeBytes = imageModel?.imageData?.size?.toLong())
    }
}

@Composable
private fun ShowErrorImage() {
    Image(
        painter = rawPainterResource(id = ERROR_IMAGE_ID),
        contentDescription = stringResource(R.string.failure_loading_image)
    )
    Text(text = stringResource(R.string.unable_to_get_image))
}
