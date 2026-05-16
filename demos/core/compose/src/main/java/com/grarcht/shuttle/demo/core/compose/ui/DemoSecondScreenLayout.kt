package com.grarcht.shuttle.demo.core.compose.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grarcht.shuttle.demo.core.compose.R

private const val BYTES_PER_KB = 1024.0
private const val HORIZONTAL_BIAS = 0.4f
private const val CROSSFADE_DURATION_MILLIS = 700
private const val CROSSFADE_LABEL = "secondScreenCrossfade"

private val CARD_BG_COLOR = Color(0xF0322D29)
private val CARD_CORNER_RADIUS = 28.dp
private val CARD_PADDING_H = 20.dp
private val CARD_PADDING_V = 24.dp
private val FULLY_ROUNDED_SHAPE = RoundedCornerShape(50)
private val PILL_BACKGROUND_COLOR = Color(0x26AC9C8D)
private val PILL_BORDER_COLOR = Color(0xFFAC9C8D)
private val PILL_BORDER_WIDTH = 1.dp
private val PILL_PADDING_H = 16.dp
private val PILL_PADDING_V = 6.dp
private val PLACEHOLDER_COLOR = Color(0xFFD1C7BD)
private val SPACING_PILL_TO_TITLE = 12.dp
private val SPACING_TITLE_TO_BODY = 8.dp
private val TITLE_COLOR = Color(0xFFEFE9E1)
private val BODY_COLOR = Color(0xFFD1C7BD)

private val PILL_STYLE = TextStyle(
    color = PILL_BORDER_COLOR,
    fontSize = 11.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 2.sp
)
private val TITLE_STYLE = TextStyle(
    color = TITLE_COLOR,
    fontSize = 52.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 56.sp,
    letterSpacing = 0.sp
)
private val BODY_STYLE = TextStyle(
    color = BODY_COLOR,
    fontSize = 15.sp,
    fontWeight = FontWeight.Light,
    letterSpacing = 0.3.sp,
    lineHeight = 22.sp
)
private val SIZE_STYLE = TextStyle(
    color = PILL_BORDER_COLOR,
    fontSize = 13.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.5.sp
)

@Composable
private fun PillRow(fileSizeBytes: Long?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicText(
            text = stringResource(R.string.second_screen_pill),
            style = PILL_STYLE,
            modifier = Modifier
                .wrapContentWidth()
                .background(color = PILL_BACKGROUND_COLOR, shape = FULLY_ROUNDED_SHAPE)
                .border(width = PILL_BORDER_WIDTH, color = PILL_BORDER_COLOR, shape = FULLY_ROUNDED_SHAPE)
                .padding(horizontal = PILL_PADDING_H, vertical = PILL_PADDING_V)
        )
        fileSizeBytes?.let { bytes ->
            val kb = bytes / BYTES_PER_KB
            val mb = kb / BYTES_PER_KB
            val sizeText = when {
                mb >= 1.0 -> stringResource(R.string.second_screen_image_size_mb, mb.toFloat())
                kb >= 1.0 -> stringResource(R.string.second_screen_image_size_kb, kb.toFloat())
                else -> stringResource(R.string.second_screen_image_size_b, bytes)
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_file_size),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                BasicText(text = sizeText, style = SIZE_STYLE)
            }
        }
    }
}

/**
 * Shared second-screen layout for Compose demos. Renders the transported image full-bleed
 * with a dark rounded card anchored at the bottom carrying success messaging.
 *
 * @param bitmap The decoded image to display. Null shows the placeholder until the image arrives.
 * @param fileSizeBytes Raw byte count of the image data, displayed as a formatted size label.
 * @param bottomContent Optional slot rendered inside the card below the body text (e.g. a kill-process button).
 */
@Composable
fun DemoSecondScreenLayout(
    bitmap: ImageBitmap?,
    fileSizeBytes: Long? = null,
    bottomContent: @Composable () -> Unit = {}
) {
    var alpha by remember { mutableStateOf(0f) }
    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = CROSSFADE_DURATION_MILLIS),
        label = CROSSFADE_LABEL
    )
    LaunchedEffect(bitmap) { alpha = if (bitmap != null) 1f else 0f }

    Box(modifier = Modifier.fillMaxSize().background(PLACEHOLDER_COLOR)) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = BiasAlignment(horizontalBias = HORIZONTAL_BIAS, verticalBias = 0f),
                modifier = Modifier.fillMaxSize().alpha(animatedAlpha)
            )
            BottomCard(animatedAlpha, fileSizeBytes, bottomContent)
        }
    }
}

@Composable
private fun BoxScope.BottomCard(
    animatedAlpha: Float,
    fileSizeBytes: Long?,
    bottomContent: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .alpha(animatedAlpha)
            .clip(RoundedCornerShape(topStart = CARD_CORNER_RADIUS, topEnd = CARD_CORNER_RADIUS))
            .background(CARD_BG_COLOR)
            .navigationBarsPadding()
            .padding(horizontal = CARD_PADDING_H, vertical = CARD_PADDING_V)
    ) {
        PillRow(fileSizeBytes)
        Spacer(modifier = Modifier.height(SPACING_PILL_TO_TITLE))
        BasicText(text = stringResource(R.string.second_screen_title), style = TITLE_STYLE)
        Spacer(modifier = Modifier.height(SPACING_TITLE_TO_BODY))
        BasicText(text = stringResource(R.string.second_screen_body), style = BODY_STYLE)
        bottomContent()
    }
}
