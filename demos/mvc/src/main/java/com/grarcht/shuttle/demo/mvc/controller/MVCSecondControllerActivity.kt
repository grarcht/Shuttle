package com.grarcht.shuttle.demo.mvc.controller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.grarcht.shuttle.demo.mvc.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MVCSecondControllerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_activity)
        initContainer(savedInstanceState)
    }

    private fun initContainer(savedInstanceState: Bundle?) {
        if (null == savedInstanceState) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MVCSecondControllerFragment())
                .commit()
        }
    }
}
