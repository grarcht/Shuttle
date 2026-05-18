package com.grarcht.shuttle.demo.mvvmwithcompose.view

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.grarcht.shuttle.demo.core.activity.setupEdgeToEdge
import com.grarcht.shuttle.demo.mvvmwithcompose.viewmodel.SecondViewModel
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The destination activity for the second screen in the MVVM with Compose demo. Receives cargo
 * from the first screen and sets up the Compose content via [MVVMSecondView]. Instance state
 * is preserved through Shuttle on configuration change.
 */
@AndroidEntryPoint
class MVVMSecondViewActivity : ComponentActivity() {
    private val viewModel: SecondViewModel by viewModels()
    private lateinit var mvvmSecondView: MVVMSecondView

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreate(savedInstanceState: Bundle?) {
        setupEdgeToEdge()
        window.setBackgroundDrawable(ContextCompat.getColor(this, com.grarcht.shuttle.demo.core.R.color.colorBeige).toDrawable())
        super.onCreate(savedInstanceState)
        mvvmSecondView = MVVMSecondView(this, viewModel, shuttle)
        setContent {
            MaterialTheme {
                mvvmSecondView.SetViewContent(savedInstanceState, intent.extras)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        // Saving the state in a guarded fashion.  The super call must have the shuttle bundle to ensure apps do not
        // crash during transactions with saving and restoring states.
        val outStateShuttleBundle = mvvmSecondView.getSavedInstanceState(shuttle, outState)
        super.onSaveInstanceState(outStateShuttleBundle, outPersistentState)
    }

    override fun onDestroy() {
        mvvmSecondView.cleanUpViewResources()
        super.onDestroy()
    }
}
