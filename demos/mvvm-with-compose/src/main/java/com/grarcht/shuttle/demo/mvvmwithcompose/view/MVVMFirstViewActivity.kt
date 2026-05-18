package com.grarcht.shuttle.demo.mvvmwithcompose.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.grarcht.shuttle.demo.core.activity.setupEdgeToEdge
import com.grarcht.shuttle.demo.core.viewmodel.FirstViewModel
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The entry point activity for the MVVM with Compose demo. Sets up the Compose content via
 * [MVVMFirstView] and releases view resources when the activity is destroyed.
 */
@AndroidEntryPoint
class MVVMFirstViewActivity : ComponentActivity() {
    private val viewModel: FirstViewModel by viewModels()
    private lateinit var mvvmFirstView: MVVMFirstView

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreate(savedInstanceState: Bundle?) {
        setupEdgeToEdge()
        super.onCreate(savedInstanceState)
        mvvmFirstView = MVVMFirstView(this, viewModel, shuttle)
        setContent {
            MaterialTheme {
                mvvmFirstView.SetViewContent()
            }
        }
    }

    override fun onDestroy() {
        mvvmFirstView.cleanUpViewResources()
        super.onDestroy()
    }
}
