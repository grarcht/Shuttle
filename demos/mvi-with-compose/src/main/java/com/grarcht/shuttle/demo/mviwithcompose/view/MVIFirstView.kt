package com.grarcht.shuttle.demo.mviwithcompose.view

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grarcht.shuttle.demo.core.image.ImageMessageType
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.mviwithcompose.R
import com.grarcht.shuttle.demo.mviwithcompose.intent.CargoTransportIntent
import com.grarcht.shuttle.demo.mviwithcompose.navigation.NavigationEvent
import com.grarcht.shuttle.demo.mviwithcompose.viewmodel.FirstViewModel
import com.grarcht.shuttle.framework.Shuttle
import java.io.Serializable

private val SMALL_PADDING = 8.dp
private val LARGE_PADDING = 16.dp
private val TOP_PADDING = 64.dp
private val BUTTON_CONTENT_PADDING = PaddingValues(SMALL_PADDING)
private const val TAG = "MVIFirstView"

/**
 * The Composable view for the first screen in the MVI with Compose demo. It displays
 * navigation buttons that allow the user to transport image cargo to the second screen
 * either safely via Shuttle or directly via Intent to demonstrate the crash scenario.
 *
 * @param context the context used to access resources and start activities.
 * @param viewModel the view model that processes intents and exposes the UI state.
 * @param shuttle the Shuttle instance used to execute cargo transport during navigation.
 */
class MVIFirstView(
    private val context: Context,
    private val viewModel: FirstViewModel,
    private val shuttle: Shuttle
) {
    @Composable
    fun SetViewContent() {
        val uiState by viewModel.uiState.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.processIntent(
                CargoTransportIntent.LoadImage(
                    resources = context.resources,
                    imageId = com.grarcht.shuttle.demo.core.R.raw.tower
                )
            )
        }

        LaunchedEffect(Unit) {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is NavigationEvent.NavigateWithShuttle -> executeShuttleNavigation(event.imageModel)
                    is NavigationEvent.NavigateNormally -> executeNormalNavigation(event.imageModel)
                }
            }
        }

        Box(modifier = Modifier.systemBarsPadding()) {
            TitleText()
            NavigationButtonsColumn(buttonsEnabled = uiState.buttonsEnabled, imageModel = uiState.imageModel)
        }
    }

    @Composable
    private fun TitleText() {
        Text(
            text = context.resources.getString(R.string.mvi_first_view_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(LARGE_PADDING).fillMaxHeight().fillMaxWidth()
        )
    }

    @Composable
    private fun NavigationButtonsColumn(
        buttonsEnabled: Boolean,
        imageModel: ImageModel?
    ) {
        Column(
            modifier = Modifier.padding(LARGE_PADDING).fillMaxHeight().fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ShuttleNavigationButton(buttonsEnabled, imageModel)
            Text(
                text = context.getString(R.string.the_app_will_not_crash_using_shuttle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color(context.getColor(android.R.color.holo_green_dark)),
                fontFamily = FontFamily.SansSerif
            )
            NormalNavigationButton(buttonsEnabled, imageModel)
            Text(
                text = context.getString(R.string.the_app_will_crash_without_using_shuttle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color(context.getColor(android.R.color.holo_red_dark)),
                fontFamily = FontFamily.SansSerif
            )
        }
    }

    @Composable
    private fun ShuttleNavigationButton(buttonsEnabled: Boolean, imageModel: ImageModel?) {
        Button(
            onClick = { viewModel.processIntent(CargoTransportIntent.NavigateWithShuttle(imageModel)) },
            enabled = buttonsEnabled,
            contentPadding = BUTTON_CONTENT_PADDING,
            modifier = Modifier.fillMaxWidth().padding(SMALL_PADDING)
        ) {
            Text(context.resources.getString(R.string.navigate_using_shuttle), style = MaterialTheme.typography.titleLarge)
        }
    }

    @Composable
    private fun NormalNavigationButton(buttonsEnabled: Boolean, imageModel: ImageModel?) {
        Button(
            onClick = { viewModel.processIntent(CargoTransportIntent.NavigateNormally(imageModel)) },
            enabled = buttonsEnabled,
            contentPadding = BUTTON_CONTENT_PADDING,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = SMALL_PADDING, top = TOP_PADDING, end = SMALL_PADDING, bottom = SMALL_PADDING)
        ) {
            Text(context.resources.getString(R.string.navigate_normally), style = MaterialTheme.typography.titleLarge)
        }
    }

    private fun executeShuttleNavigation(imageModel: ImageModel?) {
        val cargoId = ImageMessageType.ImageData.value
        shuttle.intentCargoWith(context, MVISecondViewActivity::class.java)
            .logTag(TAG)
            .transport(cargoId, imageModel)
            .cleanShuttleOnReturnTo(MVIFirstViewActivity::class.java, MVISecondViewActivity::class.java, cargoId)
            .deliver(context)
    }

    private fun executeNormalNavigation(imageModel: ImageModel?) {
        val cargoId = ImageMessageType.ImageData.value
        val intent = Intent(context, MVISecondViewActivity::class.java)
        intent.putExtra(cargoId, imageModel as Serializable)
        context.startActivity(intent)
    }
}
