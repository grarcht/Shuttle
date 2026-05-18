package com.grarcht.shuttle.demo.mvc.controller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.grarcht.shuttle.demo.core.activity.setupEdgeToEdge
import com.grarcht.shuttle.demo.mvc.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * The destination activity for the second screen in the MVC demo. Hosts
 * [MVCSecondControllerFragment] and passes the incoming [android.content.Intent] extras as
 * arguments so the fragment can retrieve or pick up the cargo.
 */
@AndroidEntryPoint
class MVCSecondControllerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setupEdgeToEdge()
        window.setBackgroundDrawable(ContextCompat.getColor(this, com.grarcht.shuttle.demo.core.R.color.colorBeige).toDrawable())
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
