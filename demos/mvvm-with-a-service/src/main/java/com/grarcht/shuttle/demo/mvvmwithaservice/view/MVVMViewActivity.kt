package com.grarcht.shuttle.demo.mvvmwithaservice.view

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.grarcht.shuttle.demo.mvvmwithaservice.R
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Part of the view component for adding/displaying the views (fragments), cleaning shuttle, etc.
 */
@AndroidEntryPoint
class MVVMViewActivity : AppCompatActivity() {
    private val viewFragment = MVVMViewFragment()

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        addMainFragmentToContainer()
    }

    override fun onDestroy() {
        // Ensure all persisted cargo data is removed.
        shuttle.cleanShuttleFromAllDeliveries()
        super.onDestroy()
    }

    @SuppressLint("CommitTransaction")
    private fun addMainFragmentToContainer() {
        if (viewFragment.isAdded.not()) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, viewFragment, MVVMViewFragment.TAG)
                .commit()
        }
    }
}
