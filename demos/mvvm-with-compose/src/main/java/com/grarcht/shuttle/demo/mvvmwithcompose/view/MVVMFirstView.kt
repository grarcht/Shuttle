package com.grarcht.shuttle.demo.mvvmwithcompose.view

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
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.mvvmwithcompose.R
import com.grarcht.shuttle.demo.mvvmwithcompose.viewmodel.FirstViewModel
import com.grarcht.shuttle.framework.Shuttle
import java.io.Serializable

private val SMALL_PADDING = 8.dp
private val LARGE_PADDING = 16.dp
private val TOP_PADDING = 64.dp
private val BUTTON_CONTENT_PADDING = PaddingValues(SMALL_PADDING)
private const val TAG = "MVVMFirstView"

class MVVMFirstView(
    private val context: Context,
    private val viewModel: FirstViewModel,
    private val shuttle: Shuttle
) {
    @Composable
    fun SetViewContent() {
        val uiState by viewModel.uiState.collectAsState()
        val buttonsEnabled = uiState is IOResult.Success<*>

        Box(modifier = Modifier.systemBarsPadding()) {
            TitleText()
            NavigationButtonsColumn(buttonsEnabled)
            LaunchedEffect(Unit) {
                viewModel.loadImage(context.resources, com.grarcht.shuttle.demo.core.R.raw.tower)
            }
        }
    }

    @Composable
    private fun TitleText() {
        Text(
            text = context.resources.getString(R.string.mvvm_first_view_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(LARGE_PADDING).fillMaxHeight().fillMaxWidth()
        )
    }

    @Composable
    private fun NavigationButtonsColumn(buttonsEnabled: Boolean) {
        Column(
            modifier = Modifier.padding(LARGE_PADDING).fillMaxHeight().fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ShuttleNavigationButton(buttonsEnabled)
            Text(
                text = context.getString(R.string.the_app_will_not_crash_using_shuttle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color(context.getColor(android.R.color.holo_green_dark)),
                fontFamily = FontFamily.SansSerif
            )
            NormalNavigationButton(buttonsEnabled)
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
    private fun ShuttleNavigationButton(buttonsEnabled: Boolean) {
        Button(
            onClick = navigateWithShuttle(),
            enabled = buttonsEnabled,
            contentPadding = BUTTON_CONTENT_PADDING,
            modifier = Modifier.fillMaxWidth().padding(SMALL_PADDING)
        ) {
            Text(context.resources.getString(R.string.navigate_using_shuttle), style = MaterialTheme.typography.titleLarge)
        }
    }

    @Composable
    private fun NormalNavigationButton(buttonsEnabled: Boolean) {
        Button(
            onClick = navigateNormally(),
            enabled = buttonsEnabled,
            contentPadding = BUTTON_CONTENT_PADDING,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = SMALL_PADDING, top = TOP_PADDING, end = SMALL_PADDING, bottom = SMALL_PADDING)
        ) {
            Text(context.resources.getString(R.string.navigate_normally), style = MaterialTheme.typography.titleLarge)
        }
    }

    fun cleanUpViewResources() {
        shuttle.cleanShuttleFromAllDeliveries()
    }

    private fun navigateWithShuttle(): () -> Unit {
        return {
            val cargoId = ImageMessageType.ImageData.value
            shuttle.intentCargoWith(context, MVVMSecondViewActivity::class.java)
                .logTag(TAG)
                .transport(cargoId, viewModel.currentImageModel())
                .cleanShuttleOnReturnTo(MVVMFirstViewActivity::class.java, MVVMSecondViewActivity::class.java, cargoId)
                .deliver(context)
        }
    }

    private fun navigateNormally(): () -> Unit {
        return {
            val cargoId = ImageMessageType.ImageData.value
            val intent = Intent(context, MVVMSecondViewActivity::class.java)
            intent.putExtra(cargoId, viewModel.currentImageModel() as Serializable)
            context.startActivity(intent)
        }
    }
}
