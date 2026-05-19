package com.grarcht.shuttle.demo.processdeath.view

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import com.grarcht.shuttle.demo.core.compose.R.drawable
import com.grarcht.shuttle.demo.core.compose.ui.DemoFirstScreenLayout
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavAnimation
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavCard
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavCardRiskyColor
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavCardShuttleColor
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavRiskyButtonColor
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavShuttleButtonColor
import com.grarcht.shuttle.demo.core.image.IMAGE_CARGO_ID
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.core.viewmodel.FirstViewModel
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.io.Serializable
import com.grarcht.shuttle.demo.core.compose.R.string as coreString

private const val LOG_TAG = "FirstView"

/**
 * The Composable view for the first screen in the MVVM with Process Death demo. Presents two
 * navigation options: using [com.grarcht.shuttle.framework.Shuttle] to transport cargo safely,
 * or passing the image directly via [android.content.Intent] to demonstrate the crash scenario.
 *
 * @param context the context used to access resources and start activities.
 * @param viewModel the view model that loads the image and exposes the UI state.
 * @param shuttle the Shuttle instance used for safe cargo transport.
 */
class FirstView(
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
        val buttonsEnabled = uiState is IOResult.Success<*>
        var animationRes by remember { mutableStateOf<Int?>(null) }
        NavigationLayout(buttonsEnabled, onAnimationRes = { animationRes = it })
        animationRes?.let { DemoNavAnimation(animationRes = it) { animationRes = null } }
    }

    @Composable
    private fun NavigationLayout(buttonsEnabled: Boolean, onAnimationRes: (Int) -> Unit) {
        DemoFirstScreenLayout(
            architectureLabel = "MVVM + Compose + Process Death",
            backgroundPainter = painterResource(com.grarcht.shuttle.demo.core.R.drawable.shuttle_bg)
        ) {
            ShuttleCard(buttonsEnabled, onAnimationRes)
            RiskyCard(buttonsEnabled, onAnimationRes)
        }
    }

    @Composable
    private fun RowScope.ShuttleCard(enabled: Boolean, onAnimationRes: (Int) -> Unit) {
        DemoNavCard(
            eyebrow = context.getString(coreString.transport_cargo),
            subtitle = context.getString(coreString.with_shuttle),
            title = context.getString(coreString.avoid_crashes),
            buttonLabel = context.getString(coreString.explore),
            imagePainter = painterResource(drawable.workingtruck),
            cardColor = DemoNavCardShuttleColor,
            buttonColor = DemoNavShuttleButtonColor,
            enabled = enabled,
            onPreviewClick = { onAnimationRes(com.grarcht.shuttle.demo.core.R.raw.shuttle_delivery_success) },
            onClick = ::navigateWithShuttle
        )
    }

    @Composable
    private fun RowScope.RiskyCard(enabled: Boolean, onAnimationRes: (Int) -> Unit) {
        DemoNavCard(
            eyebrow = context.getString(coreString.transport_cargo),
            subtitle = context.getString(coreString.without_shuttle),
            title = context.getString(coreString.risk_it),
            buttonLabel = context.getString(coreString.go_ahead_try_it),
            imagePainter = painterResource(drawable.brokentruck),
            cardColor = DemoNavCardRiskyColor,
            buttonColor = DemoNavRiskyButtonColor,
            enabled = enabled,
            onPreviewClick = { onAnimationRes(com.grarcht.shuttle.demo.core.R.raw.shuttle_delivery_fail) },
            onClick = ::navigateNormally
        )
    }

    /**
     * Cleans up all cargo delivered by Shuttle from the warehouse. Call this when the view is
     * no longer needed, such as in [android.app.Activity.onDestroy].
     */
    fun cleanUpViewResources() {
        val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        cleanupScope.launch {
            shuttle.cleanShuttleFromAllDeliveries().consumeAsFlow().collectLatest {
                when (it) {
                    is ShuttleRemoveCargoResult.Removed,
                    is ShuttleRemoveCargoResult.UnableToRemove<*>,
                    is ShuttleRemoveCargoResult.DoesNotExist -> cleanupScope.cancel()
                    else -> { /* await next result */ }
                }
            }
        }
    }

    private fun navigateWithShuttle() {
        val imageModel = viewModel.currentImageModel() ?: return
        val cargoId = IMAGE_CARGO_ID
        shuttle.intentCargoWith(context, SecondActivity::class.java)
            .logTag(LOG_TAG)
            .transport(cargoId, imageModel)
            .cleanShuttleOnReturnTo(FirstActivity::class.java, SecondActivity::class.java, cargoId)
            .deliver(context)
    }

    private fun navigateNormally() {
        val imageModel = viewModel.currentImageModel() ?: return
        val intent = Intent(context, SecondActivity::class.java)
        val cargoId = IMAGE_CARGO_ID
        intent.putExtra(cargoId, imageModel as Serializable)
        context.startActivity(intent)
    }
}
