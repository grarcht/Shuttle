package com.grarcht.shuttle.demo.mviwithcompose.view

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import com.grarcht.shuttle.demo.core.R
import com.grarcht.shuttle.demo.core.compose.R.drawable
import com.grarcht.shuttle.demo.core.compose.ui.DemoFirstScreenLayout
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavAnimation
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavCard
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavCardRiskyColor
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavCardShuttleColor
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavRiskyButtonColor
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavShuttleButtonColor
import com.grarcht.shuttle.demo.core.image.IMAGE_CARGO_ID
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.mviwithcompose.intent.CargoTransportIntent
import com.grarcht.shuttle.demo.mviwithcompose.navigation.NavigationEvent
import com.grarcht.shuttle.demo.mviwithcompose.state.CargoTransportUiState
import com.grarcht.shuttle.demo.mviwithcompose.viewmodel.FirstViewModel
import com.grarcht.shuttle.framework.Shuttle
import java.io.Serializable
import com.grarcht.shuttle.demo.core.compose.R.string as coreString

private const val TAG = "MVIFirstView"

/**
 * The Composable view for the first screen in the MVI with Compose demo. Dispatches intents to
 * [FirstViewModel] and observes the resulting [com.grarcht.shuttle.demo.mviwithcompose.state.CargoTransportUiState]
 * to enable navigation buttons when the image is loaded. Navigation side effects arrive as
 * [com.grarcht.shuttle.demo.mviwithcompose.navigation.NavigationEvent]s that this view executes.
 *
 * @param context the context used to access resources and start activities.
 * @param viewModel the MVI view model that processes intents and exposes UI state.
 * @param shuttle the Shuttle instance used for safe cargo transport.
 */
class MVIFirstView(
    private val context: Context,
    private val viewModel: FirstViewModel,
    private val shuttle: Shuttle
) {
    /**
     * Renders the first screen layout with navigation cards and a preview animation overlay.
     */
    @Composable
    fun SetViewContent() {
        val uiState by viewModel.uiState.collectAsState()
        var animationRes by remember { mutableStateOf<Int?>(null) }
        ObserveImageLoading()
        ObserveNavigationEvents()
        NavigationLayout(uiState, onAnimationRes = { animationRes = it })
        animationRes?.let { DemoNavAnimation(animationRes = it) { animationRes = null } }
    }

    @Composable
    private fun ObserveImageLoading() {
        LaunchedEffect(Unit) {
            viewModel.processIntent(
                CargoTransportIntent.LoadImage(
                    resources = context.resources,
                    imageId = R.raw.cargo
                )
            )
        }
    }

    @Composable
    private fun ObserveNavigationEvents() {
        LaunchedEffect(Unit) {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is NavigationEvent.NavigateWithShuttle -> executeShuttleNavigation(event.imageModel)
                    is NavigationEvent.NavigateNormally -> executeNormalNavigation(event.imageModel)
                }
            }
        }
    }

    @Composable
    private fun NavigationLayout(uiState: CargoTransportUiState, onAnimationRes: (Int) -> Unit) {
        DemoFirstScreenLayout(
            architectureLabel = "MVI + Compose",
            backgroundPainter = painterResource(R.drawable.shuttle_bg),
        ) {
            ShuttleCard(uiState, onAnimationRes)
            RiskyCard(uiState, onAnimationRes)
        }
    }

    @Composable
    private fun RowScope.ShuttleCard(uiState: CargoTransportUiState, onAnimationRes: (Int) -> Unit) {
        DemoNavCard(
            eyebrow = context.getString(coreString.transport_cargo),
            subtitle = context.getString(coreString.with_shuttle),
            title = context.getString(coreString.avoid_crashes),
            buttonLabel = context.getString(coreString.explore),
            imagePainter = painterResource(drawable.workingtruck),
            cardColor = DemoNavCardShuttleColor,
            buttonColor = DemoNavShuttleButtonColor,
            enabled = uiState.buttonsEnabled,
            onPreviewClick = { onAnimationRes(R.raw.shuttle_delivery_success) },
            onClick = { viewModel.processIntent(CargoTransportIntent.NavigateWithShuttle(uiState.imageModel)) }
        )
    }

    @Composable
    private fun RowScope.RiskyCard(uiState: CargoTransportUiState, onAnimationRes: (Int) -> Unit) {
        DemoNavCard(
            eyebrow = context.getString(coreString.transport_cargo),
            subtitle = context.getString(coreString.without_shuttle),
            title = context.getString(coreString.risk_it),
            buttonLabel = context.getString(coreString.go_ahead_try_it),
            imagePainter = painterResource(drawable.brokentruck),
            cardColor = DemoNavCardRiskyColor,
            buttonColor = DemoNavRiskyButtonColor,
            enabled = uiState.buttonsEnabled,
            onPreviewClick = { onAnimationRes(R.raw.shuttle_delivery_fail) },
            onClick = { viewModel.processIntent(CargoTransportIntent.NavigateNormally(uiState.imageModel)) }
        )
    }

    private fun executeShuttleNavigation(imageModel: ImageModel?) {
        val cargoId = IMAGE_CARGO_ID
        shuttle.intentCargoWith(context, MVISecondViewActivity::class.java)
            .logTag(TAG)
            .transport(cargoId, imageModel)
            .cleanShuttleOnReturnTo(MVIFirstViewActivity::class.java, MVISecondViewActivity::class.java, cargoId)
            .deliver(context)
    }

    private fun executeNormalNavigation(imageModel: ImageModel?) {
        val model = imageModel ?: return
        val cargoId = IMAGE_CARGO_ID
        val intent = Intent(context, MVISecondViewActivity::class.java)
        intent.putExtra(cargoId, model as Serializable)
        context.startActivity(intent)
    }
}
