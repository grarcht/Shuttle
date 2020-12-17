package com.grarcht.shuttle.framework.content.bundle

import android.os.Bundle
import android.os.PersistableBundle

class DefaultBundleFactory : BundleFactory {
    override fun create(): Bundle {
        return Bundle()
    }

    override fun create(loader: ClassLoader?): Bundle {
        return Bundle(loader)
    }

    override fun create(capacity: Int): Bundle {
        return Bundle(capacity)
    }

    override fun create(bundle: Bundle): Bundle {
        return Bundle(bundle)
    }

    override fun create(bundle: PersistableBundle): Bundle {
        return Bundle(bundle)
    }
}