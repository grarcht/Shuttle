package com.grarcht.shuttle.demo.core.activity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.grarcht.shuttle.demo.core.R
import com.grarcht.shuttle.framework.Shuttle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Abstract base activity for the first screen in each demo. Handles edge-to-edge setup, fragment
 * hosting, and [Shuttle] cleanup on destroy. Subclasses supply the [Fragment] to display and its
 * back stack tag.
 */
@AndroidEntryPoint
abstract class DemoFirstScreenActivity : FragmentActivity() {

    @Inject
    lateinit var shuttle: Shuttle

    /** The [Fragment] to attach to the main container when the activity is first created. */
    protected abstract val demoFragment: Fragment

    /** The tag used to identify [demoFragment] in the fragment back stack. */
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
