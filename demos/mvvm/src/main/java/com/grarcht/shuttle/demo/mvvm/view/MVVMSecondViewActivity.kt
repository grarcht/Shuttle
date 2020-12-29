package com.grarcht.shuttle.demo.mvvm.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.grarcht.shuttle.demo.mvvm.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MVVMSecondViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_activity)
        initContainer(savedInstanceState)
    }

    private fun initContainer(savedInstanceState: Bundle?) {
        if (null == savedInstanceState) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MVVMSecondViewFragment())
                .commitNow()
        }
    }
}
