package com.grarcht.shuttle.demo.mvvmwithcompose.ui

import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource

/**
 * Create a [Painter] from an Android resource id. This can load an instance of
 * [BitmapPainter]for [ImageBitmap] based assets. The resources with the given
 * id must point to either fully rasterized images (ex. PNG or JPG files)s.
 *
 * Example:
 * @sample androidx.compose.ui.samples.PainterResourceSample
 *
 * Alternative Drawable implementations can be used with compose by calling
 * [drawIntoCanvas] and drawing with the Android framework canvas provided through [nativeCanvas]
 *
 * Example:
 * @sample androidx.compose.ui.samples.AndroidDrawableInDrawScopeSample
 *
 * @param id Resources object to query the image file from
 *
 * @return [Painter] used for drawing the loaded resource
 */
@Composable
fun rawPainterResource(@RawRes id: Int): Painter {
    val context = LocalContext.current
    val res = context.resources
    val value = remember { TypedValue() }
    res.getValue(id, value, true)
    val path = value.string
    val imageBitmap = remember(path, id) { loadImageBitmapResource(res, id) }
    return BitmapPainter(imageBitmap)
}

/**
 * Helper method to validate the asset resource is a supported resource type and returns
 * an ImageBitmap resource. Because this throws exceptions we cannot have this implementation
 * as part of the composable implementation it is invoked in.
 */
private fun loadImageBitmapResource(res: Resources, id: Int): ImageBitmap {
    try {
        return ImageBitmap.imageResource(res, id)
    } catch (throwable: Throwable) {
        throw IllegalArgumentException(errorMessage)
    }
}

private const val errorMessage = "Only rasterized asset types are supported ex. PNG, JPG"