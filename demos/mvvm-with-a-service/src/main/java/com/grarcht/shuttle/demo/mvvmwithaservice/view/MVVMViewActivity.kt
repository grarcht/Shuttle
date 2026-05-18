package com.grarcht.shuttle.demo.mvvmwithaservice.view

import androidx.fragment.app.Fragment
import com.grarcht.shuttle.demo.core.activity.DemoFirstScreenActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Part of the view component for adding/displaying the views (fragments), cleaning shuttle, etc.
 */
@AndroidEntryPoint
class MVVMViewActivity : DemoFirstScreenActivity() {
    override val demoFragment: Fragment = MVVMViewFragment()
    override val fragmentTag: String = MVVMViewFragment.TAG
}
