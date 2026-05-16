package com.grarcht.shuttle.demo.mvc.controller

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.grarcht.shuttle.demo.core.activity.disableWindowContentClipping
import com.grarcht.shuttle.demo.core.activity.setupEdgeToEdge
import com.grarcht.shuttle.demo.mvc.R
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The activity used to start the demo app.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val mvcFirstControllerFragment = MVCFirstControllerFragment()

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
        if (mvcFirstControllerFragment.isAdded.not()) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, mvcFirstControllerFragment, MVCFirstControllerFragment.TAG)
                .commit()
        }
    }
}
