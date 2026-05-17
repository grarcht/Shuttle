package com.grarcht.shuttle.demo.mvvmcomposewithnavigation.view

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.grarcht.shuttle.demo.core.R
import com.grarcht.shuttle.demo.core.compose.R.drawable
import com.grarcht.shuttle.demo.core.compose.ui.DemoFirstScreenLayout
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavAnimation
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavCard
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavCardRiskyColor
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavCardShuttleColor
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavRiskyButtonColor
import com.grarcht.shuttle.demo.core.compose.ui.DemoNavShuttleButtonColor
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.core.viewmodel.FirstViewModel
import com.grarcht.shuttle.demo.core.compose.R.string as coreString

@Composable
fun FirstScreen(
    viewModel: FirstViewModel,
    onNavigateWithShuttle: () -> Unit,
    onNavigateNormally: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val buttonsEnabled = uiState is IOResult.Success<*>
    var animationRes by remember { mutableStateOf<Int?>(null) }
    NavigationLayout(buttonsEnabled, onNavigateWithShuttle, onNavigateNormally, onAnimationRes = { animationRes = it })
    animationRes?.let { DemoNavAnimation(animationRes = it) { animationRes = null } }
}

@Composable
private fun NavigationLayout(
    buttonsEnabled: Boolean,
    onNavigateWithShuttle: () -> Unit,
    onNavigateNormally: () -> Unit,
    onAnimationRes: (Int) -> Unit
) {
    DemoFirstScreenLayout(
        architectureLabel = "MVVM + Compose + Nav",
        backgroundPainter = painterResource(R.drawable.shuttle_bg)
    ) {
        ShuttleCard(buttonsEnabled, onNavigateWithShuttle, onAnimationRes)
        RiskyCard(buttonsEnabled, onNavigateNormally, onAnimationRes)
    }
}

@Composable
private fun RowScope.ShuttleCard(enabled: Boolean, onClick: () -> Unit, onAnimationRes: (Int) -> Unit) {
    DemoNavCard(
        eyebrow = stringResource(coreString.transport_cargo),
        subtitle = stringResource(coreString.with_shuttle),
        title = stringResource(coreString.avoid_crashes),
        buttonLabel = stringResource(coreString.explore),
        imagePainter = painterResource(drawable.workingtruck),
        cardColor = DemoNavCardShuttleColor,
        buttonColor = DemoNavShuttleButtonColor,
        enabled = enabled,
        onPreviewClick = { onAnimationRes(R.raw.shuttle_delivery_success) },
        onClick = onClick
    )
}

@Composable
private fun RowScope.RiskyCard(enabled: Boolean, onClick: () -> Unit, onAnimationRes: (Int) -> Unit) {
    DemoNavCard(
        eyebrow = stringResource(coreString.transport_cargo),
        subtitle = stringResource(coreString.without_shuttle),
        title = stringResource(coreString.risk_it),
        buttonLabel = stringResource(coreString.go_ahead_try_it),
        imagePainter = painterResource(drawable.brokentruck),
        cardColor = DemoNavCardRiskyColor,
        buttonColor = DemoNavRiskyButtonColor,
        enabled = enabled,
        onPreviewClick = { onAnimationRes(R.raw.shuttle_delivery_fail) },
        onClick = onClick
    )
}
