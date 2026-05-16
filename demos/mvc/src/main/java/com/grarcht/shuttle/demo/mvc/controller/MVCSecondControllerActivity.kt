package com.grarcht.shuttle.demo.mvc.controller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import com.grarcht.shuttle.demo.core.activity.setupEdgeToEdge
import com.grarcht.shuttle.demo.mvc.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MVCSecondControllerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setupEdgeToEdge()
        window.setBackgroundDrawable(0xFFD1C7BD.toInt().toDrawable())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_activity)
        initContainer(savedInstanceState)
    }

    private fun initContainer(savedInstanceState: Bundle?) {
        if (null == savedInstanceState) {
            val fragment = MVCSecondControllerFragment().apply { arguments = intent.extras }
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }
    }
}
