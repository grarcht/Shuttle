package com.grarcht.shuttle.demo.mvvm.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.grarcht.shuttle.demo.mvvm.R
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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

    private fun addMainFragmentToContainer() {
        if (viewFragment.isAdded.not()) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, viewFragment, MVVMViewFragment.TAG)
                .commit()
        }
    }
}
