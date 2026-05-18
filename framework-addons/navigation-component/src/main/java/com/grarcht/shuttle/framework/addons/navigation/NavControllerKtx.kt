package com.grarcht.shuttle.framework.addons.navigation

import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.grarcht.shuttle.framework.ExcludeFromCoverage
import com.grarcht.shuttle.framework.Shuttle

/**
 * Navigates to the destination identified by [resId] using Shuttle's warehouse and facade to
 * safely carry cargo across the navigation boundary. Returns null if this [NavController] is null.
 *
 * @param shuttle The [Shuttle] instance that provides the warehouse and facade for cargo transport.
 * @param resId The resource ID of the destination to navigate to.
 * @return A [ShuttleNavController] for chaining transport and cleanup calls, or null if this
 * [NavController] is null.
 */
fun NavController?.navigateWithShuttle(
    shuttle: Shuttle,
    @IdRes resId: Int
): ShuttleNavController? = navigateWithShuttleResId(shuttle, resId)

@ExcludeFromCoverage
private fun NavController?.navigateWithShuttleResId(
    shuttle: Shuttle,
    @IdRes resId: Int
): ShuttleNavController? =
    this?.let {
        ShuttleNavController.navigateWith(
            shuttle.shuttleWarehouse,
            shuttle.shuttleFacade,
            this,
            resId = resId
        )
    }

/**
 * Navigates to the destination identified by [resId] using Shuttle's warehouse and facade to
 * safely carry cargo across the navigation boundary, applying the given [NavOptions]. Returns
 * null if this [NavController] is null.
 *
 * @param shuttle The [Shuttle] instance that provides the warehouse and facade for cargo transport.
 * @param resId The resource ID of the destination to navigate to.
 * @param navOptions Options that control how the navigation is performed, such as animations and
 * back stack behavior. Pass null to use the default options.
 * @return A [ShuttleNavController] for chaining transport and cleanup calls, or null if this
 * [NavController] is null.
 */
fun NavController?.navigateWithShuttle(
    shuttle: Shuttle,
    @IdRes resId: Int,
    navOptions: NavOptions?
): ShuttleNavController? = navigateWithShuttleResIdOptions(shuttle, resId, navOptions)

@ExcludeFromCoverage
private fun NavController?.navigateWithShuttleResIdOptions(
    shuttle: Shuttle,
    @IdRes resId: Int,
    navOptions: NavOptions?
): ShuttleNavController? =
    this?.let {
        ShuttleNavController.navigateWith(
            shuttle.shuttleWarehouse,
            shuttle.shuttleFacade,
            this,
            resId = resId,
            navOptions = navOptions
        )
    }

/**
 * Navigates to the destination described by [directions] using Shuttle's warehouse and facade to
 * safely carry cargo across the navigation boundary, optionally applying [NavOptions]. Returns
 * null if this [NavController] is null.
 *
 * @param shuttle The [Shuttle] instance that provides the warehouse and facade for cargo transport.
 * @param directions The [NavDirections] that identify the destination and any associated arguments.
 * @param navOptions Options that control how the navigation is performed, such as animations and
 * back stack behavior. Pass null to use the default options.
 * @return A [ShuttleNavController] for chaining transport and cleanup calls, or null if this
 * [NavController] is null.
 */
fun NavController?.navigateWithShuttle(
    shuttle: Shuttle,
    directions: NavDirections,
    navOptions: NavOptions? = null
): ShuttleNavController? = navigateWithShuttleDirectionsOptions(shuttle, directions, navOptions)

@ExcludeFromCoverage
private fun NavController?.navigateWithShuttleDirectionsOptions(
    shuttle: Shuttle,
    directions: NavDirections,
    navOptions: NavOptions? = null
): ShuttleNavController? =
    this?.let {
        ShuttleNavController.navigateWith(
            shuttle.shuttleWarehouse,
            shuttle.shuttleFacade,
            this,
            navDirections = directions,
            navOptions = navOptions
        )
    }

/**
 * Navigates to the destination described by [directions] using Shuttle's warehouse and facade to
 * safely carry cargo across the navigation boundary, forwarding [navigatorExtras] to the
 * underlying [Navigator]. Returns null if this [NavController] is null.
 *
 * @param shuttle The [Shuttle] instance that provides the warehouse and facade for cargo transport.
 * @param directions The [NavDirections] that identify the destination and any associated arguments.
 * @param navigatorExtras Additional extras to pass to the [Navigator] responsible for performing
 * the navigation, such as shared element transition data.
 * @return A [ShuttleNavController] for chaining transport and cleanup calls, or null if this
 * [NavController] is null.
 */
fun NavController?.navigateWithShuttle(
    shuttle: Shuttle,
    directions: NavDirections,
    navigatorExtras: Navigator.Extras
): ShuttleNavController? = navigateWithShuttleDirectionsExtras(shuttle, directions, navigatorExtras)

@ExcludeFromCoverage
private fun NavController?.navigateWithShuttleDirectionsExtras(
    shuttle: Shuttle,
    directions: NavDirections,
    navigatorExtras: Navigator.Extras
): ShuttleNavController? =
    this?.let {
        ShuttleNavController.navigateWith(
            shuttle.shuttleWarehouse,
            shuttle.shuttleFacade,
            this,
            navDirections = directions,
            navigatorExtras = navigatorExtras
        )
    }
