package com.grarcht.shuttle.framework.content.bundle

import android.os.Bundle
import android.util.Log
import com.grarcht.shuttle.framework.content.ShuttleIntent
import com.grarcht.shuttle.framework.model.ShuttleParcelCargo
import com.grarcht.shuttle.framework.warehouse.ShuttleWarehouse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable

private const val DEFAULT_LOG_TAG = "ShuttleBundle"

/**
 *  This class creates stores a [Bundle] cargo in the [ShuttleWarehouse] and creates a [Bundle] that can be
 *  transported with avoiding the Transaction Too Large Exception.  This class uses the Fluent Interface
 *  Design Pattern to achieve function chaining.  For more information on this design pattern, refer to:
 *  <a href="https://en.wikipedia.org/wiki/Fluent_interface">Fluent Interface</a>
 */
open class ShuttleBundle(
    private val repository: ShuttleWarehouse,
    private val internalBundle: Bundle?,
    backgroundThreadDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val backgroundThreadScope = CoroutineScope(backgroundThreadDispatcher)
    private var logTag: String? = null

    /**
     * The specific tag to use if there is an issue with the [ShuttleIntent] functionality.
     */
    fun logTag(tag: String?): ShuttleBundle {
        logTag = tag ?: DEFAULT_LOG_TAG
        return this
    }

    /**
     * Sets the [serializable] for transport.
     * @param cargoId the key used for shuttle the cargo to and from the [ShuttleWarehouse]
     * @param serializable the cargo to shuttle
     * @return the [ShuttleBundle] reference use with function chaining
     */
    fun transport(cargoId: String, serializable: Serializable?): ShuttleBundle {
        verifyWithFunctionWasCalled()

        val parcelPackage = ShuttleParcelCargo(cargoId)
        internalBundle?.putParcelable(cargoId, parcelPackage)

        backgroundThreadScope.launch {
            repository.store(cargoId, serializable)
        }.invokeOnCompletion {
            it?.let { throwable ->
                Log.e(
                    logTag,
                    "There was an issues when transporting the data with the Shuttle Intent.",
                    throwable
                )
            }
        }
        return this
    }

    /**
     * Creates the [Bundle].
     * @return the reference to the newly created bundle object
     */
    fun create(): Bundle {
        verifyWithFunctionWasCalled()
        return internalBundle as Bundle
    }

    private fun verifyWithFunctionWasCalled() {
        if (internalBundle == null) {
            throw IllegalStateException("$logTag.  The with function must be called first.")
        }
    }

    companion object {
        /**
         * Creates a [ShuttleBundle] for use with transporting cargo and to avoid a Transaction Too Large Exception.
         * @param bundle
         * @param repository
         * @param bundleFactory
         */
        fun with(
            bundle: Bundle?,
            repository: ShuttleWarehouse,
            bundleFactory: BundleFactory? = DefaultBundleFactory()
        ): ShuttleBundle {
            val newBundle = bundle ?: bundleFactory?.create() as Bundle
            return ShuttleBundle(repository, newBundle)
        }
    }
}

