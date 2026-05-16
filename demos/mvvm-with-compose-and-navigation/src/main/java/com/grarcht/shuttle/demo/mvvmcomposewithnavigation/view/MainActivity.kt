package com.grarcht.shuttle.demo.mvvmcomposewithnavigation.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import com.grarcht.shuttle.demo.core.activity.disableWindowContentClipping
import com.grarcht.shuttle.demo.core.activity.setupEdgeToEdge
import com.grarcht.shuttle.demo.mvvmcomposewithnavigation.R
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CHANNEL_CAPACITY = 4
private const val LOG_TAG = "MainActivity"
private const val NAV_HOST_TAG = "nav host"
private const val LOG_REMOVING_ALL_CARGO = "Removing all cargo."
private const val LOG_REMOVED_ALL_CARGO = "Removed all cargo."
private const val LOG_FAILED_TO_REMOVE_CARGO = "Failed to remove cargo."

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreate(savedInstanceState: Bundle?) {
        setupEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        disableWindowContentClipping()
        initNavHost(savedInstanceState)
    }

    private fun initNavHost(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) return
        var navHostFragment = supportFragmentManager.findFragmentByTag(NAV_HOST_TAG)
        if (navHostFragment != null && navHostFragment.isAdded) return
        if (navHostFragment == null) {
            navHostFragment = NavHostFragment.create(R.navigation.nav_graph, intent.extras)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, navHostFragment, NAV_HOST_TAG)
            .setPrimaryNavigationFragment(navHostFragment)
            .commit()
    }

    override fun onDestroy() {
        cleanShuttleFromAllDeliveries()
        super.onDestroy()
    }

    private fun cleanShuttleFromAllDeliveries() {
        val channel = Channel<ShuttleRemoveCargoResult>(CHANNEL_CAPACITY)
        shuttle.cleanShuttleFromAllDeliveries(channel)
        // A standalone scope is used here instead of lifecycleScope because the activity's
        // lifecycle scope is cancelled during onDestroy, creating a race condition. This scope
        // is self-cancelling: it is cancelled as soon as the terminal result is received.
        val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        cleanupScope.launch {
            channel.consumeAsFlow().collectLatest {
                when (it) {
                    is ShuttleRemoveCargoResult.Removing -> Log.d(LOG_TAG, LOG_REMOVING_ALL_CARGO)
                    is ShuttleRemoveCargoResult.Removed -> {
                        Log.d(LOG_TAG, LOG_REMOVED_ALL_CARGO)
                        cleanupScope.cancel()
                    }
                    is ShuttleRemoveCargoResult.UnableToRemove<*> -> {
                        Log.e(LOG_TAG, LOG_FAILED_TO_REMOVE_CARGO, it.throwable)
                        cleanupScope.cancel()
                    }
                    else -> { /* ignore */ }
                }
            }
        }
    }
}
