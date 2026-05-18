package com.grarcht.shuttle.demo.mvvm.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.grarcht.shuttle.demo.core.R
import com.grarcht.shuttle.demo.core.animation.playAnimationOverlay
import com.grarcht.shuttle.demo.core.io.IOResult
import com.grarcht.shuttle.demo.core.view.CardWithCutoutView
import com.grarcht.shuttle.demo.core.view.applySystemBarTopInset
import com.grarcht.shuttle.demo.mvvm.viewmodel.FirstViewModel
import com.grarcht.shuttle.demo.mvvm.viewmodel.NavigationEvent
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

private const val ERROR_UNABLE_TO_GET_IMAGE = "Unable to get the image byte array."
private const val LOG_TAG = "MVVMFirstViewFragment"

@AndroidEntryPoint
class MVVMFirstViewFragment : Fragment() {
    private var navNormallyButton: Button? = null
    private var navWithShuttleButton: Button? = null
    private val viewModel by viewModels<FirstViewModel>()

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layoutId = R.layout.first_view
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.first_view_title_text).text = getString(com.grarcht.shuttle.demo.mvvm.R.string.first_view_title)
        view.findViewById<CardWithCutoutView>(R.id.shuttle_card)?.setCardColor(ContextCompat.getColor(view.context, R.color.colorTaupe))
        view.findViewById<CardWithCutoutView>(R.id.risky_card)?.setCardColor(ContextCompat.getColor(view.context, R.color.colorBeige))
        view.applySystemBarTopInset(R.id.content_layout)
        initOnClickNavigateWithShuttle(view)
        initOnClickNavigateNormally(view)
        viewModel.loadImage(resources, R.raw.cargo)
        observeUiState()
        observeNavigation()
    }

    override fun onResume() {
        super.onResume()
        enableButtons(viewModel.currentImageModel() != null)
    }

    private fun enableButtons(enable: Boolean) {
        navWithShuttleButton?.isEnabled = enable
        navNormallyButton?.isEnabled = enable
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { result ->
                    when (result) {
                        is IOResult.Unknown,
                        is IOResult.Loading -> enableButtons(false)

                        is IOResult.Success<*> -> enableButtons(true)

                        is IOResult.Error<*> -> {
                            val msg = result.throwable.message ?: ERROR_UNABLE_TO_GET_IMAGE
                            view?.let { Snackbar.make(it, msg, Snackbar.LENGTH_SHORT).show() }
                        }
                    }
                }
            }
        }
    }

    private fun initOnClickNavigateWithShuttle(view: View?) {
        view?.apply {
            navWithShuttleButton = findViewById(R.id.nav_with_shuttle_button)
            navWithShuttleButton?.setOnClickListener {
                it.isEnabled = false
                viewModel.onNavigateWithShuttle()
            }
            findViewById<ImageView>(R.id.preview_shuttle_button)?.setOnClickListener {
                playAnimationOverlay(R.raw.shuttle_delivery_success)
            }
        }
    }

    private fun initOnClickNavigateNormally(view: View?) {
        view?.apply {
            navNormallyButton = findViewById(R.id.nav_without_shuttle_button)
            navNormallyButton?.setOnClickListener {
                it.isEnabled = false
                viewModel.onNavigateNormally()
            }
            findViewById<ImageView>(R.id.preview_without_shuttle_button)?.setOnClickListener {
                playAnimationOverlay(R.raw.shuttle_delivery_fail)
            }
        }
    }

    private fun observeNavigation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    val ctx = context ?: return@collect
                    when (event) {
                        is NavigationEvent.WithShuttle -> {
                            shuttle.intentCargoWith(ctx, MVVMSecondViewActivity::class.java)
                                .logTag(LOG_TAG)
                                .transport(event.cargo.cargoId, event.cargo.imageModel)
                                .cleanShuttleOnReturnTo(MVVMFirstViewFragment::class.java, MVVMSecondViewActivity::class.java, event.cargo.cargoId)
                                .deliver(ctx)
                        }
                        is NavigationEvent.Normally -> {
                            val intent = Intent(ctx, MVVMSecondViewActivity::class.java)
                            intent.putExtra(event.cargo.cargoId, event.cargo.imageModel as Serializable)
                            ctx.startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "MVVMFirstViewFragment"
    }
}
