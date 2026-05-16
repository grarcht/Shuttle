package com.grarcht.shuttle.demo.core.compose.ui

import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource

/**
 * Returns a [Painter] for a raw resource image. Only rasterized formats such as PNG
 * and JPG are supported. Returns a transparent [ColorPainter] for unsupported asset types.
 *
 * @param id the raw resource ID of the image to load as a painter.
 */
@Composable
fun rawPainterResource(@RawRes id: Int): Painter {
    val context = LocalContext.current
    val res = context.resources
    val value = remember { TypedValue() }
    res.getValue(id, value, true)
    val path = value.string
    val imageBitmap = remember(path, id) { loadImageBitmapResource(res, id) }
    return if (imageBitmap != null) BitmapPainter(imageBitmap) else ColorPainter(Color.Transparent)
}

private fun loadImageBitmapResource(res: Resources, id: Int): ImageBitmap? {
    @Suppress("SwallowedException")
    return try {
        ImageBitmap.imageResource(res, id)
    } catch (@Suppress("TooGenericExceptionCaught") throwable: Throwable) {
        null
    }
}
