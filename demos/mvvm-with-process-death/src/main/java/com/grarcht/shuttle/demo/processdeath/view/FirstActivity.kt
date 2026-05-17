package com.grarcht.shuttle.demo.processdeath.view

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

@AndroidEntryPoint
class FirstActivity : ComponentActivity() {
    private val viewModel: FirstViewModel by viewModels()
    private lateinit var firstView: FirstView

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreate(savedInstanceState: Bundle?) {
        setupEdgeToEdge()
        super.onCreate(savedInstanceState)
        firstView = FirstView(this, viewModel, shuttle)
        viewModel.loadImage(resources, com.grarcht.shuttle.demo.core.R.raw.cargo)
        setContent {
            MaterialTheme {
                firstView.SetViewContent()
            }
        }
    }

    override fun onDestroy() {
        firstView.cleanUpViewResources()
        super.onDestroy()
    }
}
