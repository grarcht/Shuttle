package com.grarcht.shuttle.demo.core.compose.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grarcht.shuttle.demo.core.compose.R

// Non-style constants (alphabetical). TextStyle constants follow below — they reference
// Color values here and must be declared after them due to JVM static init ordering.
private val CARD_BUTTON_PADDING_H = 16.dp
private val CARD_BUTTON_PADDING_V = 8.dp
private val CARD_CONTENT_PADDING = 12.dp
private val CARD_IMAGE_HEIGHT = 90.dp
private val CARD_IMAGE_INSET = 4.dp
private val CARD_IMAGE_SHAPE = RoundedCornerShape(12.dp)
private val CARD_SHAPE_RADIUS = 16.dp
private val CARD_SHAPE = RoundedCornerShape(CARD_SHAPE_RADIUS)
private val CARD_TEXT_DARK = Color(0xFF322D29)
private val CARD_TEXT_GAP_LG = 10.dp
private val CARD_TEXT_GAP_SM = 2.dp
private val CARD_TEXT_MID = Color(0xFF72383D)
private val CARDS_HORIZONTAL_GAP = 8.dp
private val CARDS_HORIZONTAL_PADDING = 16.dp
private val FULLY_ROUNDED_SHAPE = RoundedCornerShape(50)
private val PILL_BACKGROUND_COLOR = Color(0x26AC9C8D)
private val PILL_BORDER_COLOR = Color(0xFFAC9C8D)
private val PILL_BORDER_WIDTH = 1.dp
private val PILL_PADDING_H = 16.dp
private val PILL_PADDING_V = 6.dp
private val SCRIM_COLOR = Color(0x8C322D29)
private val SPACING_PILL_TO_TITLE = 12.dp
private val SPACING_SUBTITLE_TO_CARDS = 24.dp
private val SPACING_TITLE_TO_SUBTITLE = 8.dp
private val SLIVER_OVERLAP = 8.dp

// Extends the composable beyond its layout bounds on top by SLIVER_OVERLAP and on the bottom
// by max(SLIVER_OVERLAP, extraBottomPx). This covers the small gap at the window top
// (Android 15+) and the navigation bar area at the bottom.
private fun Modifier.extendBeyondBounds(extraBottomPx: Int = 0) = layout { measurable, constraints ->
    val overlapPx = SLIVER_OVERLAP.roundToPx()
    val bottomPx = maxOf(overlapPx, extraBottomPx)
    val newMaxHeight = if (constraints.hasBoundedHeight) {
        constraints.maxHeight + overlapPx + bottomPx
    } else {
        constraints.maxHeight
    }
    val placeable = measurable.measure(constraints.copy(minHeight = newMaxHeight, maxHeight = newMaxHeight))
    layout(placeable.width, constraints.maxHeight) {
        placeable.placeRelative(0, -overlapPx)
    }
}

private val SPACING_TOP = 8.dp
private val SUBTITLE_COLOR = Color(0xFFD1C7BD)
private val TAGLINE_BOTTOM_PADDING = 32.dp
private val TAGLINE_START_PADDING = 20.dp
private val TITLE_COLOR = Color(0xFFEFE9E1)

// TextStyle constants (alphabetical). Declared after Color constants above.
private val CARD_BUTTON_STYLE = TextStyle(
    color = TITLE_COLOR,
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.5.sp
)
private val CARD_EYEBROW_STYLE = TextStyle(
    color = CARD_TEXT_DARK,
    fontSize = 14.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.5.sp
)
private val CARD_SUBTITLE_STYLE = TextStyle(
    color = CARD_TEXT_MID,
    fontSize = 14.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.3.sp
)
private val CARD_TITLE_STYLE = TextStyle(
    color = CARD_TEXT_DARK,
    fontSize = 20.sp,
    fontWeight = FontWeight.Bold
)
private val PILL_STYLE = TextStyle(
    color = PILL_BORDER_COLOR,
    fontSize = 11.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 2.sp
)
private val SUBTITLE_STYLE = TextStyle(
    color = SUBTITLE_COLOR,
    fontSize = 16.sp,
    fontWeight = FontWeight.Light,
    textAlign = TextAlign.Center,
    letterSpacing = 6.sp
)
private val TAGLINE_STYLE = TextStyle(
    color = TITLE_COLOR,
    fontSize = 64.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.sp,
    lineHeight = 68.sp
)
private const val ARC_ANGLE_HALF = 180f
private const val ARC_ANGLE_QUARTER = 90f
private const val DISABLED_ALPHA = 0.5f

private val CUTOUT_RADIUS = 28.dp

