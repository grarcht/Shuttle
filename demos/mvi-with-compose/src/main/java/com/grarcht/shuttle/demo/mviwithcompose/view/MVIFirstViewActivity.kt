package com.grarcht.shuttle.demo.mviwithcompose.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.grarcht.shuttle.demo.mviwithcompose.viewmodel.FirstViewModel
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The entry point activity for the first view in the MVI with Compose demo. It sets
 * up the Compose content via [MVIFirstView] and delegates lifecycle events to it.
 */
@AndroidEntryPoint
class MVIFirstViewActivity : ComponentActivity() {
    private val viewModel: FirstViewModel by viewModels()
    private lateinit var mviFirstView: MVIFirstView

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mviFirstView = MVIFirstView(this, viewModel, shuttle)
        setContent {
            mviFirstView.SetViewContent()
        }
    }

    override fun onDestroy() {
        mviFirstView.cleanUpViewResources()
        super.onDestroy()
    }
}
