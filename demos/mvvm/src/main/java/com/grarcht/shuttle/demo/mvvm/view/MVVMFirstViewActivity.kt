package com.grarcht.shuttle.demo.mvvm.view

import androidx.fragment.app.Fragment
import com.grarcht.shuttle.demo.core.activity.DemoFirstScreenActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * The entry point activity for the MVVM demo. Hosts [MVVMFirstViewFragment] and delegates
 * lifecycle management and [com.grarcht.shuttle.framework.Shuttle] cleanup to
 * [com.grarcht.shuttle.demo.core.activity.DemoFirstScreenActivity].
 */
@AndroidEntryPoint
class MVVMFirstViewActivity : DemoFirstScreenActivity() {
    override val demoFragment: Fragment = MVVMFirstViewFragment()
    override val fragmentTag: String = MVVMFirstViewFragment.TAG
}
