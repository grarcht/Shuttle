package com.grarcht.shuttle.demo.mvvmcomposewithnavigation.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.grarcht.shuttle.demo.mvvmcomposewithnavigation.R
import dagger.hilt.android.AndroidEntryPoint

private const val NAV_HOST_TAG = "nav host"

/**
 * The destination activity for the second screen in the MVVM with Compose and Navigation demo.
 * Hosts a secondary Navigation component graph with [SecondFragment] as the start destination,
 * passing the incoming [android.content.Intent] extras as navigation arguments.
 */
@AndroidEntryPoint
class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        initNavHost(savedInstanceState)
    }

    private fun initNavHost(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) return
        var navHostFragment = supportFragmentManager.findFragmentByTag(NAV_HOST_TAG)
        if (navHostFragment != null && navHostFragment.isAdded) return
        if (navHostFragment == null) {
            navHostFragment = NavHostFragment.create(R.navigation.second_nav_graph, intent.extras)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_second, navHostFragment, NAV_HOST_TAG)
            .setPrimaryNavigationFragment(navHostFragment)
            .commit()
    }
}
