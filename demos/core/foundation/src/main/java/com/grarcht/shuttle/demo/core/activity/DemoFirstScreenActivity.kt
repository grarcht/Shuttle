package com.grarcht.shuttle.demo.core.activity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.grarcht.shuttle.demo.core.R
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class DemoFirstScreenActivity : FragmentActivity() {

    @Inject
    lateinit var shuttle: Shuttle

    protected abstract val demoFragment: Fragment
    protected abstract val fragmentTag: String

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        setupEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        disableWindowContentClipping()
        addMainFragmentToContainer()
    }

    override fun onDestroy() {
        shuttle.cleanShuttleFromAllDeliveries()
        super.onDestroy()
    }

    private fun addMainFragmentToContainer() {
        if (demoFragment.isAdded.not()) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, demoFragment, fragmentTag)
                .commit()
        }
    }
}
