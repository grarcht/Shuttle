package com.grarcht.shuttle.demo.mvvmwithnavigation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import com.grarcht.shuttle.demo.mvvmwithnavigation.R
import dagger.hilt.android.AndroidEntryPoint

private const val NAV_HOST_TAG = "nav host"

@AndroidEntryPoint
class MVVMNavSecondViewActivity : AppCompatActivity() {
    private var view: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initFragmentContainerView()
    }

    private fun initView() {
        view = LayoutInflater.from(this).inflate(R.layout.mvvm_nav_second_view_activity, null)
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top, bottom = systemBars.bottom)
            insets
        }
    }

    private fun initFragmentContainerView() {
        var navHostFragment = supportFragmentManager.findFragmentByTag(NAV_HOST_TAG)
        if (null != navHostFragment && navHostFragment.isAdded) {
            return
        } else if (null == navHostFragment) {
            navHostFragment = NavHostFragment.create(R.navigation.second_nav_graph, intent.extras)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, navHostFragment, NAV_HOST_TAG)
            .setPrimaryNavigationFragment(navHostFragment)
            .commit()
    }
}
