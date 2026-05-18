package com.grarcht.shuttle.demo.mvvm.view

import androidx.fragment.app.Fragment
import com.grarcht.shuttle.demo.core.activity.DemoFirstScreenActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MVVMFirstViewActivity : DemoFirstScreenActivity() {
    override val demoFragment: Fragment = MVVMFirstViewFragment()
    override val fragmentTag: String = MVVMFirstViewFragment.TAG
}
