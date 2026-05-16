package com.grarcht.shuttle.demo.mvvm.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import com.grarcht.shuttle.demo.core.activity.setupEdgeToEdge
import com.grarcht.shuttle.demo.mvvm.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MVVMSecondViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setupEdgeToEdge()
        window.setBackgroundDrawable(0xFFD1C7BD.toInt().toDrawable())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_activity)
        initContainer(savedInstanceState)
    }

    private fun initContainer(savedInstanceState: Bundle?) {
        if (null == savedInstanceState) {
            val fragment = MVVMSecondViewFragment().apply { arguments = intent.extras }
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commitNow()
        }
    }
}
