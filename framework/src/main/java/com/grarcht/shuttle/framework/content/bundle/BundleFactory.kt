package com.grarcht.shuttle.framework.content.bundle

import android.os.Bundle
import android.os.PersistableBundle

interface BundleFactory {
    fun create(): Bundle
    fun create(loader: ClassLoader?): Bundle
    fun create(capacity: Int): Bundle
    fun create(bundle: Bundle): Bundle
    fun create(bundle: PersistableBundle): Bundle
}