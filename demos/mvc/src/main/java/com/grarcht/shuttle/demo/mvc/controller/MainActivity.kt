package com.grarcht.shuttle.demo.mvc.controller

import androidx.fragment.app.Fragment
import com.grarcht.shuttle.demo.core.activity.DemoFirstScreenActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * The entry point activity for the MVC demo. Hosts [MVCFirstControllerFragment] and delegates
 * lifecycle management and [com.grarcht.shuttle.framework.Shuttle] cleanup to
 * [com.grarcht.shuttle.demo.core.activity.DemoFirstScreenActivity].
 */
@AndroidEntryPoint
class MainActivity : DemoFirstScreenActivity() {
    override val demoFragment: Fragment = MVCFirstControllerFragment()
    override val fragmentTag: String = MVCFirstControllerFragment.TAG
}
