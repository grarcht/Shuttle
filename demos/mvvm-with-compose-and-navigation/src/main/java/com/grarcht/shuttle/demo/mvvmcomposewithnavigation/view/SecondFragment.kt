package com.grarcht.shuttle.demo.mvvmcomposewithnavigation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.grarcht.shuttle.demo.core.image.IMAGE_CARGO_ID
import com.grarcht.shuttle.demo.core.os.getParcelableWith
import com.grarcht.shuttle.demo.mvvmcomposewithnavigation.viewmodel.SecondViewModel
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.model.ShuttleParcelCargo
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val LOG_TAG = "SecondFragment"

@AndroidEntryPoint
class SecondFragment : Fragment() {
    private val viewModel by viewModels<SecondViewModel>()
    private var storedCargoId: String? = null

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Extract args before setContent so cargoId is available for the initial composition.
        extractArgsFrom(savedInstanceState, arguments)
        return ComposeView(inflater.context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    SecondScreen(
                        viewModel = viewModel,
                        shuttle = shuttle,
                        cargoId = storedCargoId ?: ""
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val imageModel = viewModel.currentImageModel()
        val outStateShuttleBundle = shuttle.bundleCargoWith(outState)
            .logTag(LOG_TAG)
            .transport(IMAGE_CARGO_ID, imageModel)
            .create()
        super.onSaveInstanceState(outStateShuttleBundle)
    }

    private fun extractArgsFrom(savedInstanceState: Bundle?, arguments: Bundle?) {
        val bundle: Bundle? = savedInstanceState ?: arguments
        bundle?.let {
            val cargo: ShuttleParcelCargo? =
                it.getParcelableWith(IMAGE_CARGO_ID, ShuttleParcelCargo::class.java)
            storedCargoId = cargo?.cargoId
        }
    }
}
