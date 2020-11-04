package com.grarcht.shuttle.ui.secondary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.grarcht.shuttle.R

class SecondActivity:  AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.secondary_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SecondaryFragment())
                .commitNow()
        }
    }
}