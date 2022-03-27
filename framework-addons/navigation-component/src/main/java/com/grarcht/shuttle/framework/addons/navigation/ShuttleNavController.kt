package com.grarcht.shuttle.framework.addons.navigation

import android.os.Bundle
import android.util.Log
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.grarcht.shuttle.framework.BuildConfig
import com.grarcht.shuttle.framework.content.bundle.BundleFactory
import com.grarcht.shuttle.framework.content.bundle.DefaultBundleFactory
import com.grarcht.shuttle.framework.content.bundle.ShuttleBundle
import com.grarcht.shuttle.framework.model.ShuttleParcelCargo
import com.grarcht.shuttle.framework.screen.ShuttleFacade
import com.grarcht.shuttle.framework.warehouse.ShuttleWarehouse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.Serializable

private const val DEFAULT_LOG_TAG = "ShuttleNavigator"

@SuppressWarnings("LongParameterList")
class ShuttleNavController(
    private val shuttleWarehouse: ShuttleWarehouse,
    private val shuttleScreenFacade: ShuttleFacade,
    private val navController: NavController,
    private val navDirections: NavDirections? = null,
    @IdRes private val resId: Int? = null,
    private val navOptions: NavOptions? = null,
    private val navigatorExtras: Navigator.Extras? = null,
    private val internalBundle: Bundle?,
    backgroundThreadDispatcher: CoroutineDispatcher = Dispatchers.IO,
    mainThreadDispatcher: CoroutineDispatcher = Dispatchers.Main
) {
    private val backgroundThreadCoroutineScope = CoroutineScope(backgroundThreadDispatcher)
    private val mainThreadCoroutineScope = CoroutineScope(mainThreadDispatcher)
    private var logTag: String? = null
    private var storeCargoJob: Job? = null

    /**
     * The specific tag to use if there is an issue with the [ShuttleNavController] functionality.
     */
    fun logTag(tag: String?): ShuttleNavController {
        logTag = tag ?: DEFAULT_LOG_TAG
        return this
    }

    /**
     * Sets the [serializable] for transport.
     *
     * @param cargoId the key used for shuttle the cargo to and from the [ShuttleWarehouse]
     * @param serializable the cargo to shuttle
     * @return the [ShuttleBundle] reference use with function chaining
     */

    fun transport(cargoId: String, serializable: Serializable?): ShuttleNavController {
        val parcelPackage = ShuttleParcelCargo(cargoId)
        internalBundle?.putParcelable(cargoId, parcelPackage)

        serializable?.let {
            storeCargoJob = backgroundThreadCoroutineScope.launch {
                shuttleWarehouse.store(cargoId, it)
            }
        }

        return this
    }

    /**
     * Cleans the Shuttle on returning to the [currentScreenClass] from the [nextScreenClass] via
     * the [cargoId].
     * @param currentScreenClass the current activity class reference
     * @param nextScreenClass the string for the name of the next screen's class
     * @param cargoId the id for the cargo shipped with Shuttle
     */
    fun cleanShuttleOnReturnTo(
        currentScreenClass: Class<*>,
        nextScreenClass: Class<*>,
        cargoId: String
    ): ShuttleNavController {
        shuttleScreenFacade.apply {
            removeCargoAfterDelivery(currentScreenClass, nextScreenClass, cargoId)
        }
        return this
    }

    /**
     * Navigates to the destination.  This is a terminal function.
     * @throws [IllegalArgumentException] in Debug builds if the [transport] function has not been called.
     */
    @Throws(exceptionClasses = [IllegalAccessException::class])
    fun deliver() {
        verifyWithFunctionWasCalled()
        this.storeCargoJob?.invokeOnCompletion { throwable ->
            throwable?.let {
                Log.e(logTag, "There was an issues when transporting the data with the Shuttle Intent.", throwable)
            }
            navigate()
        }
    }

    private fun navigate() {
        mainThreadCoroutineScope.launch {
            internalBundle?.let {
                if (navDirections != null) {
                    val directions = ShuttleNavDirections(navDirections.actionId, internalBundle)
                    when {
                        navOptions != null -> navController.navigate(directions, navOptions)
                        navigatorExtras != null -> navController.navigate(directions, navigatorExtras)
                        else -> navController.navigate(directions)
                    }
                } else if (resId != null) {
                    navController.navigate(resId, internalBundle, navOptions, navigatorExtras)
                }
            }
        }
    }

    private fun verifyWithFunctionWasCalled() {
        val isInternalBundleEmpty = internalBundle?.isEmpty ?: true
        if (BuildConfig.DEBUG && isInternalBundleEmpty) {
            val message = "$logTag.  ShuttleNavigator was used; however, the transport function was not called."
            throw IllegalStateException(message)
        } else if (isInternalBundleEmpty) {
            Log.w(logTag, "ShuttleNavigator was used; however, the transport function was not called.")
        }
    }

    companion object {
        /**
         * Navigates to the destination and uses Shuttle to a [ShuttleBundle] to pass into [Navigator.navigate].
         * @param navDirections for destination node to navigate to
         * @param navOptions Additional options for navigation
         * @param navigatorExtras Extras unique to your Navigator
         * @return The NavDestination that should be added to the back stack or null if no change was made to
         * the back stack (i.e., in cases of single top operations where the destination is already on top of
         * the back stack).
         */
        fun navigateWith(
            shuttleWarehouse: ShuttleWarehouse,
            shuttleScreenFacade: ShuttleFacade,
            navController: NavController,
            navDirections: NavDirections,
            navOptions: NavOptions? = null,
            navigatorExtras: Navigator.Extras? = null,
            bundleFactory: BundleFactory? = DefaultBundleFactory(),
            backgroundThreadDispatcher: CoroutineDispatcher = Dispatchers.IO,
            mainThreadDispatcher: CoroutineDispatcher = Dispatchers.Main
        ): ShuttleNavController {
            val newBundle = bundleFactory?.create() as Bundle
            return ShuttleNavController(
                shuttleWarehouse,
                shuttleScreenFacade,
                navController,
                navDirections,
                navOptions = navOptions,
                navigatorExtras = navigatorExtras,
                internalBundle = newBundle,
                backgroundThreadDispatcher = backgroundThreadDispatcher,
                mainThreadDispatcher = mainThreadDispatcher
            )
        }

        /**
         * Navigates to the destination and uses Shuttle to a [ShuttleBundle] to pass into [Navigator.navigate].
         * @param resId id for the node to navigate to
         * @param navOptions Additional options for navigation
         * @param navigatorExtras Extras unique to your Navigator
         * @return The NavDestination that should be added to the back stack or null if no change was made to
         * the back stack (i.e., in cases of single top operations where the destination is already on top of
         * the back stack).
         */
        fun navigateWith(
            shuttleWarehouse: ShuttleWarehouse,
            shuttleScreenFacade: ShuttleFacade,
            navController: NavController,
            @IdRes resId: Int,
            navOptions: NavOptions? = null,
            navigatorExtras: Navigator.Extras? = null,
            bundleFactory: BundleFactory? = DefaultBundleFactory(),
            backgroundThreadDispatcher: CoroutineDispatcher = Dispatchers.IO,
            mainThreadDispatcher: CoroutineDispatcher = Dispatchers.Main
        ): ShuttleNavController {
            val newBundle = bundleFactory?.create() as Bundle
            return ShuttleNavController(
                shuttleWarehouse,
                shuttleScreenFacade,
                navController,
                resId = resId,
                navOptions = navOptions,
                navigatorExtras = navigatorExtras,
                internalBundle = newBundle,
                backgroundThreadDispatcher = backgroundThreadDispatcher,
                mainThreadDispatcher = mainThreadDispatcher
            )
        }
    }
}
