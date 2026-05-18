package com.grarcht.shuttle.demo.mvvmcomposewithnavigation.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.grarcht.shuttle.demo.core.image.IMAGE_CARGO_ID
import com.grarcht.shuttle.demo.core.viewmodel.FirstViewModel
import com.grarcht.shuttle.demo.mvvmcomposewithnavigation.R
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.addons.navigation.navigateWithShuttle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val LOG_NAV_CONTROLLER_NOT_FOUND = "NavController not found."
private const val LOG_TAG = "FirstFragment"

/**
 * The host fragment for the first screen in the MVVM with Compose and Navigation demo. Loads the
 * image cargo and navigates to [SecondActivity] via the Navigation component, using
 * [com.grarcht.shuttle.framework.addons.navigation.navigateWithShuttle] for safe transport or a
 * standard navigation action to demonstrate the crash scenario.
 */
@AndroidEntryPoint
class FirstFragment : Fragment() {
    private val viewModel by viewModels<FirstViewModel>()
    private var navController: NavController? = null

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(inflater.context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    FirstScreen(
                        viewModel = viewModel,
                        onNavigateWithShuttle = ::navigateWithShuttle,
                        onNavigateNormally = ::navigateNormally
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadImage(resources, com.grarcht.shuttle.demo.core.R.raw.cargo)
        try {
            navController = view.findNavController()
        } catch (e: IllegalStateException) {
            Log.e(LOG_TAG, LOG_NAV_CONTROLLER_NOT_FOUND, e)
        }
    }

    private fun navigateWithShuttle() {
        val imageModel = viewModel.currentImageModel() ?: return
        navController.navigateWithShuttle(shuttle, R.id.action_firstFragment_to_secondActivity)
            ?.logTag(LOG_TAG)
            ?.transport(cargoId = IMAGE_CARGO_ID, cargo = imageModel)
            ?.cleanShuttleOnReturnTo(
                currentScreenClass = FirstFragment::class.java,
                nextScreenClass = SecondActivity::class.java,
                cargoId = IMAGE_CARGO_ID
            )
            ?.deliver()
    }

    private fun navigateNormally() {
        val imageModel = viewModel.currentImageModel() ?: return
        val args = Bundle()
        args.putSerializable(IMAGE_CARGO_ID, imageModel)
        navController?.navigate(R.id.action_firstFragment_to_secondActivity, args)
    }
}
