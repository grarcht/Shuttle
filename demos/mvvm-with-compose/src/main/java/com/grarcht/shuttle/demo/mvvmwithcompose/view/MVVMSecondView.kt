package com.grarcht.shuttle.demo.mvvmwithcompose.view

import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import com.grarcht.shuttle.demo.core.compose.ui.DemoSecondScreenLayout
import com.grarcht.shuttle.demo.core.compose.ui.rawPainterResource
import com.grarcht.shuttle.demo.core.image.BitmapDecoder
import com.grarcht.shuttle.demo.core.image.IMAGE_CARGO_ID
import com.grarcht.shuttle.demo.core.image.ImageModel
import com.grarcht.shuttle.demo.core.os.getParcelableWith
import com.grarcht.shuttle.demo.mvvmwithcompose.R
import com.grarcht.shuttle.demo.mvvmwithcompose.viewmodel.SecondViewModel
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.model.ShuttleParcelCargo
import com.grarcht.shuttle.framework.result.ShuttlePickupCargoResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "MVVMSecondView"
private val ERROR_IMAGE_ID = com.grarcht.shuttle.demo.core.R.raw.breakdown
private val PLACEHOLDER_COLOR = Color(0xFFD1C7BD)

/**
 * The Composable view for the second screen in the MVVM with Compose demo. Retrieves cargo from
 * the [com.grarcht.shuttle.framework.Shuttle] warehouse using the cargo ID extracted from the
 * saved instance state or intent extras, and displays the resulting image or an error state.
 *
 * @param context the context used to access resources and string values.
 * @param viewModel the view model that manages cargo pickup state.
 * @param shuttle the Shuttle instance used for cargo pickup and instance state bundling.
 */
class MVVMSecondView(
    private val context: Context,
    private val viewModel: SecondViewModel,
    private val shuttle: Shuttle
) {
    private var storedCargoId: String? = null

    /**
     * Renders the second screen, extracting the cargo ID from [savedInstanceState] or [extras]
     * and triggering cargo pickup via [SecondViewModel].
     *
     * @param savedInstanceState the saved instance state bundle, used on configuration change.
     * @param extras the intent extras bundle, used on first launch.
     */
    @Composable
    fun SetViewContent(
        savedInstanceState: Bundle? = null,
        extras: Bundle? = null
    ) {
        extractArgsFrom(savedInstanceState, extras)

        val cargoId = storedCargoId ?: ""
        val pickupState by viewModel.pickupCargoState.collectAsState()
        val imageModel = (pickupState as? ShuttlePickupCargoResult.Success<*>)?.data as? ImageModel

        LaunchedEffect(cargoId) {
            if (cargoId.isNotEmpty()) {
                viewModel.loadImage(shuttle, cargoId)
            }
        }

        if (cargoId.isEmpty() || pickupState is ShuttlePickupCargoResult.Error<*>) {
            Column(modifier = Modifier.fillMaxSize().background(PLACEHOLDER_COLOR).systemBarsPadding()) {
                ShowErrorImage()
            }
        } else {
            val bitmap by produceState<ImageBitmap?>(null, imageModel) {
                value = withContext(Dispatchers.IO) {
                    imageModel?.let { BitmapDecoder.decodeBitmap(it.imageData)?.asImageBitmap() }
                }
            }
            DemoSecondScreenLayout(bitmap = bitmap, fileSizeBytes = imageModel?.imageData?.size?.toLong())
        }
    }

    private fun extractArgsFrom(savedInstanceState: Bundle?, arguments: Bundle?) {
        val bundle: Bundle? = savedInstanceState ?: arguments
        bundle?.let {
            val cargo: ShuttleParcelCargo? =
                it.getParcelableWith(IMAGE_CARGO_ID, ShuttleParcelCargo::class.java)
            storedCargoId = cargo?.cargoId
        }
    }

    /**
     * Bundles the currently retrieved image model into [outState] using Shuttle so it can be
     * restored safely after a configuration change.
     *
     * @param shuttle the Shuttle instance used to bundle the cargo.
     * @param outState the bundle to write the cargo into.
     * @return the bundle with the cargo added.
     */
    fun getSavedInstanceState(shuttle: Shuttle, outState: Bundle): Bundle {
        val imageModel = (viewModel.pickupCargoState.value as? ShuttlePickupCargoResult.Success<*>)
            ?.data as? ImageModel
        return shuttle
            .bundleCargoWith(outState)
            .logTag(TAG)
            .transport(IMAGE_CARGO_ID, imageModel)
            .create()
    }

    /**
     * Releases view-held resources. State is managed by the ViewModel so this is a no-op in
     * this implementation, but is provided for symmetry with other view classes in the demos.
     */
    fun cleanUpViewResources() {
        // State is managed by the ViewModel; nothing to release here.
    }

    @Composable
    private fun ShowErrorImage() {
        Image(
            painter = rawPainterResource(id = ERROR_IMAGE_ID),
            contentDescription = stringResource(R.string.failure_loading_image)
        )
        Text(text = context.resources.getString(R.string.unable_to_get_image))
    }
}
