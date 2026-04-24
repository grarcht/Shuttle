package com.grarcht.shuttle.demo.mviwithcompose.view

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.grarcht.shuttle.demo.mviwithcompose.viewmodel.SecondViewModel
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The destination activity for the second view in the MVI with Compose demo. It
 * receives cargo from the first view, sets up the Compose content via [MVISecondView],
 * and handles instance state persistence through Shuttle.
 */
@AndroidEntryPoint
class MVISecondViewActivity : ComponentActivity() {
    private val viewModel: SecondViewModel by viewModels()
    private lateinit var mviSecondView: MVISecondView

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mviSecondView = MVISecondView(this, viewModel, shuttle)
        setContent {
            mviSecondView.SetViewContent(savedInstanceState, intent.extras)
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        val outStateShuttleBundle = mviSecondView.getSavedInstanceState(shuttle, outState)
        super.onSaveInstanceState(outStateShuttleBundle, outPersistentState)
    }

    override fun onDestroy() {
        mviSecondView.cleanUpViewResources()
        super.onDestroy()
    }
}
