package com.grarcht.shuttle.demo.mvc.controller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.grarcht.shuttle.demo.mvc.R
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The activity used to start the demo app.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mvcFirstControllerFragment = MVCFirstControllerFragment()

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
        if (mvcFirstControllerFragment.isAdded.not()) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, mvcFirstControllerFragment, MVCFirstControllerFragment.TAG)
                .commit()
        }
    }
}
