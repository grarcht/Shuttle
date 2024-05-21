package com.grarcht.shuttle.demo.mvvmwithcompose.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.grarcht.shuttle.demo.mvvmwithcompose.viewmodel.FirstViewModel
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MVVMFirstViewActivity : ComponentActivity() {
    private val viewModel: FirstViewModel by viewModels()
    private lateinit var mvvmFirstView: MVVMFirstView

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mvvmFirstView = MVVMFirstView(this, viewModel, shuttle)

        setContent {
            mvvmFirstView.SetViewContent()
        }
    }

    override fun onDestroy() {
        mvvmFirstView.cleanUpViewResources()
        super.onDestroy()
    }
}