// Distance from the right edge to the cutout center on the top edge. Must exceed
// CARD_SHAPE_RADIUS + CUTOUT_RADIUS so the notch clears the top-right corner arc.
private val CUTOUT_CENTER_X_FROM_RIGHT = CARD_SHAPE_RADIUS + CUTOUT_RADIUS + 8.dp // 46dp
private val PLAY_BUTTON_BG = Color(0xFF322D29)
private val PLAY_BUTTON_SIZE = 36.dp
private val PLAY_BUTTON_ICON_SIZE = 16.dp
private val TITLE_STYLE = TextStyle(
    color = TITLE_COLOR,
    fontSize = 56.sp,
    fontWeight = FontWeight.Black,
    textAlign = TextAlign.Center,
    letterSpacing = 12.sp
)

private class CardWithCutoutShape(
    private val cornerRadius: Dp,
    private val cutoutRadius: Dp,
    private val cutoutCenterXFromRight: Dp // distance from right edge to cutout center on top edge
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val r = with(density) { cornerRadius.toPx() }
        val cr = with(density) { cutoutRadius.toPx() }
        val cx = w - with(density) { cutoutCenterXFromRight.toPx() } // cutout center x on top edge
        val path = Path().apply {
            moveTo(r, 0f)
            lineTo(cx - cr, 0f)
            arcTo(Rect(cx - cr, -cr, cx + cr, cr), ARC_ANGLE_HALF, -ARC_ANGLE_HALF, false) // concave notch on top edge
            lineTo(w - r, 0f)
            arcTo(Rect(w - 2 * r, 0f, w, 2 * r), -ARC_ANGLE_QUARTER, ARC_ANGLE_QUARTER, false) // top-right corner
            lineTo(w, h - r)
            arcTo(Rect(w - 2 * r, h - 2 * r, w, h), 0f, ARC_ANGLE_QUARTER, false) // bottom-right corner
            lineTo(r, h)
            arcTo(Rect(0f, h - 2 * r, 2 * r, h), ARC_ANGLE_QUARTER, ARC_ANGLE_QUARTER, false) // bottom-left corner
            lineTo(0f, r)
            arcTo(Rect(0f, 0f, 2 * r, 2 * r), ARC_ANGLE_HALF, ARC_ANGLE_QUARTER, false) // top-left corner
            close()
        }
        return Outline.Generic(path)
    }
}

val DemoNavCardRiskyColor = Color(0xFFD1C7BD)
val DemoNavCardShuttleColor = Color(0xFFAC9C8D)
val DemoNavRiskyButtonColor = Color(0xFF72383D)
val DemoNavShuttleButtonColor = Color(0xFF322D29)

/**
 * Shared first-screen layout for all demos. Renders a full-screen background image with a
 * dark scrim, a top-center architecture label pill, the SHUTTLE hero title, and a content
 * slot for side-by-side navigation cards.
 *
 * @param architectureLabel Short label shown in the pill (e.g. "MVVM", "MVI + Compose").
 * @param backgroundPainter Painter for the full-screen background image.
 * @param content Row slot for [DemoNavCard] composables laid out side by side.
 */
@Composable
fun DemoFirstScreenLayout(
    architectureLabel: String,
    backgroundPainter: Painter,
    content: @Composable RowScope.() -> Unit
) {
    val navBarBottomPx = WindowInsets.navigationBars.getBottom(LocalDensity.current)
    Box(modifier = Modifier.fillMaxSize()) {
        FullScreenBackground(backgroundPainter, navBarBottomPx)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeroHeader(architectureLabel)
            Spacer(modifier = Modifier.height(SPACING_SUBTITLE_TO_CARDS))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CARDS_HORIZONTAL_PADDING),
                horizontalArrangement = Arrangement.spacedBy(CARDS_HORIZONTAL_GAP)
            ) {
                content()
            }
            ResponsiveTagline()
        }
    }
}

@Composable
private fun FullScreenBackground(backgroundPainter: Painter, navBarBottomPx: Int) {
    Image(
        painter = backgroundPainter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .extendBeyondBounds(navBarBottomPx)
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .extendBeyondBounds(navBarBottomPx)
            .background(SCRIM_COLOR)
    )
}

