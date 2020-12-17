package com.grarcht.shuttle.framework.content.bundle

import android.os.*
import android.util.Size
import android.util.SizeF
import android.util.SparseArray
import androidx.lifecycle.LifecycleOwner
import com.grarcht.shuttle.framework.content.ShuttleDataExtractor
import com.grarcht.shuttle.framework.content.ShuttleResult
import com.grarcht.shuttle.framework.model.ShuttleParcelPackage
import com.grarcht.shuttle.framework.respository.ShuttleWarehouse
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly
import java.io.Serializable
import java.util.*

private const val DEFAULT_BUNDLE_KEY = "key_default"

open class ShuttleBundle(
    private var repository: ShuttleWarehouse,
    private var internalBundle: Bundle? = EMPTY
) {
    init {
        safelyPutBundle(DEFAULT_BUNDLE_KEY, internalBundle)
    }

    fun safelyPutBundle(key: String?, value: Bundle?): ShuttleBundle {
        if (null != key && null != value) {
            GlobalScope.launch {
                val parcelPackage = ShuttleParcelPackage(repository.id, key)
                (internalBundle as Bundle).putParcelable(key, parcelPackage)
                repository.save(key, value)
            }
        }
        return this
    }

    fun create(): Bundle {
        return internalBundle as Bundle
    }

    companion object {
        private var bundleFactory: BundleFactory = DefaultBundleFactory()

        @JvmField
        val EMPTY: Bundle = bundleFactory.create()

        fun with(
            repository: ShuttleWarehouse,
            bundleFactory: BundleFactory = DefaultBundleFactory()
        ): ShuttleBundle {
            this.bundleFactory = bundleFactory
            return ShuttleBundle(repository)
        }

        fun with(
            bundle: Bundle,
            repository: ShuttleWarehouse,
            bundleFactory: BundleFactory = DefaultBundleFactory()
        ): ShuttleBundle {
            this.bundleFactory = bundleFactory
            return ShuttleBundle(repository, bundle)
        }
    }
}