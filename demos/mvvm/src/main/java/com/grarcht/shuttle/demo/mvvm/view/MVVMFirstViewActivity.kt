package com.grarcht.shuttle.demo.mvvm.view

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.grarcht.shuttle.demo.core.activity.disableWindowContentClipping
import com.grarcht.shuttle.demo.core.activity.setupEdgeToEdge
import com.grarcht.shuttle.demo.mvvm.R
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MVVMFirstViewActivity : FragmentActivity() {
    private val firstViewFragment = MVVMFirstViewFragment()

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreate(savedInstanceState: Bundle?) {
        setupEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        disableWindowContentClipping()
        addMainFragmentToContainer()
    }

    override fun onDestroy() {
        // Ensure all persisted cargo data is removed.
        shuttle.cleanShuttleFromAllDeliveries()
        super.onDestroy()
    }

    private fun addMainFragmentToContainer() {
        if (firstViewFragment.isAdded.not()) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, firstViewFragment, MVVMFirstViewFragment.TAG)
                .commit()
        }
    }
}