@Composable
private fun ColumnScope.HeroHeader(architectureLabel: String) {
    Spacer(modifier = Modifier.height(SPACING_TOP))
    ArchitecturePill(label = architectureLabel)
    Spacer(modifier = Modifier.height(SPACING_PILL_TO_TITLE))
    BasicText(
        text = stringResource(R.string.shuttle_title),
        style = TITLE_STYLE,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(SPACING_TITLE_TO_SUBTITLE))
    BasicText(
        text = stringResource(R.string.shuttle_subtitle),
        style = SUBTITLE_STYLE,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * A branded portrait navigation card with an image on top and text below.
 * Place two of these inside [DemoFirstScreenLayout]'s content slot to get
 * a side-by-side pair. Each card takes equal width via [RowScope.weight].
 */
@Suppress("LongParameterList")
@Composable
fun RowScope.DemoNavCard(
    eyebrow: String,
    subtitle: String,
    title: String,
    buttonLabel: String,
    imagePainter: Painter,
    cardColor: Color,
    buttonColor: Color,
    enabled: Boolean = true,
    onPreviewClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    // Outer Box: no clip so play button can sit on the card edge
    Box(
        modifier = Modifier
            .weight(1f)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
    ) {
        // Card body clipped with cutout shape when play button is present
        NavCardBody(
            imagePainter = imagePainter,
            eyebrow = eyebrow,
            subtitle = subtitle,
            title = title,
            buttonLabel = buttonLabel,
            buttonColor = buttonColor,
            cardColor = cardColor,
            hasPlayButton = onPreviewClick != null,
            enabled = enabled,
            onClick = onClick
        )
        // Arc stroke tracing the cutout edge, drawn on top of the image in card color
        if (onPreviewClick != null) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CUTOUT_RADIUS)
            ) {
                val cx = size.width - CUTOUT_CENTER_X_FROM_RIGHT.toPx()
                val cr = CUTOUT_RADIUS.toPx()
                drawArc(
                    color = cardColor,
                    startAngle = 180f,
                    sweepAngle = -180f,
                    useCenter = false,
                    topLeft = Offset(cx - cr, -cr),
                    size = Size(cr * 2, cr * 2),
                    style = Stroke(width = CARD_IMAGE_INSET.toPx())
                )
            }
        }
        // Play button centered on the cutout (right edge, image/text boundary)
        if (onPreviewClick != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(
                        x = -(CUTOUT_CENTER_X_FROM_RIGHT - PLAY_BUTTON_SIZE / 2),
                        y = -PLAY_BUTTON_SIZE / 2
                    )
                    .size(PLAY_BUTTON_SIZE)
                    .clip(FULLY_ROUNDED_SHAPE)
                    .background(PLAY_BUTTON_BG)
                    .clickable(enabled = enabled, onClick = onPreviewClick),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_play),
                    contentDescription = null,
                    modifier = Modifier.size(PLAY_BUTTON_ICON_SIZE)
                )
            }
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun NavCardBody(
    imagePainter: Painter,
    eyebrow: String,
    subtitle: String,
    title: String,
    buttonLabel: String,
    buttonColor: Color,
    cardColor: Color,
    hasPlayButton: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(if (hasPlayButton) CardWithCutoutShape(CARD_SHAPE_RADIUS, CUTOUT_RADIUS, CUTOUT_CENTER_X_FROM_RIGHT) else CARD_SHAPE)
            .background(cardColor)
    ) {
        Column {
            Image(
                painter = imagePainter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = CARD_IMAGE_INSET, end = CARD_IMAGE_INSET, top = CARD_IMAGE_INSET)
                    .height(CARD_IMAGE_HEIGHT)
                    .clip(CARD_IMAGE_SHAPE),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(CARD_CONTENT_PADDING)) {
                BasicText(text = eyebrow, style = CARD_EYEBROW_STYLE)
                Spacer(modifier = Modifier.height(CARD_TEXT_GAP_SM))
                BasicText(text = subtitle, style = CARD_SUBTITLE_STYLE)
                Spacer(modifier = Modifier.height(CARD_TEXT_GAP_SM))
                BasicText(text = title, style = CARD_TITLE_STYLE)
                Spacer(modifier = Modifier.height(CARD_TEXT_GAP_LG))
                Box(
                    modifier = Modifier
                        .clip(FULLY_ROUNDED_SHAPE)
                        .background(buttonColor)
                        .clickable(enabled = enabled, onClick = onClick)
                        .padding(horizontal = CARD_BUTTON_PADDING_H, vertical = CARD_BUTTON_PADDING_V)
                ) {
                    BasicText(text = buttonLabel, style = CARD_BUTTON_STYLE)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.ResponsiveTagline() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicText(
            text = stringResource(R.string.tagline),
            style = TAGLINE_STYLE,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = TAGLINE_START_PADDING, bottom = TAGLINE_BOTTOM_PADDING)
        )
    }
}

@Composable
private fun ArchitecturePill(label: String) {
    BasicText(
        text = label,
        style = PILL_STYLE,
        modifier = Modifier
            .wrapContentWidth()
            .background(color = PILL_BACKGROUND_COLOR, shape = FULLY_ROUNDED_SHAPE)
            .border(width = PILL_BORDER_WIDTH, color = PILL_BORDER_COLOR, shape = FULLY_ROUNDED_SHAPE)
            .padding(horizontal = PILL_PADDING_H, vertical = PILL_PADDING_V)
    )
}
