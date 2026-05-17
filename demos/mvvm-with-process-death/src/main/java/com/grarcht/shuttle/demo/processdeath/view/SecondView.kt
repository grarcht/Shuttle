package com.grarcht.shuttle.demo.processdeath.view

import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavRiskyButtonColor
import com.grarcht.shuttle.demo.core.compose.ui.DemoSecondScreenLayout
import com.grarcht.shuttle.demo.core.compose.ui.rawPainterResource
import com.grarcht.shuttle.demo.core.image.BitmapDecoder
import com.grarcht.shuttle.demo.core.image.IMAGE_CARGO_ID
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.os.getParcelableWith
import com.grarcht.shuttle.demo.processdeath.R
import com.grarcht.shuttle.demo.processdeath.viewmodel.SecondImageState
import com.grarcht.shuttle.demo.processdeath.viewmodel.SecondViewModel
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.model.ShuttleParcelCargo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val ERROR_CONTENT_DESCRIPTION = "Failure loading the image."
private const val LOG_TAG = "SecondView"
private val ERROR_IMAGE_ID = com.grarcht.shuttle.demo.core.R.raw.breakdown
private val PLACEHOLDER_COLOR = Color(0xFFD1C7BD)
private val KILL_BUTTON_COLOR = DemoNavRiskyButtonColor
private val KILL_BUTTON_SHAPE = RoundedCornerShape(50)
private val KILL_BUTTON_TEXT_STYLE = TextStyle(
    color = Color(0xFFEFE9E1),
    fontSize = 15.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.3.sp
)
private val PADDING = 16.dp

class SecondView(
    private val context: Context,
    private val viewModel: SecondViewModel,
    private val shuttle: Shuttle
) {
    private var storedCargoId: String? = null
    private var useMemoryCache = false

    @Composable
    fun SetViewContent(
        savedInstanceState: Bundle? = null,
        extras: Bundle? = null,
        onKillAppProcess: () -> Unit = {}
    ) {
        extractArgsFrom(savedInstanceState, extras)
        ObserveImageLoading()
        ImageContent(onKillAppProcess)
    }

    @Composable
    private fun ObserveImageLoading() {
        LaunchedEffect(useMemoryCache, storedCargoId) {
            if (useMemoryCache) {
                viewModel.loadFromMemoryCache()
            } else {
                viewModel.loadFromShuttle(shuttle, storedCargoId ?: "")
            }
        }
    }

    @Composable
    private fun ImageContent(onKillAppProcess: () -> Unit) {
        val imageState by viewModel.imageState.collectAsState()
        Box(modifier = Modifier.fillMaxSize().background(PLACEHOLDER_COLOR)) {
            when (val state = imageState) {
                is SecondImageState.Loading -> ShowLoadingPlaceholder()
                is SecondImageState.Success -> ShowSuccessContent(state.imageModel, onKillAppProcess)
                is SecondImageState.LostToProcessDeath -> ShowProcessDeathError()
                is SecondImageState.Error -> ShowGenericError(state.message)
            }
        }
    }

    fun getSavedInstanceState(outState: Bundle): Bundle {
        val imageModel = (viewModel.imageState.value as? SecondImageState.Success)?.imageModel
        return shuttle
            .bundleCargoWith(outState)
            .logTag(LOG_TAG)
            .transport(IMAGE_CARGO_ID, imageModel)
            .create()
    }

    @Suppress("UnusedParameter")
    private fun extractArgsFrom(savedInstanceState: Bundle?, extras: Bundle?) {
        useMemoryCache = extras?.getBoolean(SecondActivity.EXTRA_USE_MEMORY_CACHE, false) ?: false
        if (useMemoryCache.not()) {
            val cargo: ShuttleParcelCargo? = extras?.getParcelableWith(
                IMAGE_CARGO_ID,
                ShuttleParcelCargo::class.java
            )
            storedCargoId = cargo?.cargoId
        }
    }

    @Composable
    private fun ShowLoadingPlaceholder() {
        Box(
            modifier = Modifier.fillMaxSize().background(PLACEHOLDER_COLOR),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    @Composable
    private fun ShowSuccessContent(imageModel: ImageModel, onKillProcess: () -> Unit) {
        val bitmap by produceState<ImageBitmap?>(null, imageModel) {
            value = withContext(Dispatchers.IO) {
                BitmapDecoder.decodeBitmap(imageModel.imageData)?.asImageBitmap()
            }
        }
        DemoSecondScreenLayout(bitmap = bitmap, fileSizeBytes = imageModel.imageData.size.toLong()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = PADDING)
                    .clip(KILL_BUTTON_SHAPE)
                    .background(KILL_BUTTON_COLOR)
                    .clickable(onClick = onKillProcess)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicText(
                    text = context.getString(R.string.kill_process),
                    style = KILL_BUTTON_TEXT_STYLE
                )
            }
        }
    }

    @Composable
    private fun ShowProcessDeathError() {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rawPainterResource(id = ERROR_IMAGE_ID),
                contentDescription = ERROR_CONTENT_DESCRIPTION,
                modifier = Modifier.fillMaxSize()
            )
            BasicText(
                text = context.getString(R.string.data_lost_to_process_death),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(PADDING)
            )
        }
    }

    @Composable
    private fun ShowGenericError(message: String) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rawPainterResource(id = ERROR_IMAGE_ID),
                contentDescription = ERROR_CONTENT_DESCRIPTION,
                modifier = Modifier.fillMaxSize()
            )
            BasicText(
                text = message,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(PADDING)
            )
        }
    }
}
