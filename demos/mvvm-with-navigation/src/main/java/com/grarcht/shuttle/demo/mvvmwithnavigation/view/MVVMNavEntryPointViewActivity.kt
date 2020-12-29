package com.grarcht.shuttle.demo.mvvmwithnavigation.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.grarcht.shuttle.demo.mvvmwithnavigation.R
import com.grarcht.shuttle.framework.Shuttle
import com.grarcht.shuttle.framework.result.ShuttleRemoveCargoResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LOG_TAG = "MVVMNavEntryPointViewActivity"
private const val NAV_HOST_TAG = "nav host"

@AndroidEntryPoint
class MVVMNavEntryPointViewActivity : AppCompatActivity() {
    private var view: View? = null

    @Inject
    lateinit var shuttle: Shuttle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initFragmentContainerView()
    }

    private fun initView() {
        view = LayoutInflater.from(this).inflate(R.layout.mvvm_nav_entry_point_view_activity, null)
        setContentView(view)
    }

    private fun initFragmentContainerView() {
        var navHostFragment = supportFragmentManager.findFragmentByTag(NAV_HOST_TAG)
        if (null != navHostFragment && navHostFragment.isAdded) {
            return
        } else if (null == navHostFragment) {
            navHostFragment = NavHostFragment.create(R.navigation.first_nav_graph, intent.extras)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, navHostFragment, NAV_HOST_TAG)
            .setPrimaryNavigationFragment(navHostFragment)
            .commit()
    }

    override fun onDestroy() {
        cleanShuttleFromAllDeliveries()
        super.onDestroy()
    }

    private fun cleanShuttleFromAllDeliveries() {
        val channel = Channel<ShuttleRemoveCargoResult>(3)
        shuttle.cleanShuttleFromAllDeliveries(channel)

        // The following could be useful for verification, analytics, et cetera.
        MainScope().launch {
            // Ensure all persisted cargo data is removed.
            channel.consumeAsFlow().collect {
                when (it) {
                    is ShuttleRemoveCargoResult.Removing -> {
                        Log.d(LOG_TAG, "Removing all of the cargo.")
                    }
                    is ShuttleRemoveCargoResult.Removed -> {
                        Log.d(LOG_TAG, "Removed all of the cargo.")
                        cancel()
                    }
                    is ShuttleRemoveCargoResult.UnableToRemove<*> -> {
                        Log.e(LOG_TAG, "Removed all of the cargo.", it.throwable)
                        cancel()
                    }
                    else -> {
                        // ignore
                    }
                }
            }
        }
    }
}
