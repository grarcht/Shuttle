package com.grarcht.shuttle.demo.mvc.controller

import androidx.fragment.app.Fragment
import com.grarcht.shuttle.demo.core.activity.DemoFirstScreenActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : DemoFirstScreenActivity() {
    override val demoFragment: Fragment = MVCFirstControllerFragment()
    override val fragmentTag: String = MVCFirstControllerFragment.TAG
}
